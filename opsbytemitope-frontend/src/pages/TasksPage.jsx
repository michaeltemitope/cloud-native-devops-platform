import { useState } from 'react';
import { useTasks } from '../hooks/useTasks';
import { useProjects } from '../hooks/useProjects';
import { taskService } from '../services/taskService';
import TaskTable from '../components/tasks/TaskTable';
import TaskForm from '../components/tasks/TaskForm';
import Modal from '../components/common/Modal';
import Button from '../components/common/Button';
import Spinner from '../components/common/Spinner';
import Alert from '../components/common/Alert';
import { getErrorMessage } from '../utils/helpers';
import styles from './TasksPage.module.css';

const STATUS_FILTERS = [
  { value: '', label: 'All' },
  { value: 'todo',        label: 'To Do' },
  { value: 'in_progress', label: 'In Progress' },
  { value: 'in_review',   label: 'In Review' },
  { value: 'done',        label: 'Done' },
  { value: 'blocked',     label: 'Blocked' },
];

export default function TasksPage() {
  const { tasks, loading, error, setTasks } = useTasks();
  const { projects } = useProjects();

  const [statusFilter, setStatusFilter] = useState('');
  const [search, setSearch] = useState('');
  const [showCreate, setShowCreate] = useState(false);
  const [editTask, setEditTask] = useState(null);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [actionError, setActionError] = useState('');
  const [deleteLoading, setDeleteLoading] = useState(false);

  const filtered = tasks.filter(t => {
    const matchStatus = !statusFilter || t.status === statusFilter;
    const matchSearch = !search || t.title.toLowerCase().includes(search.toLowerCase());
    return matchStatus && matchSearch;
  });

  async function handleCreate(formData) {
    const created = await taskService.create(formData);
    setTasks(prev => [created.task || created, ...prev]);
    setShowCreate(false);
  }

  async function handleEdit(formData) {
    const updated = await taskService.update(editTask.id, formData);
    setTasks(prev => prev.map(t => t.id === editTask.id ? (updated.task || updated) : t));
    setEditTask(null);
  }

  async function handleStatusChange(task, status) {
    try {
      await taskService.updateStatus(task.id, status);
      setTasks(prev => prev.map(t => t.id === task.id ? { ...t, status } : t));
    } catch (err) {
      setActionError(getErrorMessage(err));
    }
  }

  async function confirmDelete() {
    setDeleteLoading(true);
    try {
      await taskService.delete(deleteTarget.id);
      setTasks(prev => prev.filter(t => t.id !== deleteTarget.id));
      setDeleteTarget(null);
    } catch (err) {
      setActionError(getErrorMessage(err));
    } finally {
      setDeleteLoading(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h2 className={styles.pageTitle}>Tasks</h2>
          <p className={styles.pageSubtitle}>{filtered.length} of {tasks.length} tasks</p>
        </div>
        <Button variant="primary" onClick={() => setShowCreate(true)}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          New Task
        </Button>
      </div>

      {actionError && <Alert type="error" onDismiss={() => setActionError('')}>{actionError}</Alert>}
      {error && <Alert type="error">{error}</Alert>}

      {/* Filters */}
      <div className={styles.filters}>
        <input
          type="text"
          placeholder="Search tasks…"
          value={search}
          onChange={e => setSearch(e.target.value)}
          className={styles.searchInput}
        />
        <div className={styles.statusFilters}>
          {STATUS_FILTERS.map(f => (
            <button
              key={f.value}
              className={`${styles.filterBtn} ${statusFilter === f.value ? styles.active : ''}`}
              onClick={() => setStatusFilter(f.value)}
            >
              {f.label}
            </button>
          ))}
        </div>
      </div>

      {loading ? (
        <Spinner center size="lg" />
      ) : (
        <TaskTable
          tasks={filtered}
          onEdit={setEditTask}
          onDelete={setDeleteTarget}
          onStatusChange={handleStatusChange}
        />
      )}

      {/* Create Modal */}
      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="New Task">
        <TaskForm
          projects={projects}
          onSubmit={handleCreate}
          onCancel={() => setShowCreate(false)}
        />
      </Modal>

      {/* Edit Modal */}
      <Modal isOpen={!!editTask} onClose={() => setEditTask(null)} title="Edit Task">
        {editTask && (
          <TaskForm
            initialValues={editTask}
            projects={projects}
            onSubmit={handleEdit}
            onCancel={() => setEditTask(null)}
            submitLabel="Save Changes"
          />
        )}
      </Modal>

      {/* Delete Confirm */}
      <Modal isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} title="Delete Task" size="sm">
        <p className={styles.confirmText}>
          Delete <strong>{deleteTarget?.title}</strong>? This cannot be undone.
        </p>
        <div className={styles.confirmActions}>
          <Button variant="ghost" onClick={() => setDeleteTarget(null)} disabled={deleteLoading}>Cancel</Button>
          <Button variant="danger" onClick={confirmDelete} loading={deleteLoading}>Delete</Button>
        </div>
      </Modal>
    </div>
  );
}
