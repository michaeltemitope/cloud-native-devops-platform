import api from './api';

export const taskService = {
  async getAll(params = {}) {
    const response = await api.get('/tasks', { params });
    return response.data.data;
  },

  async getByProject(projectId, params = {}) {
    const response = await api.get(`/projects/${projectId}/tasks`, { params });
    return response.data.data;
  },

  async getById(id) {
    const response = await api.get(`/tasks/${id}`);
    return response.data.data;
  },

  async create(data) {
    const response = await api.post('/tasks', data);
    return response.data.data;
  },

  async update(id, data) {
    const response = await api.put(`/tasks/${id}`, data);
    return response.data.data;
  },

  async updateStatus(id, status) {
    const response = await api.patch(`/tasks/${id}/status`, { status });
    return response.data.data;
  },

  async delete(id) {
    const response = await api.delete(`/tasks/${id}`);
    return response.data.data;
  },
};
