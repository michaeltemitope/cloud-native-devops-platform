import { useState } from 'react';
import { useProjects } from '../hooks/useProjects';
import { projectService } from '../services/projectService';
import ProjectCard from '../components/projects/ProjectCard';
import ProjectForm from '../components/projects/ProjectForm';
import Modal from '../components/common/Modal';
import Button from '../components/common/Button';
import Spinner from '../components/common/Spinner';
import Alert from '../components/common/Alert';
import styles from './ProjectsPage.module.css';

export default function ProjectsPage() {
  const { projects, loading, error, refetch, setProjects } = useProjects();
  const [showCreate, setShowCreate] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState(null);
  const [deleteLoading, setDeleteLoading] = useState(false);
  const [actionError, setActionError] = useState('');

  async function handleCreate(formData) {
    const created = await projectService.create(formData);
    setProjects(prev => [created.project || created, ...prev]);
    setShowCreate(false);
  }

  async function handleDelete(project) {
    setDeleteLoading(true);
    setActionError('');
    try {
      await projectService.delete(project.id);
      setProjects(prev => prev.filter(p => p.id !== project.id));
      setDeleteTarget(null);
    } catch (err) {
      setActionError(err.response?.data?.message || 'Failed to delete project');
    } finally {
      setDeleteLoading(false);
    }
  }

  return (
    <div className={styles.page}>
      <div className={styles.pageHeader}>
        <div>
          <h2 className={styles.pageTitle}>Projects</h2>
          <p className={styles.pageSubtitle}>{projects.length} project{projects.length !== 1 ? 's' : ''}</p>
        </div>
        <Button variant="primary" onClick={() => setShowCreate(true)}>
          <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <line x1="12" y1="5" x2="12" y2="19" /><line x1="5" y1="12" x2="19" y2="12" />
          </svg>
          New Project
        </Button>
      </div>

      {actionError && <Alert type="error" onDismiss={() => setActionError('')}>{actionError}</Alert>}
      {error      && <Alert type="error">{error}</Alert>}

      {loading ? (
        <Spinner center size="lg" />
      ) : projects.length === 0 ? (
        <div className={styles.empty}>
          <svg width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" opacity="0.25">
            <path d="M3 3h6l3 3h9a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z" />
          </svg>
          <p>No projects yet. Create your first one!</p>
          <Button variant="primary" onClick={() => setShowCreate(true)}>Create Project</Button>
        </div>
      ) : (
        <div className={styles.grid}>
          {projects.map(project => (
            <ProjectCard
              key={project.id}
              project={project}
              onDelete={setDeleteTarget}
            />
          ))}
        </div>
      )}

      {/* Create Modal */}
      <Modal isOpen={showCreate} onClose={() => setShowCreate(false)} title="New Project">
        <ProjectForm
          onSubmit={handleCreate}
          onCancel={() => setShowCreate(false)}
        />
      </Modal>

      {/* Confirm Delete Modal */}
      <Modal isOpen={!!deleteTarget} onClose={() => setDeleteTarget(null)} title="Delete Project" size="sm">
        <p className={styles.confirmText}>
          Are you sure you want to delete <strong>{deleteTarget?.name}</strong>? This action cannot be undone.
        </p>
        <div className={styles.confirmActions}>
          <Button variant="ghost" onClick={() => setDeleteTarget(null)} disabled={deleteLoading}>
            Cancel
          </Button>
          <Button variant="danger" onClick={() => handleDelete(deleteTarget)} loading={deleteLoading}>
            Delete
          </Button>
        </div>
      </Modal>
    </div>
  );
}
