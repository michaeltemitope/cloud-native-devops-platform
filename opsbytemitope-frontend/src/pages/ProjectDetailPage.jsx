import { useState, useEffect } from 'react';
import { useParams, useNavigate, Link } from 'react-router-dom';
import { projectService } from '../services/projectService';
import { taskService } from '../services/taskService';
import { useTasks } from '../hooks/useTasks';
import TaskTable from '../components/tasks/TaskTable';
import TaskForm from '../components/tasks/TaskForm';
import ProjectForm from '../components/projects/ProjectForm';
import Modal from '../components/common/Modal';
import Button from '../components/common/Button';
import Badge from '../components/common/Badge';
import Card from '../components/common/Card';
import Spinner from '../components/common/Spinner';
import Alert from '../components/common/Alert';
import { STATUS_CONFIG, formatDate, getErrorMessage } from '../utils/helpers';
import styles from './ProjectDetailPage.module.css';

export default function ProjectDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();

  const [project,      setProject]      = useState(null);
  const [projLoading,  setProjLoading]  = useState(true);
  const [projError,    setProjError]    = useState('');
  const [showEdit,     setShowEdit]     = useState(false);
  const [showDelete,   setShowDelete]   = useState(false);
  const [showNewTask,  setShowNewTask]  = useState(false);
  const [editTask,     setEditTask]     = useState(null);
  const [actionError,  setActionError]  = useState('');

  const { tasks, loading: tasksLoading, refetch: refetchTasks, setTasks } = useTasks(id);

  useEffect(() => {
    projectService.getById(id)
      .then(data => setProject(data.project || data))
      .catch(err => setProjError(getErrorMessage(err)))
      .finally(() => setProjLoading(false));
  }, [id]);

  async function handleEditProject(formData) {
    const updated = await projectService.update(id, formData);
    setProject(updated.project || updated);
    setShowEdit(false);
  }

  async function handleDeleteProject() {
    try {
      await projectService.delete(id);
      navigate('/projects');
    } catch (err) {
      setActionError(getErrorMessage(err));
      setShowDelete(false);
    }
  }

  async function handleCreateTask(formData) {
    const created = await taskService.create({ ...formData, projectId: id });
    setTasks(prev => [created.task || created, ...prev]);
    setShowNewTask(false);
  }

  async function handleEditTask(formData) {
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

  async function handleDeleteTask(task) {
    try {
      await taskService.delete(task.id);
      setTasks(prev => prev.filter(t => t.id !== task.id));
    } catch (err) {
      setActionError(getErrorMessage(err));
    }
  }

  if (projLoading) return <Spinner center size="lg" />;
  if (projError)   return <Alert type="error">{projError}</Alert>;
  if (!project)    return null;

  const statusCfg = STATUS_CONFIG[project.status] || STATUS_CONFIG.active;

  return (
    <div className={styles.page}>
      {actionError && <Alert type="error" onDismiss={() => setActionError('')}>{actionError}</Alert>}

      {/* Project Header */}
      <Card>
        <div className={styles.projHeader}>
          <div className={styles.projMeta}>
            <Link to="/projects" className={styles.breadcrumb}>← Projects</Link>
            <div className={styles.projTitle}>
              <h2>{project.name}</h2>
              <Badge variant={statusCfg.variant}>{statusCfg.label}</Badge>
            </div>
            {project.description && (
              <p className={styles.projDesc}>{project.description}</p>
            )}
            <span className={styles.projDate}>Created {formatDate(project.createdAt || project.created_at)}</span>
          </div>
          <div className={styles.projActions}>
            <Button variant="ghost" size="sm" onClick={() => setShowEdit(true)}>Edit</Button>
            <Button variant="danger" size="sm" onClick={() => setShowDelete(true)}>Delete</Button>
          </div>
        </div>
      </Card>

      {/* Tasks */}
      <div className={styles.tasksSection}>
        <div className={styles.taskHeader}>
          <h3 className={styles.sectionTitle}>Tasks ({tasks.length})</h3>
          <Button variant="primary" size="sm" onClick={() => setShowNewTask(true)}>
            + New Task
          </Button>
        </div>

        {tasksLoading ? (
          <Spinner center />
        ) : (
          <TaskTable
            tasks={tasks}
            onEdit={setEditTask}
            onDelete={handleDeleteTask}
            onStatusChange={handleStatusChange}
          />
        )}
      </div>

      {/* Edit Project Modal */}
      <Modal isOpen={showEdit} onClose={() => setShowEdit(false)} title="Edit Project">
        <ProjectForm
          initialValues={project}
          onSubmit={handleEditProject}
          onCancel={() => setShowEdit(false)}
          submitLabel="Save Changes"
        />
      </Modal>

      {/* Delete Project Modal */}
      <Modal isOpen={showDelete} onClose={() => setShowDelete(false)} title="Delete Project" size="sm">
        <p className={styles.confirmText}>
          Delete <strong>{project.name}</strong>? All associated tasks will also be removed. This cannot be undone.
        </p>
        <div className={styles.confirmActions}>
          <Button variant="ghost" onClick={() => setShowDelete(false)}>Cancel</Button>
          <Button variant="danger" onClick={handleDeleteProject}>Delete</Button>
        </div>
      </Modal>

      {/* New Task Modal */}
      <Modal isOpen={showNewTask} onClose={() => setShowNewTask(false)} title="New Task">
        <TaskForm onSubmit={handleCreateTask} onCancel={() => setShowNewTask(false)} />
      </Modal>

      {/* Edit Task Modal */}
      <Modal isOpen={!!editTask} onClose={() => setEditTask(null)} title="Edit Task">
        {editTask && (
          <TaskForm
            initialValues={editTask}
            onSubmit={handleEditTask}
            onCancel={() => setEditTask(null)}
            submitLabel="Save Changes"
          />
        )}
      </Modal>
    </div>
  );
}
