import styles from './Spinner.module.css';

export default function Spinner({ size = 'md', center = false }) {
  return (
    <div className={center ? styles.center : undefined}>
      <div className={`${styles.spinner} ${styles[size]}`} role="status">
        <span className="sr-only">Loading…</span>
      </div>
    </div>
  );
}
