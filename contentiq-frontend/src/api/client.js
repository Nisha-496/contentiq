import axios from 'axios';

const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8081';

export const api = axios.create({
  baseURL: API_BASE,
  timeout: 60000,
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem('contentiq_token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    const status = error.response?.status;
    if (status === 401 || status === 403) {
      const onAuthPage = window.location.pathname.startsWith('/login');
      if (!onAuthPage) {
        localStorage.removeItem('contentiq_token');
        localStorage.removeItem('contentiq_user');
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export const auth = {
  login: (username, password) =>
    api.post('/api/auth/login', { username, password }).then((r) => r.data),
  register: (username, email, password) =>
    api.post('/api/auth/register', { username, email, password }).then((r) => r.data),
};

export const videos = {
  importFromYouTube: (urlOrId, transcript) =>
    api.post('/api/youtube/videos/import', { urlOrId, transcript }).then((r) => r.data),
  list: () => api.get('/api/videos').then((r) => r.data),
  get: (id) => api.get(`/api/videos/${id}`).then((r) => r.data),
};

export const comments = {
  importFromYouTube: (videoId, maxResults = 100) =>
    api
      .post(`/api/youtube/videos/${videoId}/comments/import`, null, { params: { maxResults } })
      .then((r) => r.data),
  forVideo: (videoId) => api.get(`/api/comments/${videoId}`).then((r) => r.data),
};

export const analysis = {
  analyzeComments: (videoId) =>
    api.post(`/api/analysis/comments/${videoId}`).then((r) => r.data),
  getReport: (reportId) =>
    api.get(`/api/analysis/report/${reportId}`).then((r) => r.data),
  reportsForVideo: (videoId) =>
    api.get(`/api/analysis/video/${videoId}/reports`).then((r) => r.data),
};
