import { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { dashboardService } from '../services/dashboardService';
import StatCard from '../components/dashboard/StatCard';
import ActivityFeed from '../components/dashboard/ActivityFeed';
import Card from '../components/common/Card';
import Spinner from '../components/common/Spinner';
import Alert from '../components/common/Alert';
import styles from './DashboardPage.module.css';

export default function DashboardPage() {
  const { user } = useAuth();
  const [summary,  setSummary]  = useState(null);
  const [activity, setActivity] = useState([]);
  const [loading,  setLoading]  = useState(true);
  const [error,    setError]    = useState('');

  useEffect(() => {
    async function load() {
      try {
        const [sum, act] = await Promise.all([
          dashboardService.getSummary(),
          dashboardService.getActivity({ limit: 10 }),
        ]);
        setSummary(sum);
        setActivity(act);
      } catch (err) {
        setError(err.response?.data?.message || 'Failed to load dashboard data');
      } finally {
        setLoading(false);
      }
    }
    load();
  }, []);

  if (loading) return <Spinner center size="lg" />;

  // Backend returns fullName; fall back to firstName if available
  const displayName = user?.fullName || user?.firstName || user?.name || 'there';

  return (
    <div className={styles.page}>
      <div className={styles.welcome}>
        <h2 className={styles.welcomeText}>
          Good {getTimeOfDay()}, <strong>{displayName.split(' ')[0]}</strong> 👋
        </h2>
        <p className={styles.welcomeSub}>Here's what's going on with your projects today.</p>
      </div>

      {error && <Alert type="error">{error}</Alert>}

      {/* Stat Cards */}
      <div className={styles.statsGrid}>
        <StatCard
          title="Total Projects"
          value={summary?.totalProjects ?? 0}
          variant="primary"
          icon={<FolderIcon />}
        />
        <StatCard
          title="Total Tasks"
          value={summary?.totalTasks ?? 0}
          variant="default"
          icon={<TaskIcon />}
        />
        <StatCard
          title="In Progress"
          value={summary?.tasksByStatus?.IN_PROGRESS ?? 0}
          variant="warning"
          icon={<ProgressIcon />}
        />
        <StatCard
          title="Completed"
          value={summary?.completedTasks ?? 0}
          variant="success"
          icon={<CheckIcon />}
        />
      </div>

      <div className={styles.bottomGrid}>
        {/* Recent Activity */}
        <Card>
          <div className={styles.sectionHeader}>
            <h3 className={styles.sectionTitle}>Recent Activity</h3>
          </div>
          <ActivityFeed events={activity} />
        </Card>

        {/* Quick Links */}
        <Card>
          <div className={styles.sectionHeader}>
            <h3 className={styles.sectionTitle}>Quick Actions</h3>
          </div>
          <div className={styles.quickActions}>
            <Link to="/projects" className={styles.quickLink}>
              <span className={styles.quickIcon} style={{ background: 'var(--color-primary-light)', color: 'var(--color-primary)' }}>
                <FolderIcon />
              </span>
              <div>
                <span className={styles.quickLabel}>View All Projects</span>
                <span className={styles.quickSub}>Manage your project portfolio</span>
              </div>
            </Link>
            <Link to="/tasks" className={styles.quickLink}>
              <span className={styles.quickIcon} style={{ background: 'var(--color-success-bg)', color: 'var(--color-success)' }}>
                <TaskIcon />
              </span>
              <div>
                <span className={styles.quickLabel}>Manage Tasks</span>
                <span className={styles.quickSub}>View and update all tasks</span>
              </div>
            </Link>
            <Link to="/profile" className={styles.quickLink}>
              <span className={styles.quickIcon} style={{ background: 'var(--color-warning-bg)', color: 'var(--color-warning)' }}>
                <UserIcon />
              </span>
              <div>
                <span className={styles.quickLabel}>Edit Profile</span>
                <span className={styles.quickSub}>Update your account settings</span>
              </div>
            </Link>
          </div>
        </Card>
      </div>
    </div>
  );
}

function getTimeOfDay() {
  const h = new Date().getHours();
  if (h < 12) return 'morning';
  if (h < 17) return 'afternoon';
  return 'evening';
}

function FolderIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M3 3h6l3 3h9a2 2 0 0 1 2 2v9a2 2 0 0 1-2 2H3a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2z" />
    </svg>
  );
}

function TaskIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M9 11l3 3L22 4" /><path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11" />
    </svg>
  );
}

function ProgressIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <circle cx="12" cy="12" r="10" /><polyline points="12 6 12 12 16 14" />
    </svg>
  );
}

function CheckIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <polyline points="20 6 9 17 4 12" />
    </svg>
  );
}

function UserIcon() {
  return (
    <svg width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" /><circle cx="12" cy="7" r="4" />
    </svg>
  );
}
