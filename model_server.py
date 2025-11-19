import os
import logging
import re
from collections import deque
from flask import Flask, request, jsonify
from flask_cors import CORS
import httpx
from functools import lru_cache
from concurrent.futures import ThreadPoolExecutor

app = Flask(__name__)
CORS(app)

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Thread pool for handling requests
executor = ThreadPoolExecutor(max_workers=10)

class ContextManager:
    def __init__(self, window_size=6):
        self.window = deque(maxlen=window_size)
        self.theme_tracker = {}

    def update(self, exchange):
        self.window.append(exchange)
        self._track_themes(exchange)

    def _track_themes(self, exchange):
        themes = {
            'anxiety': r'\b(anxious|worried|panic|stress)\b',
            'depression': r'\b(sad|depressed|hopeless|down)\b',
            'relationships': r'\b(relationship|family|friend|partner)\b',
            'self_esteem': r'\b(worthless|confidence|self-esteem)\b',
            'trauma': r'\b(trauma|abuse|ptsd|flashback)\b'
        }
        text = exchange.lower()
        for theme, pattern in themes.items():
            if re.search(pattern, text, re.I):
                self.theme_tracker[theme] = self.theme_tracker.get(theme, 0) + 1

    def get_context(self):
        return {
            "recent_exchanges": list(self.window),
            "dominant_themes": sorted(
                self.theme_tracker.items(),
                key=lambda x: x[1],
                reverse=True
            )[:3]
        }

def detect_crisis(text):
    patterns = {
        'immediate': r'\b(suicide|kill myself)\b',
        'high': r'\b(hopeless|worthless)\b',
        'medium': r'\b(panic attack)\b'
    }
    for level, pat in patterns.items():
        if re.search(pat, text, re.IGNORECASE):
            return level
    return None

# Initialize context manager
context_manager = ContextManager()

async def get_ollama_response(message, chat_type=None):
    """
    Get response from Ollama API with context awareness
    """
    try:
        # Check for crisis
        crisis_level = detect_crisis(message)
        if crisis_level == 'immediate':
            return "I notice you're expressing thoughts of self-harm. Please reach out to a crisis helpline immediately: Call 14416 or 1800-599-0019. Your life matters and help is available 24/7."

        # Update context
        context_manager.update(message)
        context_info = context_manager.get_context()
        
        # Prepare the system message based on chat type and context
        system_message = "You are an empathetic AI assistant focused on mental health support. "
        
        # Add context awareness
        if context_info["dominant_themes"]:
            themes = [f"{theme} ({count})" for theme, count in context_info["dominant_themes"]]
            system_message += f"Current conversation themes: {', '.join(themes)}. "
        
        if chat_type:
            system_message += {
                "GENERAL": "Provide general assistance and support.",
                "CRISIS_SUPPORT": "Offer empathetic crisis support and guidance.",
                "THERAPY": "Act as a supportive therapy assistant.",
                "WELLNESS": "Focus on mental wellness and self-improvement strategies."
            }.get(chat_type, "Provide general assistance and support.")

        # Prepare the request payload
        payload = {
            "model": "mistral",
            "messages": [
                {"role": "system", "content": system_message},
                {"role": "user", "content": message}
            ],
            "stream": False
        }

        # Add recent context if available
        if context_info["recent_exchanges"]:
            for exchange in context_info["recent_exchanges"][-3:]:  # Last 3 exchanges
                payload["messages"].insert(-1, {
                    "role": "user" if len(payload["messages"]) % 2 == 0 else "assistant",
                    "content": exchange
                })

        async with httpx.AsyncClient(timeout=30.0) as client:
            response = await client.post("http://localhost:11434/api/chat", json=payload)
            response.raise_for_status()
            data = response.json()
            return data.get('message', {}).get('content', '')

    except httpx.TimeoutException:
        logger.error("Timeout while calling Ollama API")
        return "I apologize, but I'm taking too long to respond. Please try again."
    except Exception as e:
        logger.error(f"Error calling Ollama API: {str(e)}")
        return "I apologize, but I'm having trouble processing your request. Please try again."

@app.route('/chat', methods=['POST'])
async def chat():
    try:
        data = request.get_json()
        
        if not data:
            return jsonify({'error': 'No data provided'}), 400
            
        message = data.get('message', '')
        chat_type = data.get('chat_type', 'GENERAL')
        
        logger.info(f"Received message: {message}")
        
        # Get response from Ollama
        response = await get_ollama_response(message, chat_type)
        return jsonify({'response': response})
        
    except Exception as e:
        logger.error(f"Error in chat endpoint: {str(e)}")
        return jsonify({'error': 'Internal server error'}), 500

@app.route('/health', methods=['GET'])
def health_check():
    return jsonify({'status': 'healthy'})

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True, use_reloader=False) 