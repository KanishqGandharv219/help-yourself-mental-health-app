from flask import Flask, jsonify
import logging

logging.basicConfig(level=logging.DEBUG)
logger = logging.getLogger(__name__)

app = Flask(__name__)

@app.route('/health', methods=['GET'])
def health():
    logger.info("Health check called")
    return jsonify({"status": "ok", "server": "simple_test"})

if __name__ == "__main__":
    logger.info("Starting simple test server...")
    app.run(host='0.0.0.0', port=5002, debug=True) 