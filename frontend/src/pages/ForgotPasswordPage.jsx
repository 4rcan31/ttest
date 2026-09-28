import { useState } from 'react';
import { Link } from 'react-router';
import { authApi } from '../api/services.js';
import Alert from '../components/Alert.jsx';
import FormField from '../components/FormField.jsx';
import useForm from '../utils/useForm.js';
import { validateEmail } from '../utils/validators.js';

const validate = (values) => {
  const email = validateEmail(values.email);
  return email ? { email } : {};
};

const MAILPIT_URL = import.meta.env.VITE_MAILPIT_URL;

export default function ForgotPasswordPage() {
  const [message, setMessage] = useState('');
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { values, errors, handleChange, handleBlur, validateAll } = useForm({ email: '' }, validate);

  const onSubmit = async (event) => {
    event.preventDefault();
    setServerError('');
    if (!validateAll()) return;
    setSubmitting(true);
    try {
      const response = await authApi.forgotPassword(values.email.trim());
      setMessage(response.message);
    } catch (error) {
      setServerError(error.message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="auth">
      <div className="card auth__card">
        <h1 className="auth__title">Recuperar contraseña</h1>
        <p className="auth__subtitle">Te enviaremos un enlace válido por 30 minutos para crear una nueva contraseña.</p>
        <Alert>{serverError}</Alert>

        {message ? (
          <>
            <Alert type="success">{message}</Alert>
            {MAILPIT_URL && (
              <p className="auth__hint">
                Entorno de demostración: revisa el correo en <a href={MAILPIT_URL} target="_blank" rel="noreferrer">Mailpit</a>.
              </p>
            )}
            <Link to="/login" className="btn btn--ghost btn--block">Volver a iniciar sesión</Link>
          </>
        ) : (
          <form onSubmit={onSubmit} noValidate>
            <FormField label="Correo electrónico" name="email" type="email" autoComplete="email"
              value={values.email} onChange={handleChange} onBlur={handleBlur} error={errors.email} />
            <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
              {submitting ? 'Enviando…' : 'Enviar enlace de recuperación'}
            </button>
            <p className="auth__footer"><Link to="/login">Volver a iniciar sesión</Link></p>
          </form>
        )}
      </div>
    </div>
  );
}
