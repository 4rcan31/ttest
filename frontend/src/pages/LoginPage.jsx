import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate, useSearchParams } from 'react-router';
import Alert from '../components/Alert.jsx';
import FormField from '../components/FormField.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { validateEmail } from '../utils/validators.js';
import useForm from '../utils/useForm.js';

const validate = (values) => {
  const errors = { email: validateEmail(values.email), password: values.password ? null : 'La contraseña es obligatoria' };
  return Object.fromEntries(Object.entries(errors).filter(([, v]) => v));
};

export default function LoginPage() {
  const { login, isAuthenticated } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [params] = useSearchParams();
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { values, errors, handleChange, handleBlur, validateAll } = useForm({ email: '', password: '' }, validate);

  const redirectTo = location.state?.from ?? '/';

  if (isAuthenticated && !submitting) return <Navigate to={redirectTo} replace />;

  const onSubmit = async (event) => {
    event.preventDefault();
    setServerError('');
    if (!validateAll()) return;
    setSubmitting(true);
    try {
      const user = await login(values.email.trim(), values.password);
      toast.success(`¡Bienvenido de nuevo, ${user.firstName.split(' ')[0]}!`);
      navigate(redirectTo, { replace: true });
    } catch (error) {
      setServerError(error.message);
      setSubmitting(false);
    }
  };

  return (
    <div className="auth">
      <div className="card auth__card">
        <h1 className="auth__title">Iniciar sesión</h1>
        <p className="auth__subtitle">Gestiona tu carrito, tus pedidos y tu perfil.</p>

        {params.get('expirada') && <Alert type="info">Tu sesión expiró. Inicia sesión nuevamente.</Alert>}
        <Alert>{serverError}</Alert>

        <form onSubmit={onSubmit} noValidate>
          <FormField label="Correo electrónico" name="email" type="email" autoComplete="email"
            value={values.email} onChange={handleChange} onBlur={handleBlur} error={errors.email} />
          <FormField label="Contraseña" name="password" type="password" autoComplete="current-password"
            value={values.password} onChange={handleChange} onBlur={handleBlur} error={errors.password} />
          <div className="auth__row">
            <Link to="/recuperar-password">¿Olvidaste tu contraseña?</Link>
          </div>
          <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
            {submitting ? 'Ingresando…' : 'Iniciar sesión'}
          </button>
        </form>

        <p className="auth__footer">¿No tienes cuenta? <Link to="/registro" state={location.state}>Regístrate</Link></p>

        <div className="demo-box">
          <strong>Cuentas de demostración</strong>
          <span>Cliente: cliente@sportshop.com · Demo1234</span>
          <span>Administrador: admin@sportshop.com · Demo1234</span>
        </div>
      </div>
    </div>
  );
}
