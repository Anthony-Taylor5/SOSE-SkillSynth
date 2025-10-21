from fastapi import FastAPI, HTTPException, Body, Request
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
from typing import Dict, List, Optional, Any
from skill_generator import skill_generator
import time
import json

# Initialize API + Generator
app = FastAPI(title="SkillSynth ML API", version="1.0")
generator = skill_generator()

# CORS Middleware
app.add_middleware(
    CORSMiddleware,
    allow_origins=[
        "http://localhost:8080", #Backend API
        "http://localhost:5173" #replace with frontend API link
    ],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Logging Middleware
@app.middleware("http")
async def log_requests(request: Request, call_next):
    start_time = time.time()
    response = await call_next(request)
    process_time = (time.time() - start_time) * 1000
    print(f"{request.method} {request.url} completed in {process_time:.2f}ms")
    return response

# -------------------- Request Models --------------------
class RelevantSkillsRequest(BaseModel):
    main_skill: str
    top_k: Optional[int] = 3

class ProjectRequest(BaseModel):
    main_skills: List[str]
    time_availability: int
    experience_level: int

class User(BaseModel):
    id: str
    skills: Dict[str, int]
    time_availability: int

class UploadUsersRequest(BaseModel):
    users: List[User]

class FindTeammatesRequest(BaseModel):
    user: User
    top_k: Optional[int] = 15

# -------------------- Response Models --------------------
class ProcessSkillsResponse(BaseModel):
    status: str
    processed_skills: List[str]
    uploaded_count: int

class RelevantSkillItem(BaseModel):
    skill: str
    score: float
    category: str
    description: str

class GrabRelevantSkillsResponse(BaseModel):
    main_skill: str
    relevant_skills: List[RelevantSkillItem]

class ProjectResponse(BaseModel):
    project_name: str
    description: str
    experience_level: int
    time_availability: int
    learning_resources: List[str]
    relevant_skills: List[str]

class GetProjectResponse(BaseModel):
    project: ProjectResponse

class UploadUsersResponse(BaseModel):
    status: str
    users_uploaded: int

class TeammateItem(BaseModel):
    user_id: str
    match_score: float

class FindTeammatesResponse(BaseModel):
    user_id: str
    teammates: List[TeammateItem]

# -------------------- Routes --------------------
@app.get("/")
def root():
    return {"message": "SkillSynth ML API is running"}

@app.post(
    "/process_and_upload_skills",
    response_model=ProcessSkillsResponse,
    summary="Process and upload new skills"
)
def process_and_upload_skills(skills: Dict[str, List[str]] = Body(
        ...,
        example={
            "Databases": ["PostgreSQL", "MySQL", "SQLite"],
            "DevOps & Cloud": ["Docker", "Kubernetes", "AWS"],
            "Cybersecurity": ["Threat Modeling", "Vuln Scanning", "OWASP Top 10"]
        }
    )
):
    try:
        embeddings = generator.process_skills(skills)
        generator.upload_skills(embeddings)
        return {
            "status": "success",
            "processed_skills": list(embeddings.keys()),
            "uploaded_count": len(embeddings)
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post(
    "/grab_relevant_skills",
    response_model=GrabRelevantSkillsResponse,
    summary="Get top relevant skills for a given skill"
)
def grab_relevant_skills(req: RelevantSkillsRequest):
    try:
        relevant = generator.grab_relevant_skills(req.main_skill, req.top_k)
        return {"main_skill": req.main_skill, "relevant_skills": relevant}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post(
    "/get_project",
    response_model=GetProjectResponse,
    summary="Generate an AI project based on skills and experience"
)
def get_project(req: ProjectRequest):
    try:
        project = generator.get_project(req.main_skills, req.time_availability, req.experience_level)

       
        if isinstance(project, str):
            project = json.loads(project)

        return {"project": project}

    except Exception as e:
        print(f"Error in get_project: {e}")
        import traceback; traceback.print_exc()
        raise HTTPException(status_code=500, detail=str(e))

@app.post(
    "/upload_users",
    response_model=UploadUsersResponse,
    summary="Upload user profiles"
)
def upload_users(req: UploadUsersRequest):
    try:
        generator.upload_users([user.dict() for user in req.users])
        return {"status": "success", "users_uploaded": len(req.users)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))

@app.post(
    "/find_teammates",
    response_model=FindTeammatesResponse,
    summary="Find matching teammates for a user"
)
def find_teammates(req: FindTeammatesRequest):
    try:
        teammates = generator.find_teammates(req.user.dict(), req.top_k)
        return {"user_id": req.user.id, "teammates": teammates}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
