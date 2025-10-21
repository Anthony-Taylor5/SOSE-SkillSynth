To use the Ollama LLM you must download Ollama on your device
Then open command
if server isnt running run this command:
ollama serve

by default it will start a local HTTP API at:
http://127.0.0.1:11434

you will need to pull models but ensure you have enough space in memory for their sizes:
ollama pull mistral:latest
ollama pull mxbai-embed-large

# Install dependencies if not done
pip install fastapi uvicorn pinecone python-dotenv requests

start the api server with: 
if in ML folder
python -m uvicorn main_api:app --host localhost --port 8000
or if in root 
python -m uvicorn ml.main_api:app --host localhost --port 8000


if you need to access endpoints: 
  if youre running the api you can see full documentation at this link:
  http://localhost:8000/docs#/
