import api from './api';

export const authService = {
  async login(email, password) {
    // Backend wraps response in { success, data: { accessToken, user, ... } }
    const response = await api.post('/auth/login', { email, password });
    return response.data.data;
  },

  async register(firstName, lastName, email, password) {
    const response = await api.post('/auth/register', { firstName, lastName, email, password });
    return response.data.data;
  },

  async getMe() {
    const response = await api.get('/users/me');
    return response.data.data;
  },

  async updateProfile(data) {
    const response = await api.put('/users/me', data);
    return response.data.data;
  },

  async changePassword(currentPassword, newPassword) {
    const response = await api.put('/users/me/password', { currentPassword, newPassword });
    return response.data.data;
  },
};
