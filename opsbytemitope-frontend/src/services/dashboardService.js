import api from './api';

export const dashboardService = {
  // Backend exposes a single GET /dashboard endpoint that returns everything.
  // We call it once and split the result into summary + activity so the rest
  // of the app keeps the same two-value destructuring it already uses.
  async getSummary() {
    const response = await api.get('/dashboard');
    return response.data.data;
  },

  async getActivity(params = {}) {
    // Activity is embedded in the dashboard response under recentActivity.
    const response = await api.get('/dashboard');
    const data = response.data.data;
    return data.recentActivity || [];
  },
};
