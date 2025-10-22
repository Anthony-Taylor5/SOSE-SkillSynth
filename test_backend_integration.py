import requests
import json
import time

BACKEND_URL = "http://localhost:8080/api"
ML_URL = "http://localhost:8000"

def pretty_print(title, data):
    print(f"\n=== {title} ===")
    print(json.dumps(data, indent=2))

def wait_for_service(url, timeout=30):
    for _ in range(timeout):
        try:
            res = requests.get(url)
            if res.status_code == 200:
                print(f"✅ {url} is up!")
                return
        except requests.exceptions.RequestException:
            pass
        time.sleep(1)
    raise RuntimeError(f"❌ {url} did not start in time!")

def test_backend_integration():
    # Step 0: Health checks
    print("🔍 Checking services...")
    wait_for_service(f"{ML_URL}/")
    wait_for_service(f"{BACKEND_URL}/users")

    # Step 1: Create a skill
    print("\n🧠 Creating a skill...")
    skill_payload = {
        "name": "Python",
        "category": "Programming",
        "level": 3
    }
    skill_res = requests.post(f"{BACKEND_URL}/skills", json=skill_payload)
    assert skill_res.status_code == 200, "Failed to create skill"
    created_skill = skill_res.json()
    pretty_print("Skill Created", created_skill)

    # Step 2: Create a user
    print("\n👤 Creating a user...")
    user_payload = {
        "username": "johndoe",
        "level": 5,
        "allSkills": [created_skill]
    }
    user_res = requests.post(f"{BACKEND_URL}/users", json=user_payload)
    print(f"User creation status: {user_res.status_code}")
    print(user_res.text)
    assert user_res.status_code == 200, "Failed to create user"

    created_user = user_res.json()
    pretty_print("User Created", created_user)

    # Step 3: Update user
    print("\n✏️ Updating user level...")
    created_user["level"] = 6
    update_res = requests.put(f"{BACKEND_URL}/users", json=created_user)
    assert update_res.status_code == 200, "Failed to update user"
    pretty_print("User Updated", update_res.json())

    # Step 4: Generate an AI project
    print("\n🤖 Generating AI project...")
    project_payload = {
        "skills": [
            {"skillName": "Python", "category": "Programming"}
        ],
        "time_availability": 10,
        "experience_level": 3
    }
    project_res = requests.post(f"{BACKEND_URL}/projects/ai-generate", json=project_payload)
    print(f"AI project status: {project_res.status_code}")
    print(project_res.text)
    assert project_res.status_code == 200, "Failed to generate AI project"

   
    ai_project = project_res.json()
    pretty_print("AI Project Generated", ai_project)

    # Step 5: Verify data retrieval
    print("\n📦 Fetching all users, skills, projects...")
    users = requests.get(f"{BACKEND_URL}/users").json()
    skills = requests.get(f"{BACKEND_URL}/skills").json()
    projects = requests.get(f"{BACKEND_URL}/projects").json()

    pretty_print("All Users", users)
    pretty_print("All Skills", skills)
    pretty_print("All Projects", projects)

    print("\n✅ Integration Test Completed Successfully!")

if __name__ == "__main__":
    test_backend_integration()
