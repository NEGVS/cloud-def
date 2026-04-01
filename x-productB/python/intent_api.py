from flask import Flask, request, jsonify
from transformers import pipeline

app = Flask(__name__)
classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")

@app.route('/intent', methods=['POST'])
def detect_intent():
    text = request.json['text']
    result = classifier(text, candidate_labels=["求职", "闲聊"])
    intent = "job" if result['labels'][0] == "求职" else "chat"
    return jsonify({"intent": intent})

if __name__ == '__main__':
    app.run(port=5000)
