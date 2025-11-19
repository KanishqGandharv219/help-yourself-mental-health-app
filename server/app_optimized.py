from flask import Flask, request, jsonify
from flask_cors import CORS
import sys
import os
import logging
import asyncio
import time
from optimized_embeddings import OptimizedEmbeddings
from transformers import pipeline
import torch

# Configure GPU detection
USE_GPU = torch.cuda.is_available()
if USE_GPU:
    os.environ["CUDA_VISIBLE_DEVICES"] = "0"
    logging.info(f"GPU is available: {torch.cuda.get_device_name(0)}")
else:
    logging.info("GPU not available, using CPU only")

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

# Initialize the optimized embeddings system
embedder = OptimizedEmbeddings(persist_directory="./chroma_db")

# Create collections for different chat types
collections = {
    "GENERAL": "general_conversations",
    "CRISIS_SUPPORT": "crisis_support",
    "THERAPY": "therapy_sessions",
    "WELLNESS": "wellness_advice"
}

# Crisis detection system
CRISIS_KEYWORDS = [
    "suicide", "kill myself", "end my life", "die", "worthless", 
    "cant go on", "hopeless", "nobody cares", "self harm", "hurt myself"
]

def is_high_risk(text: str) -> bool:
    """Check if a message contains crisis indicators"""
    if not text:
        return False
    lower = text.lower()
    return any(kw in lower for kw in CRISIS_KEYWORDS)

# Helpline information
HELPLINES = {
    "tele-manas": {"number": "14416", "desc": "24×7 Government helpline (all Indian languages)"},
    "kiran":      {"number": "1800-599-0019", "desc": "National Mental Health Rehab Helpline"},
    "aasra":      {"number": "+91-22-27546669", "desc": "24×7 Suicide Prevention"},
    "vandrevala": {"number": "+91-9999666555", "desc": "24×7 Free Counseling"},
}

def lookup_helpline(topic: str):
    """Find the most appropriate helpline based on the topic"""
    tl = topic.lower()
    if "suicide" in tl or "die" in tl or "end my life" in tl:
        return HELPLINES["aasra"]
    if "depress" in tl or "depression" in tl or "anxiety" in tl:
        return HELPLINES["tele-manas"]
    # default
    return HELPLINES["tele-manas"]

# Initialize emotion detection (will be loaded on first use to avoid startup delays)
emo_pipeline = None

def get_emotion_pipeline():
    """Lazy loading of emotion detection pipeline"""
    global emo_pipeline
    if emo_pipeline is None:
        try:
            logger.info("Initializing emotion detection pipeline...")
            # Use GPU if available, otherwise fallback to CPU
            device = 0 if USE_GPU else -1
            emo_pipeline = pipeline(
                "text-classification",
                model="j-hartmann/emotion-english-distilroberta-base",
                return_all_scores=True,
                device=device
            )
            logger.info(f"Emotion detection pipeline initialized on {'GPU' if USE_GPU else 'CPU'}")
        except Exception as e:
            logger.warning(f"Could not initialize emotion detection: {str(e)}")
            return None
    return emo_pipeline

def top_emotion(text):
    """Detect the primary emotion in text"""
    pipeline = get_emotion_pipeline()
    if not pipeline or not text:
        return "unknown"
    
    try:
        scores = pipeline(text)[0]
        best = max(scores, key=lambda x: x["score"])
        return best["label"]
    except Exception as e:
        logger.warning(f"Error detecting emotion: {str(e)}")
        return "unknown"

# Session storage to track conversation history
sessions = {}

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Create a simple fallback function that returns a basic response
def basic_response(message):
    return f"I received your message: '{message}'. I'm running in basic mode because the advanced response system couldn't be loaded."

# Add parent directory to Python path to fix import issues
parent_dir = os.path.abspath(os.path.join(os.path.dirname(__file__), '..'))
if parent_dir not in sys.path:
    sys.path.insert(0, parent_dir)
    logger.info(f"Added parent directory to Python path: {parent_dir}")

# Try to import the RAG chain
has_rag_chain = False
try:
    from ollama_rag.modelrag import after_rag_chain
    logger.info("Successfully imported after_rag_chain")
    has_rag_chain = True
except ImportError as e:
    logger.error(f"Could not import after_rag_chain: {str(e)}")
    after_rag_chain = None

# We need to see where the ollama_rag module is
logger.info(f"Current sys.path: {sys.path}")

def get_collection_for_chat_type(chat_type):
    """Get the appropriate collection name for a chat type"""
    return collections.get(chat_type, collections["GENERAL"])

def get_crisis_response(message):
    """Generate a response for crisis situations"""
    helpline = lookup_helpline(message)
    return (
        "I'm really concerned about what you're sharing. Please consider calling:\n"
        f"• {helpline['desc']}: {helpline['number']} (24×7, free)\n"
        "If you ever feel unsafe, please reach out to your nearest emergency services. "
        "Remember that you're not alone, and there are professionals ready to help you through this difficult time."
    )

def get_helpline_response(message):
    """Generate a response for helpline requests"""
    helpline = lookup_helpline(message)
    return f"For support, you can call {helpline['desc']} at {helpline['number']}. This service is available 24/7 and is free of charge."

@app.route('/chat', methods=['POST'])
def chat():
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({"error": "Invalid request: no JSON data"}), 400
            
        message = data.get('message', '')
        chat_type = data.get('chat_type', 'GENERAL')
        session_id = data.get('session_id', 'default-session')
        
        if not message:
            return jsonify({"error": "Message cannot be empty"}), 400
            
        # Log incoming request
        logger.info(f"Received chat request: session={session_id}, type={chat_type}, message='{message[:30]}...'")
        
        # Get collection name based on chat type
        collection_name = collections.get(chat_type, collections["GENERAL"])
        
        # Check for crisis indicators
        is_crisis = is_high_risk(message)
        if is_crisis:
            logger.warning(f"Crisis detected in message: '{message}'")
            helpline = lookup_helpline(message)
            response = f"I notice you may be going through a difficult time. If you need immediate support, please consider contacting {helpline['desc']} at {helpline['number']}. Remember, it's okay to ask for help."
            return jsonify({"response": response})
            
        # Detect emotion
        emotion = top_emotion(message)
        logger.info(f"Detected emotion: {emotion}")
        
        # Prepare context for the model
        # Track session for conversation history
        if session_id not in sessions:
            sessions[session_id] = []
        
        # Add the new message to the session
        sessions[session_id].append({"role": "user", "content": message})
        
        # Generate response
        if has_rag_chain and after_rag_chain:
            logger.info("Using RAG chain for response")
            try:
                # Log before calling the chain
                logger.info("Calling after_rag_chain...")
                
                # Call the RAG chain with the message - fix the invocation method
                start_time = time.time()
                response = after_rag_chain.invoke(message)
                elapsed = time.time() - start_time
                
                logger.info(f"Got response from RAG chain in {elapsed:.2f}s")
            except Exception as e:
                logger.error(f"Error from RAG chain: {str(e)}")
                # Fall back to basic response
                response = basic_response(message)
        else:
            logger.warning("Using basic response (RAG chain not available)")
            response = basic_response(message)
            
        # Add the response to the session
        sessions[session_id].append({"role": "assistant", "content": response})
        
        return jsonify({"response": response})
        
    except Exception as e:
        logger.error(f"Error in chat endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    status = {
        "status": "ok",
        "rag_available": has_rag_chain,
        "embeddings": embedder.status(),
        "emotion_detection": get_emotion_pipeline() is not None,
        "gpu_available": USE_GPU
    }
    return jsonify(status)

if __name__ == '__main__':
    logger.info("Starting optimized mental health chat server...")
    logger.info(f"RAG chain available: {has_rag_chain}")
    logger.info(f"GPU acceleration: {USE_GPU}")
    app.run(host='0.0.0.0', port=5001, debug=True) 