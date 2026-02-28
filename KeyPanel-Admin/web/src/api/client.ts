import axios from 'axios';

const api = axios.create({
  baseURL: (globalThis as any).__API_URL__ || 'http://localhost:8080'
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('panel_jwt');
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

export default api;
