import axios from "axios";

const API_URL = "http://localhost:8080/api";

// Users
export const getUsers = () => axios.get(`${API_URL}/users`);
export const getUserById = (id: number) => axios.get(`${API_URL}/users/${id}`);
export const createUser = (user: { username: string; level: number }) =>
  axios.post(`${API_URL}/users`, user);
export const updateUser = (user: { id: number; username: string; level: number }) =>
  axios.put(`${API_URL}/users`, user);
export const deleteUser = (id: number) => axios.delete(`${API_URL}/users/${id}`);

// Skills
export const getSkills = () => axios.get(`${API_URL}/skills`);
export const getSkillById = (id: number) => axios.get(`${API_URL}/skills/${id}`);
export const createSkill = (skill: { skillName: string; description: string }) =>
  axios.post(`${API_URL}/skills`, skill);
export const updateSkill = (skill: { id: number; skillName: string; description: string }) =>
  axios.put(`${API_URL}/skills`, skill);
export const deleteSkill = (id: number) => axios.delete(`${API_URL}/skills/${id}`);

// Projects
export const getProjects = () => axios.get(`${API_URL}/projects`);
export const getProjectById = (id: number) => axios.get(`${API_URL}/projects/${id}`);
export const createProject = (project: {
  name: string;
  recommendedSkills: string[];
  dateRange: string;
  projectDescription: string;
  experienceLevel: number;
}) => axios.post(`${API_URL}/projects`, project);
export const updateProject = (project: {
  id: number;
  name: string;
  recommendedSkills: string[];
  dateRange: string;
  projectDescription: string;
  experienceLevel: number;
}) => axios.put(`${API_URL}/projects`, project);
export const deleteProject = (id: number) => axios.delete(`${API_URL}/projects/${id}`);
