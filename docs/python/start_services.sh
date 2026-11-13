#!/bin/bash
uvicorn intent_service:app --port 8000 &
uvicorn cluster_service:app --port 8001 &
echo "Python services started on ports 8000 and 8001"
