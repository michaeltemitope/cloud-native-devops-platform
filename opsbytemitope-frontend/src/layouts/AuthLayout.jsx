import { Outlet } from 'react-router-dom';
import styles from './AuthLayout.module.css';

export default function AuthLayout() {
  return (
    <div className={styles.page}>
      <div className={styles.brand}>
        <svg width="32" height="32" viewBox="0 0 32 32" fill="none">
          <rect width="32" height="32" rx="8" fill="#4f6ef7" />
          <path d="M8 16 L14 10 L20 16 L26 10" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" fill="none"/>
          <path d="M8 22 L14 16 L20 22 L26 16" stroke="white" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round" fill="none" opacity="0.6"/>
        </svg>
        <span className={styles.brandName}>OpsByTemitope</span>
      </div>
      <div className={styles.card}>
        <Outlet />
      </div>
    </div>
  );
}
