import { useState, useEffect, useCallback } from 'react';
import { taskService } from '../services/taskService';

export function useTasks(projectId = null) {
  const [tasks, setTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchTasks = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const data = projectId
        ? await taskService.getByProject(projectId)
        : await taskService.getAll();
      setTasks(data.tasks || data);
    } catch (err) {
      setError(err.response?.data?.message || 'Failed to load tasks');
    } finally {
      setLoading(false);
    }
  }, [projectId]);

  useEffect(() => {
    fetchTasks();
  }, [fetchTasks]);

  return { tasks, loading, error, refetch: fetchTasks, setTasks };
}
