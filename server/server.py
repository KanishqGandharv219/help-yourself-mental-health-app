from flask import Flask, request, jsonify
from flask_cors import CORS
from pathlib import Path
import logging
import sys
import os
import traceback

# Import RAG components
from langchain_chroma import Chroma
from langchain_ollama import OllamaEmbeddings
from langchain_ollama import ChatOllama
from langchain_core.runnables import RunnablePassthrough
from langchain_core.output_parsers import StrOutputParser
from langchain_core.prompts import ChatPromptTemplate
from langchain.text_splitter import CharacterTextSplitter
from langchain_core.documents import Document

app = Flask(__name__)
CORS(app)
logging.basicConfig(
    level=logging.DEBUG,
    format='%(asctime)s - %(name)s - %(levelname)s - %(message)s'
)
logger = logging.getLogger(__name__)

# Dictionary to store loaded models
models = {}

# Initialize RAG components
try:
    logger.info("Initializing Ollama model...")
    # Initialize Ollama model
    model_local = ChatOllama(model="qwen2.5:latest")
    logger.info("Ollama model initialized successfully")
    
    logger.info("Creating test documents...")
    # Create test documents with mental health information
    docs = [
        Document(
            page_content="""Mental Health Helplines in India:
            1. AASRA - 24x7 Helpline: 91-9820466726
            2. Vandrevala Foundation: 1860-2662-345
            3. iCall: +91 22 2556 3291
            4. NIMHANS: 080-46110007
            
            These helplines provide 24/7 support for mental health issues including depression, anxiety, and crisis intervention.
            Trained counselors are available to help in multiple languages."""
        ),
        Document(
            page_content="""Mental Health Resources in India:
            1. The Live Love Laugh Foundation offers free counseling and mental health support
            2. NIMHANS provides comprehensive mental healthcare services
            3. Mpower Minds offers online counseling sessions
            4. WHO India supports various mental health initiatives
            
            If you're experiencing mental health issues, don't hesitate to reach out for help."""
        )
    ]
    
    text_splitter = CharacterTextSplitter.from_tiktoken_encoder(chunk_size=500, chunk_overlap=50)
    doc_splits = text_splitter.split_documents(docs)
    logger.info(f"Created {len(doc_splits)} document chunks")
    
    logger.info("Creating vector store...")
    # Create embeddings and store in vector DB
    vectorstore = Chroma.from_documents(
        documents=doc_splits,
        collection_name="mental-health-india",
        embedding=OllamaEmbeddings(model='nomic-embed-text'),
    )
    retriever = vectorstore.as_retriever()
    logger.info("Vector store created successfully")
    
    # Setup RAG prompt template
    rag_template = """You are a mental health support chatbot. Your purpose is to provide information about mental health resources and support services in India. 
    Use ONLY the following context to answer the question. If you don't find relevant information in the context, say "I can only provide information about mental health resources and support services in India."

    Context: {context}

    Question: {question}
    
    Answer:"""
    
    rag_prompt = ChatPromptTemplate.from_template(rag_template)
    
    logger.info("Creating RAG chain...")
    # Create RAG chain
    rag_chain = (
        {"context": retriever, "question": RunnablePassthrough()}
        | rag_prompt
        | model_local
        | StrOutputParser()
    )
    logger.info("RAG chain created successfully")
    
    # Define available models
    available_models = {
        "rag": {
            "name": "RAG-Enhanced Qwen2.5",
            "type": "rag"
        }
    }
    
    # Store RAG components
    models["rag"] = {
        "chain": rag_chain
    }
    logger.info("Models initialized successfully")
    
except Exception as e:
    logger.error(f"Error during initialization: {str(e)}")
    logger.error(f"Traceback: {traceback.format_exc()}")
    raise

@app.route('/', methods=['GET'])
def home():
    return jsonify({
        'status': 'server is running', 
        'endpoint': '/generate',
        'available_models': list(available_models.keys())
    })

@app.route('/models', methods=['GET'])
def get_models():
    return jsonify({
        'models': [
            {
                'id': model_id,
                'name': model_info['name']
            } for model_id, model_info in available_models.items()
        ]
    })

@app.route('/generate', methods=['POST'])
def generate():
    try:
        data = request.json
        if not data:
            return jsonify({'error': 'No input provided'}), 400
            
        prompt = data.get('prompt', '')
        model_id = data.get('model_id', 'rag')  # Default to RAG model
        
        logger.info(f"Received generate request - prompt: {prompt}, model_id: {model_id}")
        
        if not prompt:
            return jsonify({'error': 'Prompt is required'}), 400
        
        if model_id not in available_models:
            return jsonify({'error': f'Model {model_id} not available'}), 400
        
        # Handle RAG model
        if model_id == "rag":
            logger.info("Using RAG model for generation")
            try:
                response = models["rag"]["chain"].invoke(prompt)
                logger.info("RAG model generated response successfully")
                return jsonify({'response': response, 'model': model_id})
            except Exception as e:
                logger.error(f"Error during RAG generation: {str(e)}")
                logger.error(f"Traceback: {traceback.format_exc()}")
                return jsonify({'error': str(e)}), 500
        
        else:
            return jsonify({'error': f'Unknown model type for {model_id}'}), 500

    except Exception as e:
        logger.error(f"Error generating response: {str(e)}")
        logger.error(f"Traceback: {traceback.format_exc()}")
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)