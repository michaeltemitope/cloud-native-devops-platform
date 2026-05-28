import { useState } from 'react';
import { useAuth } from '../context/AuthContext';
import { authService } from '../services/authService';
import Card from '../components/common/Card';
import Input from '../components/common/Input';
import Button from '../components/common/Button';
import Alert from '../components/common/Alert';
import { getInitials, getErrorMessage } from '../utils/helpers';
import styles from './ProfilePage.module.css';

export default function ProfilePage() {
  const { user, updateUser } = useAuth();

  // Backend UserResponse has firstName, lastName, fullName, email
  const [profileForm, setProfileForm] = useState({
    firstName: user?.firstName || '',
    lastName:  user?.lastName  || '',
    email:     user?.email     || '',
  });
  const [profileError,   setProfileError]   = useState('');
  const [profileSuccess, setProfileSuccess] = useState('');
  const [profileLoading, setProfileLoading] = useState(false);

  const [pwForm, setPwForm] = useState({ currentPassword: '', newPassword: '', confirmPassword: '' });
  const [pwError,   setPwError]   = useState('');
  const [pwSuccess, setPwSuccess] = useState('');
  const [pwLoading, setPwLoading] = useState(false);
  const [pwErrors,  setPwErrors]  = useState({});

  function handleProfileChange(e) {
    setProfileForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
  }

  async function handleProfileSubmit(e) {
    e.preventDefault();
    setProfileLoading(true);
    setProfileError('');
    setProfileSuccess('');
    try {
      const updated = await authService.updateProfile(profileForm);
      updateUser(updated || profileForm);
      setProfileSuccess('Profile updated successfully.');
    } catch (err) {
      setProfileError(getErrorMessage(err));
    } finally {
      setProfileLoading(false);
    }
  }

  function handlePwChange(e) {
    setPwForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    if (pwErrors[e.target.name]) setPwErrors(prev => ({ ...prev, [e.target.name]: '' }));
  }

  function validatePw() {
    const errs = {};
    if (!pwForm.currentPassword)       errs.currentPassword = 'Required';
    if (!pwForm.newPassword)           errs.newPassword     = 'Required';
    if (pwForm.newPassword.length < 8) errs.newPassword     = 'Min. 8 characters';
    if (pwForm.newPassword !== pwForm.confirmPassword) errs.confirmPassword = 'Passwords do not match';
    return errs;
  }

  async function handlePwSubmit(e) {
    e.preventDefault();
    const errs = validatePw();
    if (Object.keys(errs).length) { setPwErrors(errs); return; }

    setPwLoading(true);
    setPwError('');
    setPwSuccess('');
    try {
      await authService.changePassword(pwForm.currentPassword, pwForm.newPassword);
      setPwSuccess('Password changed successfully.');
      setPwForm({ currentPassword: '', newPassword: '', confirmPassword: '' });
    } catch (err) {
      setPwError(getErrorMessage(err));
    } finally {
      setPwLoading(false);
    }
  }

  const displayName = user?.fullName || `${user?.firstName || ''} ${user?.lastName || ''}`.trim() || 'User';

  return (
    <div className={styles.page}>
      {/* User Info Header */}
      <Card className={styles.profileCard}>
        <div className={styles.avatar}>{getInitials(displayName)}</div>
        <div className={styles.userInfo}>
          <h2 className={styles.userName}>{displayName}</h2>
          <p className={styles.userEmail}>{user?.email}</p>
        </div>
      </Card>

      <div className={styles.grid}>
        {/* Edit Profile */}
        <Card>
          <h3 className={styles.sectionTitle}>Profile Information</h3>
          {profileError   && <Alert type="error"   onDismiss={() => setProfileError('')}>{profileError}</Alert>}
          {profileSuccess && <Alert type="success" onDismiss={() => setProfileSuccess('')}>{profileSuccess}</Alert>}
          <form onSubmit={handleProfileSubmit} className={styles.form} noValidate>
            <Input
              id="profile-firstName"
              label="First Name"
              name="firstName"
              value={profileForm.firstName}
              onChange={handleProfileChange}
              placeholder="First name"
              required
            />
            <Input
              id="profile-lastName"
              label="Last Name"
              name="lastName"
              value={profileForm.lastName}
              onChange={handleProfileChange}
              placeholder="Last name"
              required
            />
            <Input
              id="profile-email"
              label="Email Address"
              name="email"
              type="email"
              value={profileForm.email}
              onChange={handleProfileChange}
              placeholder="you@example.com"
              required
            />
            <Button type="submit" variant="primary" loading={profileLoading}>
              Save Changes
            </Button>
          </form>
        </Card>

        {/* Change Password */}
        <Card>
          <h3 className={styles.sectionTitle}>Change Password</h3>
          {pwError   && <Alert type="error"   onDismiss={() => setPwError('')}>{pwError}</Alert>}
          {pwSuccess && <Alert type="success" onDismiss={() => setPwSuccess('')}>{pwSuccess}</Alert>}
          <form onSubmit={handlePwSubmit} className={styles.form} noValidate>
            <Input
              id="current-pw"
              label="Current Password"
              name="currentPassword"
              type="password"
              value={pwForm.currentPassword}
              onChange={handlePwChange}
              error={pwErrors.currentPassword}
              required
            />
            <Input
              id="new-pw"
              label="New Password"
              name="newPassword"
              type="password"
              value={pwForm.newPassword}
              onChange={handlePwChange}
              error={pwErrors.newPassword}
              placeholder="Min. 8 characters"
              required
            />
            <Input
              id="confirm-pw"
              label="Confirm New Password"
              name="confirmPassword"
              type="password"
              value={pwForm.confirmPassword}
              onChange={handlePwChange}
              error={pwErrors.confirmPassword}
              required
            />
            <Button type="submit" variant="primary" loading={pwLoading}>
              Update Password
            </Button>
          </form>
        </Card>
      </div>
    </div>
  );
}
