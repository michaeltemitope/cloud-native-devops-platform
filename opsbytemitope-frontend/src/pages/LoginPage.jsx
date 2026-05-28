import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import Alert from '../components/common/Alert';
import { getErrorMessage } from '../utils/helpers';
import styles from './AuthPage.module.css';

export default function LoginPage() {
  const [form, setForm]       = useState({ email: '', password: '' });
  const [errors, setErrors]   = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading] = useState(false);
  const { login } = useAuth();
  const navigate = useNavigate();

  function validate() {
    const errs = {};
    if (!form.email.trim())    errs.email    = 'Email is required';
    if (!form.password)        errs.password = 'Password is required';
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
      await login(form.email, form.password);
      navigate('/dashboard');
    } catch (err) {
      setApiError(getErrorMessage(err));
    } finally {
      setLoading(false);
    }
  }

  return (
    <>
      <div className={styles.heading}>
        <h1 className={styles.title}>Welcome back</h1>
        <p className={styles.subtitle}>Sign in to your OpsByTemitope account</p>
      </div>

      {apiError && <Alert type="error" onDismiss={() => setApiError('')}>{apiError}</Alert>}

      <form onSubmit={handleSubmit} className={styles.form} noValidate>
        <Input
          id="email"
          label="Email"
          name="email"
          type="email"
          value={form.email}
          onChange={handleChange}
          error={errors.email}
          placeholder="you@example.com"
          autoComplete="email"
          required
          autoFocus
        />
        <Input
          id="password"
          label="Password"
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          error={errors.password}
          placeholder="Your password"
          autoComplete="current-password"
          required
        />
        <Button type="submit" variant="primary" size="lg" loading={loading} style={{ width: '100%', marginTop: 4 }}>
          Sign In
        </Button>
      </form>

      <p className={styles.footer}>
        Don't have an account? <Link to="/register">Create one</Link>
      </p>
    </>
  );
}
