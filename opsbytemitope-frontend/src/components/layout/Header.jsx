import { useLocation } from 'react-router-dom';
import styles from './Header.module.css';

const PAGE_TITLES = {
  '/dashboard': 'Dashboard',
  '/projects': 'Projects',
  '/tasks': 'Tasks',
  '/profile': 'Profile',
};

export default function Header() {
  const location = useLocation();
  const title = PAGE_TITLES[location.pathname] || 'OpsByTemitope';

  return (
    <header className={styles.header}>
      <h1 className={styles.title}>{title}</h1>
      <div className={styles.actions}>
        <span className={styles.date}>
          {new Date().toLocaleDateString('en-US', { weekday: 'short', month: 'short', day: 'numeric' })}
        </span>
      </div>
    </header>
  );
}
