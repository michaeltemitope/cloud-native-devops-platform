/**
 * Format an ISO date string to a readable short date.
 */
export function formatDate(dateString) {
  if (!dateString) return '—';
  return new Date(dateString).toLocaleDateString('en-US', {
    month: 'short',
    day: 'numeric',
    year: 'numeric',
  });
}

/**
 * Format an ISO date string to a relative time string (e.g., "2 hours ago").
 */
export function timeAgo(dateString) {
  if (!dateString) return '';
  const diff = Date.now() - new Date(dateString).getTime();
  const mins = Math.floor(diff / 60000);
  if (mins < 1) return 'just now';
  if (mins < 60) return `${mins}m ago`;
  const hrs = Math.floor(mins / 60);
  if (hrs < 24) return `${hrs}h ago`;
  const days = Math.floor(hrs / 24);
  if (days < 7) return `${days}d ago`;
  return formatDate(dateString);
}

/**
 * Get initials from a name string.
 */
export function getInitials(name = '') {
  return name
    .split(' ')
    .slice(0, 2)
    .map(n => n[0])
    .join('')
    .toUpperCase();
}

/**
 * Extract a user-facing error message from an Axios error.
 */
export function getErrorMessage(error) {
  if (typeof error === 'string') return error;
  return (
    error?.response?.data?.message ||
    error?.response?.data?.error ||
    error?.message ||
    'Something went wrong. Please try again.'
  );
}

/**
 * Map task/project status values to display labels and CSS class modifiers.
 */
export const STATUS_CONFIG = {
  todo: { label: 'To Do', variant: 'default' },
  in_progress: { label: 'In Progress', variant: 'info' },
  in_review: { label: 'In Review', variant: 'warning' },
  done: { label: 'Done', variant: 'success' },
  blocked: { label: 'Blocked', variant: 'danger' },
  active: { label: 'Active', variant: 'success' },
  archived: { label: 'Archived', variant: 'default' },
};

export const PRIORITY_CONFIG = {
  low: { label: 'Low', variant: 'default' },
  medium: { label: 'Medium', variant: 'warning' },
  high: { label: 'High', variant: 'danger' },
};
