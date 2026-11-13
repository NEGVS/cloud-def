from fastapi import FastAPI
from pydantic import BaseModel
from transformers import pipeline

app = FastAPI()
classifier = pipeline("zero-shot-classification", model="facebook/bart-large-mnli")

class TextRequest(BaseModel):
    text: str

@app.post("/intent")
def detect_intent(req: TextRequest):
    result = classifier(req.text, candidate_labels=["求职招聘", "闲聊"])
    intent = "RECRUITMENT" if result['labels'][0] == "求职招聘" else "GENERAL"
    return {"intent": intent, "score": result['scores'][0]}
