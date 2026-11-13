from fastapi import FastAPI
from pydantic import BaseModel
import hdbscan
import numpy as np
from typing import List

app = FastAPI()
clusterer = hdbscan.HDBSCAN(min_cluster_size=5)

class VectorRequest(BaseModel):
    vectors: List[List[float]]

@app.post("/cluster")
def cluster_vectors(req: VectorRequest):
    vectors = np.array(req.vectors)
    labels = clusterer.fit_predict(vectors)
    return {"cluster_ids": labels.tolist()}

@app.post("/predict")
def predict_cluster(req: VectorRequest):
    vectors = np.array(req.vectors)
    labels = clusterer.fit_predict(vectors)
    return {"cluster_ids": labels.tolist()}
