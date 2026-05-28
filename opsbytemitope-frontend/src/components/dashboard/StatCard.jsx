import styles from './StatCard.module.css';

export default function StatCard({ title, value, icon, variant = 'default', trend }) {
  return (
    <div className={`${styles.card} ${styles[variant]}`}>
      <div className={styles.top}>
        <span className={styles.title}>{title}</span>
        {icon && <span className={styles.icon}>{icon}</span>}
      </div>
      <div className={styles.value}>{value ?? '—'}</div>
      {trend && <div className={styles.trend}>{trend}</div>}
    </div>
  );
}
