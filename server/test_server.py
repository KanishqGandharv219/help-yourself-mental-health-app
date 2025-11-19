from flask import Flask, request, jsonify
from flask_cors import CORS
import logging

# Configure logging
logging.basicConfig(level=logging.INFO, format='%(asctime)s - %(name)s - %(levelname)s - %(message)s')
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)  # Enable CORS for all routes

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
        logger.info(f"Received chat request: session={session_id}, type={chat_type}, message='{message}'")
        
        # Generate a simple response
        response = f"Hello! You said: '{message}'. This is a test response. Your chat type is {chat_type}."
        
        return jsonify({"response": response})
        
    except Exception as e:
        logger.error(f"Error in chat endpoint: {str(e)}")
        return jsonify({"error": str(e)}), 500

@app.route('/health', methods=['GET'])
def health_check():
    """Health check endpoint"""
    status = {
        "status": "ok",
        "server": "test_server"
    }
    return jsonify(status)

if __name__ == '__main__':
    logger.info("Starting test server on port 5001...")
    app.run(host='0.0.0.0', port=5001, debug=True) 