import { useState } from 'react';
import { Link, useSearchParams } from 'react-router';
import { authApi } from '../api/services.js';
import Alert from '../components/Alert.jsx';
import FormField from '../components/FormField.jsx';
import { CheckIcon } from '../components/Icons.jsx';
import useForm from '../utils/useForm.js';
import { passwordChecks, validatePassword, validatePasswordConfirmation } from '../utils/validators.js';

const validate = (values) => {
  const errors = {
    password: validatePassword(values.password),
    confirmPassword: validatePasswordConfirmation(values.password, values.confirmPassword),
  };
  return Object.fromEntries(Object.entries(errors).filter(([, v]) => v));
};

export default function ResetPasswordPage() {
  const [params] = useSearchParams();
  const token = params.get('token');
  const [done, setDone] = useState('');
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { values, errors, handleChange, handleBlur, validateAll, setServerErrors } =
    useForm({ password: '', confirmPassword: '' }, validate);

  const onSubmit = async (event) => {
    event.preventDefault();
    setServerError('');
    if (!validateAll()) return;
    setSubmitting(true);
    try {
      const response = await authApi.resetPassword(token, values.password);
      setDone(response.message);
    } catch (error) {
      setServerErrors({ password: error.fieldErrors?.newPassword });
      setServerError(error.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth">
      <div className="card auth__card">
        <h1 className="auth__title">Nueva contraseña</h1>
        {!token && (
          <Alert>El enlace no es válido. <Link to="/recuperar-password">Solicita uno nuevo</Link>.</Alert>
        )}
        <Alert>{serverError}</Alert>
        {done ? (
          <>
            <Alert type="success">{done}</Alert>
            <Link to="/login" className="btn btn--primary btn--block">Iniciar sesión</Link>
          </>
        ) : token && (
          <form onSubmit={onSubmit} noValidate>
            <FormField label="Nueva contraseña" name="password" type="password" autoComplete="new-password"
              value={values.password} onChange={handleChange} onBlur={handleBlur} error={errors.password} />
            <FormField label="Confirmar contraseña" name="confirmPassword" type="password" autoComplete="new-password"
              value={values.confirmPassword} onChange={handleChange} onBlur={handleBlur} error={errors.confirmPassword} />
            <ul className="password-checks">
              {passwordChecks(values.password).map((check) => (
                <li key={check.label} className={check.ok ? 'ok' : ''}><CheckIcon size={14} /> {check.label}</li>
              ))}
            </ul>
            <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
              {submitting ? 'Guardando…' : 'Guardar contraseña'}
            </button>
          </form>
        )}
      </div>
    </div>
  );
}
