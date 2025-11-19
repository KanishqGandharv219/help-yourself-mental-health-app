from flask import Flask, request, jsonify, Response, stream_with_context
from flask_cors import CORS
import logging
import requests
import json
import time

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

# Ollama API endpoint
OLLAMA_API = "http://localhost:11434/api/generate"
OLLAMA_STREAM_API = "http://localhost:11434/api/generate"
MODEL_NAME = "qwen2.5:latest"
REQUEST_TIMEOUT = 120  # seconds

# Specialized contexts for different chat types
GENERAL_CONTEXT = """“You're an India-focused crisis support assistant. Offer empathetic, supportive guidance for anyone in mental distress, prioritize their safety, suggest immediate coping strategies, and recommend professional help. If they mention self‑harm or harming others, gently urge them to seek urgent assistance and share these 24×7 helplines:
-Tele‑Manas: 14416
-Kiran: 1800‑599‑0019
-Aasra: +91‑22‑27546669"

"""

CRISIS_SUPPORT_CONTEXT = """
You are a crisis support assistant. You are an India specific mental health assistant. The person you're talking to may be experiencing distress
or a mental health crisis. Be supportive, empathetic, and focus on safety. Acknowledge their
feelings, provide immediate coping strategies, and suggest professional resources.
Always prioritize their safety and well-being. If they express thoughts of harm to themselves
or others, gently encourage them to seek immediate professional help and provide these crisis lines:
- Tele-Manas: 14416 (24×7 Government helpline in all Indian languages)
- Kiran: 1800-599-0019 (National Mental Health Rehab Helpline)
- Aasra: +91-22-27546669 (24×7 Suicide Prevention)
"""

THERAPY_CONTEXT = """
You are a therapy assistant providing supportive conversation using evidence-based therapeutic
approaches. You are an India specific mental health assistant. Use techniques like cognitive reframing, validation, open-ended questions, and
reflective listening. Help the user explore their thoughts and feelings, but make it clear
you're not a replacement for a licensed therapist. Encourage healthy coping skills and
self-reflection. Focus on being non-judgmental and supportive. If they express thoughts of harm to themselves
or others, gently encourage them to seek immediate professional help and provide these crisis lines:
- Tele-Manas: 14416 (24×7 Government helpline in all Indian languages)
- Kiran: 1800-599-0019 (National Mental Health Rehab Helpline)
- Aasra: +91-22-27546669 (24×7 Suicide Prevention)
"""

# Session storage
sessions = {}

def get_ollama_response(message, chat_type="GENERAL", session_id=None):
    """Get a response directly from Ollama API with specialized context based on chat type"""
    try:
        # Select the appropriate context based on chat type
        if chat_type == "CRISIS_SUPPORT":
            context = CRISIS_SUPPORT_CONTEXT
        elif chat_type == "THERAPY":
            context = THERAPY_CONTEXT
        else:
            # Default to general context
            context = GENERAL_CONTEXT
            
        logger.info(f"Using context for chat type: {chat_type}")
        
        # Build prompt with session history if available
        prompt = context
        
        # Add conversation history for context
        if session_id and session_id in sessions:
            history = sessions[session_id]
            for exchange in history[-3:]:  # Only use last 3 exchanges for context
                prompt += f"\nUser: {exchange['user']}\nAssistant: {exchange['assistant']}"
        
        # Add current message
        prompt += f"\nUser: {message}\nAssistant:"
        
        # Prepare request to Ollama
        payload = {
            "model": MODEL_NAME,
            "prompt": prompt,
            "stream": False,
            "temperature": 0.7,
            "max_tokens": 500
        }
        
        # Log the request
        logger.info(f"Sending request to Ollama with model {MODEL_NAME} for chat type {chat_type}")
        
        # Send request to Ollama
        start_time = time.time()
        response = requests.post(OLLAMA_API, json=payload, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()  # Raise exception for HTTP errors
        
        # Parse response
        response_data = response.json()
        generated_text = response_data.get("response", "")
        
        # Log timing
        elapsed = time.time() - start_time
        logger.info(f"Received response from Ollama in {elapsed:.2f}s")
        
        # Clean up the response if needed
        if generated_text.strip().startswith("I'm"):
            generated_text = generated_text.strip()
        
        return generated_text
        
    except requests.exceptions.RequestException as e:
        logger.error(f"Error connecting to Ollama API: {str(e)}")
        return f"I'm sorry, I couldn't process your request. There was an error connecting to the AI service: {str(e)}"
        
    except Exception as e:
        logger.error(f"Unexpected error: {str(e)}")
        return f"I'm sorry, I couldn't process your request due to an error: {str(e)}"

def stream_ollama_response(message, chat_type="GENERAL", session_id=None):
    """Stream a response from Ollama API with specialized context based on chat type"""
    try:
        # Select the appropriate context based on chat type
        if chat_type == "CRISIS_SUPPORT":
            context = CRISIS_SUPPORT_CONTEXT
        elif chat_type == "THERAPY":
            context = THERAPY_CONTEXT
        else:
            # Default to general context
            context = GENERAL_CONTEXT
            
        logger.info(f"Using context for chat type: {chat_type} (streaming)")
        
        # Build prompt with session history if available
        prompt = context
        
        # Add conversation history for context
        if session_id and session_id in sessions:
            history = sessions[session_id]
            for exchange in history[-3:]:  # Only use last 3 exchanges for context
                prompt += f"\nUser: {exchange['user']}\nAssistant: {exchange['assistant']}"
        
        # Add current message
        prompt += f"\nUser: {message}\nAssistant:"
        
        # Prepare request to Ollama
        payload = {
            "model": MODEL_NAME,
            "prompt": prompt,
            "stream": True,
            "temperature": 0.7,
            "max_tokens": 500
        }
        
        # Log the request
        logger.info(f"Sending streaming request to Ollama with model {MODEL_NAME} for chat type {chat_type}")
        
        # Send request to Ollama with streaming
        response = requests.post(OLLAMA_STREAM_API, json=payload, stream=True, timeout=REQUEST_TIMEOUT)
        response.raise_for_status()
        
        # Track the full response for session history
        full_response = ""
        
        # Return streaming response
        for line in response.iter_lines():
            if line:
                # Parse the JSON line
                try:
                    chunk = json.loads(line)
                    token = chunk.get("response", "")
                    full_response += token
                    
                    # Format as JSON for the client
                    yield json.dumps({"chunk": token, "done": False}) + "\n"
                    
                except json.JSONDecodeError:
                    logger.warning(f"Failed to decode JSON: {line}")
        
        # Send the final done message
        yield json.dumps({"chunk": "", "done": True, "full_response": full_response}) + "\n"
        
        # Save to session history
        if session_id:
            if session_id not in sessions:
                sessions[session_id] = []
                
            sessions[session_id].append({
                "user": message,
                "assistant": full_response
            })
            
            # Limit session history size
            if len(sessions[session_id]) > 10:
                sessions[session_id] = sessions[session_id][-10:]
        
    except Exception as e:
        logger.error(f"Error in streaming: {str(e)}")
        yield json.dumps({"chunk": f"Error: {str(e)}", "done": True}) + "\n"

# Define welcome messages for different chat types
def get_welcome_message(chat_type="GENERAL"):
    if chat_type == "CRISIS_SUPPORT":
        return "I understand you've selected crisis support. I'm here to help during difficult moments. While I'm not a replacement for professional help in emergencies, I can listen and provide support. How are you feeling right now, and how can I help you today?"
    elif chat_type == "THERAPY":
        return "Welcome to your therapy session space. I'm here to provide a supportive conversation using evidence-based approaches. Remember, I'm not a replacement for a licensed therapist but can help you explore thoughts and feelings. What brings you to therapy today?"
    else:
        return "Hello! I'm your mental health assistant. I'm here to provide general support and information about mental health topics. How can I help you today?"

@app.route('/chat', methods=['POST'])
def chat():
    try:
        data = request.get_json()
        logger.info(f"Received data: {data}")
        
        message = data.get('message', '')
        chat_type = data.get('chat_type', 'GENERAL')
        session_id = data.get('session_id', 'default-session')
        context = data.get('context', {})
        
        logger.info(f"Message: {message}, Type: {chat_type}, Session: {session_id}")
        
        # Handle empty messages as welcome message requests
        if not message or message.strip() == "":
            logger.info(f"Empty message, treating as welcome message request for {chat_type}")
            response = get_welcome_message(chat_type)
            return jsonify({"response": response})
        
        # Check if Ollama is available
        try:
            # Quick check if Ollama is running
            version_check = requests.get("http://localhost:11434/api/version", timeout=2)
            version_check.raise_for_status()
            logger.info(f"Ollama is available, version: {version_check.json().get('version')}")
            
            # Get response from Ollama
            response = get_ollama_response(message, chat_type, session_id)
            
            # Store conversation in session history
            if session_id not in sessions:
                sessions[session_id] = []
                
            sessions[session_id].append({
                "user": message,
                "assistant": response
            })
            
            # Limit session history size
            if len(sessions[session_id]) > 10:
                sessions[session_id] = sessions[session_id][-10:]
                
        except requests.exceptions.RequestException as e:
            logger.warning(f"Ollama is not available: {str(e)}")
            response = f"Hello! You said: '{message}'. I'm running in backup mode because the AI service is currently unavailable."
            
        return jsonify({"response": response})
        
    except Exception as e:
        logger.error(f"Error: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/chat/stream', methods=['POST'])
def chat_stream():
    try:
        data = request.get_json()
        logger.info(f"Received streaming request: {data}")
        
        message = data.get('message', '')
        chat_type = data.get('chat_type', 'GENERAL')
        session_id = data.get('session_id', 'default-session')
        
        logger.info(f"Streaming Message: {message}, Type: {chat_type}, Session: {session_id}")
        
        # Handle empty messages as welcome message requests
        if not message or message.strip() == "":
            logger.info(f"Empty message, treating as welcome message request for {chat_type} (streaming)")
            welcome = get_welcome_message(chat_type)
            
            # For welcome messages, we'll send a single chunk with done=true
            def welcome_generator():
                yield json.dumps({"chunk": welcome, "done": False}) + "\n"
                yield json.dumps({"chunk": "", "done": True, "full_response": welcome}) + "\n"
                
            return Response(stream_with_context(welcome_generator()), 
                          content_type='application/json')
        
        # Check if Ollama is available
        try:
            # Quick check if Ollama is running
            version_check = requests.get("http://localhost:11434/api/version", timeout=2)
            version_check.raise_for_status()
            logger.info(f"Ollama is available for streaming, version: {version_check.json().get('version')}")
            
            # Stream response from Ollama
            return Response(
                stream_with_context(stream_ollama_response(message, chat_type, session_id)),
                content_type='application/json'
            )
                
        except requests.exceptions.RequestException as e:
            logger.warning(f"Ollama is not available for streaming: {str(e)}")
            
            # Return a fallback response as a stream
            def fallback_generator():
                fallback = f"Hello! You said: '{message}'. I'm running in backup mode because the AI service is currently unavailable."
                yield json.dumps({"chunk": fallback, "done": False}) + "\n"
                yield json.dumps({"chunk": "", "done": True, "full_response": fallback}) + "\n"
                
            return Response(stream_with_context(fallback_generator()), 
                          content_type='application/json')
            
    except Exception as e:
        logger.error(f"Error in streaming: {str(e)}")
        
        # Return error as a stream
        def error_generator():
            error_msg = f"Error: {str(e)}"
            yield json.dumps({"chunk": error_msg, "done": True}) + "\n"
            
        return Response(stream_with_context(error_generator()), 
                      content_type='application/json')

@app.route('/health', methods=['GET'])
def health_check():
    logger.info("Health check called")
    
    # Check Ollama availability
    ollama_status = "unavailable"
    try:
        response = requests.get("http://localhost:11434/api/version", timeout=1)
        if response.status_code == 200:
            ollama_status = "available"
    except:
        pass
        
    return jsonify({
        "status": "ok", 
        "server": "direct_ollama",
        "ollama_status": ollama_status
    })

if __name__ == "__main__":
    logger.info("Starting direct Ollama server on port 5002...")
    app.run(host='0.0.0.0', port=5002, debug=True) 