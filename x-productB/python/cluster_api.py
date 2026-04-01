from flask import Flask, request, jsonify
import hdbscan
import numpy as np
import pickle

app = Flask(__name__)

# 预训练的聚类模型
with open('hdbscan_model.pkl', 'rb') as f:
    clusterer = pickle.load(f)

with open('embedding_model.pkl', 'rb') as f:
    embed_model = pickle.load(f)

@app.route('/cluster', methods=['POST'])
def get_cluster():
    text = request.json['text']
    embedding = embed_model.encode([text])[0]
    category_id = clusterer.predict([embedding])[0]
    return jsonify({"category_id": int(category_id)})

if __name__ == '__main__':
    app.run(port=5000)
