import { useState } from 'react';
import Input from '../common/Input';
import Select from '../common/Select';
import Button from '../common/Button';
import Alert from '../common/Alert';
import { getErrorMessage } from '../../utils/helpers';
import styles from './ProjectForm.module.css';

const STATUS_OPTIONS = [
  { value: 'active', label: 'Active' },
  { value: 'archived', label: 'Archived' },
];

export default function ProjectForm({ initialValues = {}, onSubmit, onCancel, submitLabel = 'Create Project' }) {
  const [form, setForm] = useState({
    name: initialValues.name || '',
    description: initialValues.description || '',
    status: initialValues.status || 'active',
  });
  const [errors, setErrors] = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);

  function validate() {
    const errs = {};
    if (!form.name.trim()) errs.name = 'Project name is required';
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

  return (
    <form onSubmit={handleSubmit} className={styles.form} noValidate>
      {apiError && <Alert type="error">{apiError}</Alert>}

      <Input
        id="project-name"
        label="Project Name"
        name="name"
        value={form.name}
        onChange={handleChange}
        error={errors.name}
        placeholder="e.g. Backend API Refactor"
        required
        autoFocus
      />

      <div className={styles.field}>
        <label htmlFor="project-desc" className={styles.label}>Description</label>
        <textarea
          id="project-desc"
          name="description"
          value={form.description}
          onChange={handleChange}
          placeholder="What is this project about?"
          className={styles.textarea}
          rows={3}
        />
      </div>

      <Select
        id="project-status"
        label="Status"
        name="status"
        value={form.status}
        onChange={handleChange}
        options={STATUS_OPTIONS}
      />

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
