import { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router';
import Alert from '../components/Alert.jsx';
import FormField from '../components/FormField.jsx';
import { CheckIcon } from '../components/Icons.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import useForm from '../utils/useForm.js';
import { maxBirthDate, passwordChecks, validateRegistration } from '../utils/validators.js';

const INITIAL = {
  firstName: '', lastName: '', email: '', birthDate: '', shippingAddress: '', password: '', confirmPassword: '',
};

export default function RegisterPage() {
  const { register, isAuthenticated } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [serverError, setServerError] = useState('');
  const [submitting, setSubmitting] = useState(false);
  const { values, errors, handleChange, handleBlur, validateAll, setServerErrors } = useForm(INITIAL, validateRegistration);

  if (isAuthenticated && !submitting) return <Navigate to="/" replace />;

  const onSubmit = async (event) => {
    event.preventDefault();
    setServerError('');
    if (!validateAll()) return;
    setSubmitting(true);
    try {
      const { confirmPassword: _ignored, ...payload } = values;
      const user = await register({
        ...payload,
        firstName: payload.firstName.trim(),
        lastName: payload.lastName.trim(),
        email: payload.email.trim(),
        shippingAddress: payload.shippingAddress.trim(),
      });
      toast.success(`¡Cuenta creada! Bienvenido, ${user.firstName.split(' ')[0]}`);
      navigate(location.state?.from ?? '/', { replace: true });
    } catch (error) {
      setServerErrors(error.fieldErrors);
      setServerError(error.message);
      setSubmitting(false);
    }
  };

  const field = (name) => ({ name, value: values[name], onChange: handleChange, onBlur: handleBlur, error: errors[name] });

  return (
    <div className="auth auth--wide">
      <div className="card auth__card">
        <h1 className="auth__title">Crear cuenta</h1>
        <p className="auth__subtitle">Todos los campos son obligatorios.</p>
        <Alert>{serverError}</Alert>

        <form onSubmit={onSubmit} noValidate aria-label="Registro de usuario">
          <div className="form-grid">
            <FormField label="Nombres" autoComplete="given-name" {...field('firstName')} />
            <FormField label="Apellidos" autoComplete="family-name" {...field('lastName')} />
            <FormField label="Correo electrónico" type="email" autoComplete="email" {...field('email')} />
            <FormField label="Fecha de nacimiento" type="date" max={maxBirthDate()} autoComplete="bday"
              hint="Debes ser mayor de 18 años" {...field('birthDate')} />
          </div>
          <FormField label="Dirección de envío" as="textarea" rows={2} autoComplete="street-address"
            placeholder="Colonia, calle, número de casa, ciudad" {...field('shippingAddress')} />
          <div className="form-grid">
            <FormField label="Contraseña" type="password" autoComplete="new-password" {...field('password')} />
            <FormField label="Confirmar contraseña" type="password" autoComplete="new-password" {...field('confirmPassword')} />
          </div>
          <ul className="password-checks" aria-label="Requisitos de la contraseña">
            {passwordChecks(values.password).map((check) => (
              <li key={check.label} className={check.ok ? 'ok' : ''}><CheckIcon size={14} /> {check.label}</li>
            ))}
          </ul>
          <button type="submit" className="btn btn--primary btn--block" disabled={submitting}>
            {submitting ? 'Creando cuenta…' : 'Crear cuenta'}
          </button>
        </form>
        <p className="auth__footer">¿Ya tienes cuenta? <Link to="/login" state={location.state}>Inicia sesión</Link></p>
      </div>
    </div>
  );
}
