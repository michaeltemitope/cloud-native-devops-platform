import { Link } from 'react-router-dom';
import Badge from '../common/Badge';
import { formatDate, STATUS_CONFIG } from '../../utils/helpers';
import styles from './ProjectCard.module.css';

export default function ProjectCard({ project, onDelete }) {
  const status = STATUS_CONFIG[project.status] || STATUS_CONFIG.active;

  return (
    <div className={styles.card}>
      <div className={styles.header}>
        <Link to={`/projects/${project.id}`} className={styles.name}>
          {project.name}
        </Link>
        <Badge variant={status.variant}>{status.label}</Badge>
      </div>

      {project.description && (
        <p className={styles.description}>{project.description}</p>
      )}

      <div className={styles.meta}>
        <span className={styles.metaItem}>
          <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <rect x="3" y="4" width="18" height="18" rx="2" ry="2" /><line x1="16" y1="2" x2="16" y2="6" /><line x1="8" y1="2" x2="8" y2="6" /><line x1="3" y1="10" x2="21" y2="10" />
          </svg>
          {formatDate(project.createdAt || project.created_at)}
        </span>
        {project.taskCount !== undefined && (
          <span className={styles.metaItem}>
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <path d="M9 11l3 3L22 4" /><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
            </svg>
            {project.taskCount} tasks
          </span>
        )}
      </div>

      <div className={styles.actions}>
        <Link to={`/projects/${project.id}`} className={styles.viewBtn}>
          View Project →
        </Link>
        {onDelete && (
          <button
            className={styles.deleteBtn}
            onClick={() => onDelete(project)}
            aria-label="Delete project"
          >
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
              <polyline points="3 6 5 6 21 6" /><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" /><path d="M10 11v6M14 11v6" /><path d="M9 6V4h6v2" />
            </svg>
          </button>
        )}
      </div>
    </div>
  );
}
