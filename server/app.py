from flask import Flask, request, jsonify
from flask_cors import CORS
import sys
import os

# Add ollama_rag directory to Python path
sys.path.append(os.path.join(os.path.dirname(__file__), '..'))
from ollama_rag.modelrag import after_rag_chain
import logging

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

@app.route('/chat', methods=['POST'])
def chat():
    try:
        logger.info("Received chat request")
        data = request.get_json()
        
        if not data:
            logger.error("No JSON data received")
            return jsonify({'error': 'No data provided'}), 400
            
        message = data.get('message')
        if not message:
            logger.error("No message in request")
            return jsonify({'error': 'Message is required'}), 400
            
        logger.info(f"Processing message: {message}")
        
        # Get response from Ollama RAG
        response = after_rag_chain.invoke(message)
        logger.info(f"Generated response: {response}")
        
        return jsonify({'response': response})
        
    except Exception as e:
        logger.error(f"Error processing request: {str(e)}", exc_info=True)
        return jsonify({'error': f"Server error: {str(e)}"}), 500

if __name__ == '__main__':
    logger.info("Starting Flask server...")
    app.run(host='0.0.0.0', port=5000, debug=True) 