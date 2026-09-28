import { useEffect, useState } from 'react';
import { useNavigate } from 'react-router';
import { userApi } from '../api/services.js';
import Alert from '../components/Alert.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import FormField from '../components/FormField.jsx';
import Spinner from '../components/Spinner.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { formatDate } from '../utils/format.js';
import useForm from '../utils/useForm.js';
import {
  maxBirthDate, validatePassword, validatePasswordConfirmation, validateProfile,
} from '../utils/validators.js';

const EMPTY_PROFILE = { firstName: '', lastName: '', email: '', birthDate: '', shippingAddress: '' };

const validatePasswordForm = (values) => {
  const errors = {
    currentPassword: values.currentPassword ? null : 'La contraseña actual es obligatoria',
    newPassword: validatePassword(values.newPassword),
    confirmPassword: validatePasswordConfirmation(values.newPassword, values.confirmPassword),
  };
  return Object.fromEntries(Object.entries(errors).filter(([, v]) => v));
};

function PasswordForm() {
  const toast = useToast();
  const [serverError, setServerError] = useState('');
  const [saving, setSaving] = useState(false);
  const form = useForm({ currentPassword: '', newPassword: '', confirmPassword: '' }, validatePasswordForm);

  const onSubmit = async (event) => {
    event.preventDefault();
    setServerError('');
    if (!form.validateAll()) return;
    setSaving(true);
    try {
      await userApi.changePassword(form.values.currentPassword, form.values.newPassword);
      form.setValues({ currentPassword: '', newPassword: '', confirmPassword: '' });
      toast.success('Contraseña actualizada');
    } catch (error) {
      form.setServerErrors(error.fieldErrors);
      setServerError(error.message);
    } finally {
      setSaving(false);
    }
  };

  const field = (name) => ({
    name, value: form.values[name], onChange: form.handleChange, onBlur: form.handleBlur, error: form.errors[name],
  });

  return (
    <form className="card section-card" onSubmit={onSubmit} noValidate>
      <h2 className="section-card__title">Cambiar contraseña</h2>
      <Alert>{serverError}</Alert>
      <FormField label="Contraseña actual" type="password" autoComplete="current-password" {...field('currentPassword')} />
      <div className="form-grid">
        <FormField label="Nueva contraseña" type="password" autoComplete="new-password" {...field('newPassword')} />
        <FormField label="Confirmar nueva contraseña" type="password" autoComplete="new-password" {...field('confirmPassword')} />
      </div>
      <button type="submit" className="btn btn--primary" disabled={saving}>
        {saving ? 'Guardando…' : 'Actualizar contraseña'}
      </button>
    </form>
  );
}

export default function ProfilePage() {
  const { updateUser, logout } = useAuth();
  const { reset } = useCart();
  const toast = useToast();
  const navigate = useNavigate();
  const [profile, setProfile] = useState(null);
  const [editing, setEditing] = useState(false);
  const [serverError, setServerError] = useState('');
  const [saving, setSaving] = useState(false);
  const [confirmDelete, setConfirmDelete] = useState(false);
  const [deleting, setDeleting] = useState(false);
  const form = useForm(EMPTY_PROFILE, validateProfile);
  const { setValues } = form;

  useEffect(() => {
    userApi.me()
      .then((data) => {
        setProfile(data);
        setValues(pickEditable(data));
      })
      .catch((error) => setServerError(error.message));
  }, [setValues]);

  const onSubmit = async (event) => {
    event.preventDefault();
    setServerError('');
    if (!form.validateAll()) return;
    setSaving(true);
    try {
      const updated = await userApi.update({
        ...form.values,
        firstName: form.values.firstName.trim(),
        lastName: form.values.lastName.trim(),
        email: form.values.email.trim(),
        shippingAddress: form.values.shippingAddress.trim(),
      });
      setProfile(updated);
      updateUser(updated);
      setEditing(false);
      toast.success('Perfil actualizado correctamente');
    } catch (error) {
      form.setServerErrors(error.fieldErrors);
      setServerError(error.message);
    } finally {
      setSaving(false);
    }
  };

  const cancelEdit = () => {
    setValues(pickEditable(profile));
    form.setServerErrors(Object.fromEntries(Object.keys(EMPTY_PROFILE).map((key) => [key, null])));
    setServerError('');
    setEditing(false);
  };

  const deleteAccount = async () => {
    setDeleting(true);
    try {
      await userApi.remove();
      logout();
      reset();
      toast.success('Tu cuenta fue eliminada');
      navigate('/', { replace: true });
    } catch (error) {
      toast.error(error.message);
      setDeleting(false);
      setConfirmDelete(false);
    }
  };

  if (!profile) return serverError ? <Alert>{serverError}</Alert> : <Spinner label="Cargando perfil…" />;

  const field = (name) => ({
    name, value: form.values[name], onChange: form.handleChange, onBlur: form.handleBlur, error: form.errors[name],
  });

  return (
    <div className="page">
      <header className="page__header">
        <h1 className="page__title">Mi perfil</h1>
        <p className="page__subtitle">Cliente desde {formatDate(profile.createdAt.slice(0, 10))}</p>
      </header>

      <div className="profile-layout">
        <section className="card section-card">
          <div className="section-card__header">
            <h2 className="section-card__title">Datos personales</h2>
            {!editing && <button type="button" className="btn btn--ghost btn--sm" onClick={() => setEditing(true)}>Editar</button>}
          </div>
          <Alert>{serverError}</Alert>

          {editing ? (
            <form onSubmit={onSubmit} noValidate aria-label="Editar perfil">
              <div className="form-grid">
                <FormField label="Nombres" autoComplete="given-name" {...field('firstName')} />
                <FormField label="Apellidos" autoComplete="family-name" {...field('lastName')} />
                <FormField label="Correo electrónico" type="email" autoComplete="email" {...field('email')} />
                <FormField label="Fecha de nacimiento" type="date" max={maxBirthDate()} {...field('birthDate')} />
              </div>
              <FormField label="Dirección de envío" as="textarea" rows={2} {...field('shippingAddress')} />
              <div className="form-actions">
                <button type="button" className="btn btn--ghost" onClick={cancelEdit} disabled={saving}>Cancelar</button>
                <button type="submit" className="btn btn--primary" disabled={saving}>
                  {saving ? 'Guardando…' : 'Guardar cambios'}
                </button>
              </div>
            </form>
          ) : (
            <dl className="details">
              <div><dt>Nombres</dt><dd>{profile.firstName}</dd></div>
              <div><dt>Apellidos</dt><dd>{profile.lastName}</dd></div>
              <div><dt>Correo electrónico</dt><dd>{profile.email}</dd></div>
              <div><dt>Fecha de nacimiento</dt><dd>{formatDate(profile.birthDate)}</dd></div>
              <div className="details__wide"><dt>Dirección de envío</dt><dd>{profile.shippingAddress}</dd></div>
            </dl>
          )}
        </section>

        <PasswordForm />

        <section className="card section-card section-card--danger">
          <h2 className="section-card__title">Eliminar cuenta</h2>
          <p>Se eliminarán tus datos personales. Esta acción no se puede deshacer.</p>
          <button type="button" className="btn btn--danger" onClick={() => setConfirmDelete(true)}>Eliminar mi cuenta</button>
        </section>
      </div>

      <ConfirmDialog open={confirmDelete} title="¿Eliminar tu cuenta?" confirmLabel="Sí, eliminar" danger busy={deleting}
        onConfirm={deleteAccount} onCancel={() => setConfirmDelete(false)}>
        Perderás el acceso a tu carrito y a tu historial de pedidos.
      </ConfirmDialog>
    </div>
  );
}

function pickEditable(profile) {
  return {
    firstName: profile.firstName,
    lastName: profile.lastName,
    email: profile.email,
    birthDate: profile.birthDate,
    shippingAddress: profile.shippingAddress,
  };
}
