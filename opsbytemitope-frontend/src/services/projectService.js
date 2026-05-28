import api from './api';

export const projectService = {
  async getAll(params = {}) {
    const response = await api.get('/projects', { params });
    return response.data.data;
  },

  async getById(id) {
    const response = await api.get(`/projects/${id}`);
    return response.data.data;
  },

  async create(data) {
    const response = await api.post('/projects', data);
    return response.data.data;
  },

  async update(id, data) {
    const response = await api.put(`/projects/${id}`, data);
    return response.data.data;
  },

  async delete(id) {
    const response = await api.delete(`/projects/${id}`);
    return response.data.data;
  },
};
