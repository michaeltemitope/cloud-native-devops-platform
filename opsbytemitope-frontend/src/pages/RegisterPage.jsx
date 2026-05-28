import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import Alert from '../components/common/Alert';
import { getErrorMessage } from '../utils/helpers';
import styles from './AuthPage.module.css';

export default function RegisterPage() {
  const [form, setForm]         = useState({ firstName: '', lastName: '', email: '', password: '', confirm: '' });
  const [errors, setErrors]     = useState({});
  const [apiError, setApiError] = useState('');
  const [loading, setLoading]   = useState(false);
  const { register } = useAuth();
  const navigate = useNavigate();

  function validate() {
    const errs = {};
    if (!form.firstName.trim())   errs.firstName = 'First name is required';
    if (!form.lastName.trim())    errs.lastName  = 'Last name is required';
    if (!form.email.trim())       errs.email     = 'Email is required';
    if (!form.password)           errs.password  = 'Password is required';
    if (form.password.length < 8) errs.password  = 'Password must be at least 8 characters';
    if (form.password !== form.confirm) errs.confirm = 'Passwords do not match';
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
      await register(form.firstName, form.lastName, form.email, form.password);
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
        <h1 className={styles.title}>Create your account</h1>
        <p className={styles.subtitle}>Start managing your work with OpsByTemitope</p>
      </div>

      {apiError && <Alert type="error" onDismiss={() => setApiError('')}>{apiError}</Alert>}

      <form onSubmit={handleSubmit} className={styles.form} noValidate>
        <Input
          id="firstName"
          label="First Name"
          name="firstName"
          type="text"
          value={form.firstName}
          onChange={handleChange}
          error={errors.firstName}
          placeholder="Jane"
          autoComplete="given-name"
          required
          autoFocus
        />
        <Input
          id="lastName"
          label="Last Name"
          name="lastName"
          type="text"
          value={form.lastName}
          onChange={handleChange}
          error={errors.lastName}
          placeholder="Smith"
          autoComplete="family-name"
          required
        />
        <Input
          id="email"
          label="Email"
          name="email"
          type="email"
          value={form.email}
          onChange={handleChange}
          error={errors.email}
          placeholder="jane@example.com"
          autoComplete="email"
          required
        />
        <Input
          id="password"
          label="Password"
          name="password"
          type="password"
          value={form.password}
          onChange={handleChange}
          error={errors.password}
          placeholder="Min. 8 characters"
          autoComplete="new-password"
          required
        />
        <Input
          id="confirm"
          label="Confirm Password"
          name="confirm"
          type="password"
          value={form.confirm}
          onChange={handleChange}
          error={errors.confirm}
          placeholder="Repeat your password"
          autoComplete="new-password"
          required
        />
        <Button type="submit" variant="primary" size="lg" loading={loading} style={{ width: '100%', marginTop: 4 }}>
          Create Account
        </Button>
      </form>

      <p className={styles.footer}>
        Already have an account? <Link to="/login">Sign in</Link>
      </p>
    </>
  );
}
