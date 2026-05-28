import { useState } from 'react';
import Input from '../common/Input';
import Select from '../common/Select';
import Button from '../common/Button';
import Alert from '../common/Alert';
import { getErrorMessage } from '../../utils/helpers';
import styles from './TaskForm.module.css';

const STATUS_OPTIONS = [
  { value: 'todo',        label: 'To Do' },
  { value: 'in_progress', label: 'In Progress' },
  { value: 'in_review',   label: 'In Review' },
  { value: 'done',        label: 'Done' },
  { value: 'blocked',     label: 'Blocked' },
];

const PRIORITY_OPTIONS = [
  { value: 'low',    label: 'Low' },
  { value: 'medium', label: 'Medium' },
  { value: 'high',   label: 'High' },
];

export default function TaskForm({ initialValues = {}, projects = [], onSubmit, onCancel, submitLabel = 'Create Task' }) {
  const [form, setForm] = useState({
    title:       initialValues.title       || '',
    description: initialValues.description || '',
    status:      initialValues.status      || 'todo',
    priority:    initialValues.priority    || 'medium',
    dueDate:     initialValues.dueDate     || initialValues.due_date || '',
    projectId:   initialValues.projectId   || initialValues.project_id || '',
  });
  const [errors,   setErrors]   = useState({});
  const [apiError, setApiError] = useState('');
  const [loading,  setLoading]  = useState(false);

  function validate() {
    const errs = {};
    if (!form.title.trim()) errs.title = 'Task title is required';
    return errs;
  }

  function handleChange(e) {
    const { name, value } = e.target;
    setForm(prev => ({ ...prev, [name]: value }));
    if (errors[name]) setErrors(prev => ({ ...prev, [name]: '' }));
  }

  async function handleSubmit(e) {
    e.preventDefault();
    const errs = validate();
    if (Object.keys(errs).length) { setErrors(errs); return; }

    setLoading(true);
    setApiError('');
    try {
      await onSubmit(form);
    } catch (err) {
      setApiError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  const projectOptions = [
    { value: '', label: '— No Project —' },
    ...projects.map(p => ({ value: p.id, label: p.name })),
  ];

  return (
    <form onSubmit={handleSubmit} className={styles.form} noValidate>
      {apiError && <Alert type="error">{apiError}</Alert>}

      <Input
        id="task-title"
        label="Title"
        name="title"
        value={form.title}
        onChange={handleChange}
        error={errors.title}
        placeholder="e.g. Fix authentication bug"
        required
        autoFocus
      />

      <div className={styles.field}>
        <label htmlFor="task-desc" className={styles.label}>Description</label>
        <textarea
          id="task-desc"
          name="description"
          value={form.description}
          onChange={handleChange}
          placeholder="Optional details about this task…"
          className={styles.textarea}
          rows={3}
        />
      </div>

      <div className={styles.row}>
        <Select
          id="task-status"
          label="Status"
          name="status"
          value={form.status}
          onChange={handleChange}
          options={STATUS_OPTIONS}
        />
        <Select
          id="task-priority"
          label="Priority"
          name="priority"
          value={form.priority}
          onChange={handleChange}
          options={PRIORITY_OPTIONS}
        />
      </div>

      <div className={styles.row}>
        <Input
          id="task-due"
          label="Due Date"
          name="dueDate"
          type="date"
          value={form.dueDate}
          onChange={handleChange}
        />
        {projects.length > 0 && (
          <Select
            id="task-project"
            label="Project"
            name="projectId"
            value={form.projectId}
            onChange={handleChange}
            options={projectOptions}
          />
        )}
      </div>

      <div className={styles.actions}>
        {onCancel && (
          <Button type="button" variant="ghost" onClick={onCancel} disabled={loading}>
            Cancel
          </Button>
        )}
        <Button type="submit" variant="primary" loading={loading}>
          {submitLabel}
        </Button>
      </div>
    </form>
  );
}
