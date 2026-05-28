import Badge from '../common/Badge';
import Button from '../common/Button';
import { formatDate, STATUS_CONFIG, PRIORITY_CONFIG } from '../../utils/helpers';
import styles from './TaskTable.module.css';

export default function TaskTable({ tasks, onEdit, onDelete, onStatusChange }) {
  if (!tasks.length) {
    return (
      <div className={styles.empty}>
        <svg width="40" height="40" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round" opacity="0.3">
          <path d="M9 11l3 3L22 4" /><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
        </svg>
        <p>No tasks found.</p>
      </div>
    );
  }

  return (
    <div className={styles.wrapper}>
      <table className={styles.table}>
        <thead>
          <tr>
            <th>Title</th>
            <th>Status</th>
            <th>Priority</th>
            <th>Due Date</th>
            <th>Actions</th>
          </tr>
        </thead>
        <tbody>
          {tasks.map(task => {
            const status   = STATUS_CONFIG[task.status]   || STATUS_CONFIG.todo;
            const priority = PRIORITY_CONFIG[task.priority] || PRIORITY_CONFIG.medium;

            return (
              <tr key={task.id}>
                <td className={styles.titleCell}>
                  <span className={styles.taskTitle}>{task.title}</span>
                  {task.description && (
                    <span className={styles.taskDesc}>{task.description}</span>
                  )}
                </td>
                <td>
                  <select
                    value={task.status}
                    onChange={e => onStatusChange && onStatusChange(task, e.target.value)}
                    className={styles.statusSelect}
                    aria-label="Task status"
                  >
                    {Object.entries(STATUS_CONFIG)
                      .filter(([k]) => !['active','archived'].includes(k))
                      .map(([value, cfg]) => (
                        <option key={value} value={value}>{cfg.label}</option>
                      ))}
                  </select>
                </td>
                <td>
                  <Badge variant={priority.variant}>{priority.label}</Badge>
                </td>
                <td className={styles.dateCell}>
                  {formatDate(task.dueDate || task.due_date)}
                </td>
                <td>
                  <div className={styles.rowActions}>
                    {onEdit && (
                      <Button variant="ghost" size="sm" onClick={() => onEdit(task)}>
                        Edit
                      </Button>
                    )}
                    {onDelete && (
                      <Button variant="ghost" size="sm" onClick={() => onDelete(task)}>
                        <svg width="13" height="13" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                          <polyline points="3 6 5 6 21 6" /><path d="M19 6l-1 14a2 2 0 0 1-2 2H8a2 2 0 0 1-2-2L5 6" />
                        </svg>
                      </Button>
                    )}
                  </div>
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}
