import requests
import json
from pinecone import Pinecone, ServerlessSpec
from dotenv import load_dotenv
import os

load_dotenv()  # loads variables from .env

pinecone_api_key = os.getenv("PINECONE_API_KEY")

if not pinecone_api_key:
    raise ValueError("Pinecone API key not found. Please set PINECONE_API_KEY in .env")

print("Pinecone API key loaded successfully")


    

class skill_generator:
    def __init__ (self):
        self.generate_url = "http://localhost:11434/api/generate"
        self.embed_url = "http://localhost:11434/api/embed"
        self.pc = Pinecone(api_key= pinecone_api_key)
        


    #create pinecone skills index:
    #run once code:
    def create_skills_db(self):
        index_name = "skills-index"
        if index_name not in self.pc.list_indexes().names():
            self.pc.create_index(
                name=index_name,
                dimension=1024,  # vector size
                metric="cosine",  # or 'dotproduct', 'euclidean'
                spec=ServerlessSpec( cloud="aws", region="us-east-1")
            )

    def create_users_db(self):
        index_name = "users-index"
        if index_name not in self.pc.list_indexes().names():
            self.pc.create_index(
                name=index_name,
                dimension=1024,  # vector size
                metric="cosine",  # or 'dotproduct', 'euclidean'
                spec=ServerlessSpec( cloud="aws", region="us-east-1")
            )

    #anytime we add more skills run this
    def upload_skills(self, embeddings_dict, index_name="skills-index"):
        
        index = self.pc.Index(index_name)

        vectors = [
            {
                "id": skill,
                "values": embedding[0],
                "metadata": {"source": "ollama", "category":embedding[1], "description": embedding[2]}
            }
            for skill, embedding in embeddings_dict.items()
        ]

        index.upsert(vectors=vectors)
        print(f"Uploaded {len(vectors)} skill embeddings to Pinecone index '{index_name}'")

    def upload_users(self, users, index_name="users-index"):
        
        embeddings = {}
        for user in users:
            user_id = user.get("id")
            user_time_availability = user.get("time_availability")
            user_skills = user.get("skills")
            get_embed = {
            "model": "mxbai-embed-large"
            }

            #embed
            skill_descriptions = ", ".join([f"{skill} ({level}/5)" for skill, level in user_skills.items()])
            user_profile_text = (
                f"User profile: skills include {skill_descriptions}. "
                f"Experience levels are on a 1-5 scale. "
                f"Available time: {user_time_availability} hours per week. "
                f"Available time is on a 1-20 scale."
            )
            get_embed["input"] = [user_profile_text]
            res = requests.post(self.embed_url, json=get_embed)
            
            #if embed worked
            if res.status_code == 200:
                embed_data = res.json()
                embedding = embed_data.get("embeddings", [[]])[0]
                embeddings[user_id] = [embedding, user_time_availability, user_skills]
            else:
                print(f"Embedding  user: {user_id} did not work")
                

        index = self.pc.Index(index_name)

        vectors = [
            {
                "id": id,
                "values": vector,
                "metadata": {"source": "ollama", "time_availability": time_availability, "skills": json.dumps(skills)}
            }
            for id, (vector, time_availability, skills) in embeddings.items()

        ]

        index.upsert(vectors=vectors)
        print(f"Uploaded {len(vectors)} user embeddings to Pinecone index '{index_name}'")

    
    def grab_relevant_skills(self, main_skill, top_k=3, index_name="skills-index"):
        index = self.pc.Index(index_name)

        # Fetch the vector of the given skill ID from the index
        fetch_result = index.fetch(ids=[main_skill])

        if main_skill not in fetch_result.vectors:
            print(f"Skill '{main_skill}' not found in Pinecone index.")
            return []

        # Extract the stored vector
        main_vector = fetch_result.vectors[main_skill].values

        # Query Pinecone using the existing vector
        query_result = index.query(
            vector=main_vector,
            top_k=top_k + 1,  # +1 because the queried skill itself will appear as top match
            include_metadata=True
        )

        # Filter out the skill itself from results
        relevant_skills = []
        for match in query_result.matches:
            if match.id != main_skill:
                relevant_skills.append({
                    "skill": match.id,
                    "score": match.score,
                    "category": match.metadata.get("category", "Unknown"),
                    "description": match.metadata.get("description", "")
                })

        # Display results
        print(f"\nTop {top_k} similar skills to '{main_skill}':")
        for s in relevant_skills[:top_k]:
            print(f"• {s['skill']} ({s['category']}) — score: {s['score']:.3f}")

        return relevant_skills[:top_k]
    
    def get_project(self, main_skills=[], time_availability=4, experience_level=1):
        #def check_project_in_database(skill):
        #     #get api request - get projects with that skill, get if user has completed
        #     for project in list:
            #     if not user_complete:
            #         return project
            #   
            # return False
        #if check_project_in_database(skill):
        #       return project 
        #else:
        #get main_skills, skill experience level, time availability 
        make_project = {
            "model": "mistral:latest",
            "stream": False
        }
        relevant_skills = []
        for skill in main_skills:
            try:
                skill_results = self.grab_relevant_skills(skill)
                relevant_skills.append(skill_results)
                print(f"Found {len(skill_results)} relevant skills for {skill}")
            except Exception as e:
                print(f"Warning: Could not get relevant skills for {skill}: {e}")
                relevant_skills.append([])  # Add empty list for missing skills
        
        relevant_skills_names = set()
        for arr in relevant_skills:
            for skill in arr:
                relevant_skills_names.add(skill.get("skill"))
        relevant_skills_names = list(relevant_skills_names)
        print(f"All relevant skills: {relevant_skills_names}")
        
        make_project["prompt"] = f"""
                You are an expert computer science educator and project mentor. 
                You help students and developers design personalized learning projects that fit their skills, experience level, and available time.

                Your goal is to generate a logical, realistic, and CS-oriented project idea that helps the user grow in their chosen skill(s). 
                The project should be appropriate for their time availability and experience level.

                EXPERIENCE LEVEL SCALE (1-5):
                1 = Complete beginner (no prior experience)
                2 = Novice (some exposure, needs structured guidance)
                3 = Intermediate (comfortable with basics, ready for applied work)
                4 = Advanced (proficient, ready for complex systems or optimization)
                5 = Expert (very strong, can handle research-level or production projects)

                TIME AVAILABILITY SCALE (1-20 hours per week):
                1-5 hrs = small project (quick prototype, single concept)
                6-12 hrs = moderate project (multi-step, moderate depth)
                13-20 hrs = complex project (full app, multiple components, deeper exploration)

                INPUTS:
                - Skill(s): {main_skills}
                - Experience Level (1-5): {experience_level}
                - Time Availability (1-20 hrs/week): {time_availability}

                OUTPUT FORMAT (return in structured JSON-like form):
                {{
                "project_name": "string",
                "description": "string — a concise but detailed explanation of the project, what it does, and how it teaches the skill(s)",
                "experience_level": {experience_level}
                "time_availability": {time_availability}
                "learning_resources": [
                    "List of 3-5 recommended tutorials, documentation links, helpful articles, or free resources relevant to the skill(s)"
                ]
                "relevant skills": {relevant_skills_names}
                }}

                GUIDELINES:
                - Scale the project complexity to match the user's experience level (1-5) and available time (1-20 hrs/week).
                - Ensure the project is clearly computer-science-oriented (software, AI, data science, web dev, systems, cybersecurity, etc.).
                - Make the project achievable but still challenging — something that will push the user's skills.
                - The project name should sound original, creative, and domain-appropriate.
                - Favor official documentation, reputable tutorials, or open-source resources.
                """


        res = requests.post(self.generate_url, json=make_project)
        data = res.json()
        print(data.get("response"))
        return data.get("response")



    def find_teammates(self, user_object, top_k=15, index_name="users-index"):
        user_id = user_object.get("id")
        index = self.pc.Index(index_name)

        # Fetch the vector of the given skill ID from the index
        fetch_result = index.fetch(ids=[user_id])

        if user_id not in fetch_result.vectors:
            print(f"User: '{user_id}' not found in Pinecone index.")
            return []

        # Extract the stored vector
        main_vector = fetch_result.vectors[user_id].values

        # Query Pinecone using the existing vector
        query_result = index.query(
            vector=main_vector,
            top_k=top_k + 1,  # +1 because the queried skill itself will appear as top match
            include_metadata=True
        )

        # Filter out the skill itself from results
        suggested_users = []
        for match in query_result.matches:
            if match.id != user_id:
                suggested_users.append({
                    "user_id": match.id,
                    "match_score": match.score,
                })

        # Display results
        print(f"\nTop {top_k} similar skills to '{user_id}':")
        for s in suggested_users[:top_k]:
            print(f"• {s['user_id']} — match score: {s['match_score']:.3f}")

        return suggested_users[:top_k]





    
    #anytime we add more skills run this (data must be in dicrtionary form)
    def process_skills(self, data):
        
        get_description = {
            "model": "mistral:latest",
            "stream": False
        }

        get_embed = {
        "model": "mxbai-embed-large"
        }


        #we'll gather a default amount of skills
        errors = {}
        embeddings = {}

        for category, skills in data.items():
            for skill in skills: 
                #get description
                get_description["prompt"] = f"Summarize {skill} in two concise sentences."
                res = requests.post(self.generate_url, json=get_description)

                # check if description request succeeded
                if res.status_code == 200:
                    description_data = res.json()
                    description = description_data.get("response", "")
                
                    #embed
                    get_embed["input"] = [f"{skill}: {description}"]
                    res3 = requests.post(self.embed_url, json=get_embed)
                    
                    #if embed worked
                    if res3.status_code == 200:

                        embed_data = res3.json()
                        embedding = embed_data.get("embeddings", [[]])[0]

                        embeddings[skill] = (embedding, category, description)
                        print(f"{skill}\n{description}\nEmbedding length: {len(embedding)}")
                
                    #save errors
                    else: 
                        print(f"Issue with embedding: {skill}, {res3.status_code}")
                        errors[skill] = f"embed: {res3.status_code}"
                else: 
                        print(f"Issue with description: {skill}, {res.status_code}")
                        errors[skill] = f"description: {res.status_code}"
               
        print(f"Length of errors: {len(errors)}")
        print(f"Length of embeddings: {len(embeddings)}")
        return embeddings 


def main():
    new_skill_generator = skill_generator()
    # with open("skills_base.txt", "r", encoding="utf-8") as file:
    #     skills_str = file.read()

    # #load as json
    # data = json.loads(skills_str)
    
    # test_skills = ["Python", "Machine Learning", "Data Science", "hahdujfn", "under water basket weaving"]
    # embeddings = new_skill_generator.process_skills(data)
    # dimension = len(next(iter(embeddings.values()))[0])
    # new_skill_generator.create_skills_db()
    # new_skill_generator.upload_skills(embeddings)
    # # new_skill_generator.grab_relevant_skills("PyTorch")
    # new_skill_generator.get_project(["PyTorch", "TensorFlow", "NumPy"])
    # new_skill_generator.get_project("PyTorch", 16, 4)
    # new_skill_generator.get_project("PyTorch", 8, 5)
    # new_skill_generator.get_project("PyTorch", 8, 2)
    # new_skill_generator.get_project("PyTorch", 3, 3)
   
    # new_skill_generator.create_users_db()
    # users = [{
    #       "id": "u423",
    #       "skills": {
    #         "C++": 2,
    #         "C": 1,
    #         "Java": 2
    #       },
    #       "time_availability": 10
    #     }, 
    #     {
    #       "id": "u567",
    #       "skills": {
    #         "Python": 1,
    #         "Machine Learning": 1,
    #         "React": 5
    #       },
    #       "time_availability": 1
    #     }]
    # new_skill_generator.upload_users(users)
    # user = {
    #       "id": "u123",
    #       "skills": {
    #         "Python": 4,
    #         "Machine Learning": 3,
    #         "React": 2
    #       },
    #       "time_availability": 10
    #     }
    # new_skill_generator.find_teammates(user,2)


    

if __name__ == "__main__":
    main()

# group matchmaking:
# creator side:
#     create a match score based off of experience levels
#     they should get random users who have the closest percent same level as requested in project
#     Lets say grab first 15 users
# inputs: skills, levels of the skills, 



#this version processes a list of new skills not in the db
# def process_skills(self, skills):
#     # def check_in_database(skill):
#     #     #get api request
#     #     if response == skill:
#     #         return True
#     #     else:
#     #         return False
    
#     get_description = {
#         "model": "mistral:latest",
#         "stream": False
#     }

#     check_applicability = {
#         "model": "mistral:latest",
#         "stream": False
#     }

#     get_embed = {
#     "model": "mxbai-embed-large"
#     }


#     #we'll gather a default amount of skills
#     descriptions = {}
#     errors = {}
#     embeddings = {}

#     for skill in skills:
#         #check that the skill is in the database
#         #if check_in_database(skill):
#             #continue   

#         # get description for the skill
#         get_description["prompt"] = f"Summarize {skill} in two concise sentences."
#         res = requests.post(self.generate_url, json=get_description)

#         # check if description request succeeded
#         if res.status_code == 200:
#             data = res.json()
#             description = data.get("response", "")
    
#             #after gathering description check the skill is part of CS
#             check_applicability["prompt"] = (
#                 f"Verify if this concept belongs to the field of Computer Science:\n\n"
#                 f"{description}\n\n"
#                 "If you are unsure respond with false. Respond only with lowercase true or false."

#             )
#             res2 = requests.post(self.generate_url, json=check_applicability)
        
#             #check if applicability request succeeded
#             if res2.status_code == 200:
#                 data2 = res2.json()
#                 boolean_response = data2.get("response", "").strip().lower()
#                 if not boolean_response or boolean_response == "false":
#                     continue
#                 else:
#                     #if valid skill add the description and embed
#                     descriptions[skill] = description
                
#                     #embed
#                     get_embed["input"] = [f"{skill}: {description}"]
#                     res3 = requests.post(self.embed_url, json=get_embed)
                
#                     #if embed worked
#                     if res3.status_code == 200:

#                         data3 = res3.json()
#                         embedding = data3.get("embeddings", [[]])[0]

#                         embeddings[skill] = embedding
#                         print(f"{skill}\n{description}\nEmbedding length: {len(embedding)}")
            
#                     #save errors
#                     else: 
#                         print(f"Issue with embedding: {skill}, {res3.status_code}")
#                         errors[skill] = f"embed: {res3.status_code}"
    
#             else: 
#                 print(f"Issue with applicability: {skill}, {res2.status_code}")
#                 errors[skill] =  f"applicability: {res2.status_code}"

#         else:
#             print(f"Error getting description for {skill}: {res.status_code}")
#             errors[skill] =f"description: {res.status_code}"

#     print(f"Length of errors: {len(errors)}")
#     print(f"Length of embeddings: {len(embeddings)}")
#     return embeddings #skills, descriptions, embeddings
