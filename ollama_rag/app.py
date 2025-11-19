from flask import Flask, request, jsonify
from flask_cors import CORS
import logging
from simple_rag import TherapistBot

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = Flask(__name__)
CORS(app)

# Initialize the bot
bot = TherapistBot()

@app.route('/chat', methods=['POST'])
def chat():
    try:
        data = request.get_json()
        user_message = data.get('message', '')
        
        if not user_message:
            return jsonify({'error': 'No message provided'}), 400

        logger.info(f"Received message: {user_message}")
        
        # Process the message using our enhanced bot
        response = bot.chat(user_message)
        
        logger.info(f"Generated response: {response}")
        return jsonify({'response': response})

    except Exception as e:
        logger.error(f"Error processing request: {str(e)}", exc_info=True)
        return jsonify({'error': str(e)}), 500

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000) 