import axios from 'axios';
import { keycloak } from './keycloak.js';

const api = axios.create({ baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080' });

api.interceptors.request.use(async (cfg) => {
  try { await keycloak.updateToken(30); } catch { keycloak.login(); }
  cfg.headers.Authorization = `Bearer ${keycloak.token}`;
  return cfg;
});

const orNull = (e) => (e.response?.status === 404 ? null : Promise.reject(e));

export const getProfile = () => api.get('/api/users/me').then((r) => r.data).catch(orNull);
export const saveProfile = (p) => api.post('/api/users/profile', p).then((r) => r.data);
export const getActivities = () => api.get('/api/activities').then((r) => r.data);
export const addActivity = (a) => api.post('/api/activities', a).then((r) => r.data);
export const getRecommendation = (id) => api.get(`/api/recommendations/${id}`).then((r) => r.data).catch(orNull);
