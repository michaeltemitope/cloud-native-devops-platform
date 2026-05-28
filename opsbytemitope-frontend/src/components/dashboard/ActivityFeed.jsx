import { timeAgo } from '../../utils/helpers';
import styles from './ActivityFeed.module.css';

export default function ActivityFeed({ events = [], loading }) {
  if (loading) {
    return (
      <div className={styles.list}>
        {[...Array(5)].map((_, i) => (
          <div key={i} className={styles.skeleton} />
        ))}
      </div>
    );
  }

  if (!events.length) {
    return <p className={styles.empty}>No recent activity.</p>;
  }

  return (
    <ul className={styles.list}>
      {events.map(event => (
        <li key={event.id} className={styles.item}>
          <div className={styles.dot} />
          <div className={styles.content}>
            <span className={styles.description}>{event.description}</span>
            <span className={styles.time}>{timeAgo(event.createdAt || event.created_at)}</span>
          </div>
        </li>
      ))}
    </ul>
  );
}
