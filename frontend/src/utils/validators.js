// Mismas reglas que valida el backend (user-service/ValidationRules): el frontend da feedback
// inmediato, pero la validación autoritativa siempre ocurre en el servidor.

export const NAME_PATTERN = /^[\p{L}][\p{L} '.-]*$/u;
export const EMAIL_PATTERN = /^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$/;
export const PASSWORD_PATTERN = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d).{8,72}$/;
export const MIN_AGE = 18;

export function calculateAge(isoDate, today = new Date()) {
  const [year, month, day] = isoDate.split('-').map(Number);
  let age = today.getFullYear() - year;
  const beforeBirthday =
    today.getMonth() + 1 < month || (today.getMonth() + 1 === month && today.getDate() < day);
  if (beforeBirthday) age -= 1;
  return age;
}

export function validateName(value, label) {
  const text = value?.trim() ?? '';
  if (!text) return `${label} son obligatorios`;
  if (text.length < 2 || text.length > 80) return `${label} deben tener entre 2 y 80 caracteres`;
  if (!NAME_PATTERN.test(text)) return `${label} solo pueden contener letras`;
  return null;
}

export function validateEmail(value) {
  const text = value?.trim() ?? '';
  if (!text) return 'El correo electrónico es obligatorio';
  if (text.length > 120 || !EMAIL_PATTERN.test(text)) return 'El formato del correo electrónico no es válido';
  return null;
}

export function validateAddress(value) {
  const text = value?.trim() ?? '';
  if (!text) return 'La dirección de envío es obligatoria';
  if (text.length < 10 || text.length > 255) return 'La dirección de envío debe tener entre 10 y 255 caracteres';
  return null;
}

export function validateBirthDate(value, today = new Date()) {
  if (!value) return 'La fecha de nacimiento es obligatoria';
  if (!/^\d{4}-\d{2}-\d{2}$/.test(value)) return 'La fecha de nacimiento no es válida';
  const age = calculateAge(value, today);
  if (age < MIN_AGE) return 'Debe ser mayor de 18 años';
  if (age > 120) return 'La fecha de nacimiento no es válida';
  return null;
}

export function validatePassword(value) {
  if (!value) return 'La contraseña es obligatoria';
  if (!PASSWORD_PATTERN.test(value)) {
    return 'La contraseña debe tener entre 8 y 72 caracteres e incluir mayúscula, minúscula y número';
  }
  return null;
}

export function validatePasswordConfirmation(password, confirmation) {
  if (!confirmation) return 'Confirme la contraseña';
  if (password !== confirmation) return 'Las contraseñas no coinciden';
  return null;
}

/** Requisitos individuales para mostrar una guía visual mientras el usuario escribe. */
export function passwordChecks(value = '') {
  return [
    { label: 'Mínimo 8 caracteres', ok: value.length >= 8 },
    { label: 'Una letra mayúscula', ok: /[A-Z]/.test(value) },
    { label: 'Una letra minúscula', ok: /[a-z]/.test(value) },
    { label: 'Un número', ok: /\d/.test(value) },
  ];
}

export function validateProfile(values) {
  const errors = {
    firstName: validateName(values.firstName, 'Los nombres'),
    lastName: validateName(values.lastName, 'Los apellidos'),
    shippingAddress: validateAddress(values.shippingAddress),
    email: validateEmail(values.email),
    birthDate: validateBirthDate(values.birthDate),
  };
  return Object.fromEntries(Object.entries(errors).filter(([, message]) => message));
}

export function validateRegistration(values) {
  const errors = {
    ...validateProfile(values),
    password: validatePassword(values.password),
    confirmPassword: validatePasswordConfirmation(values.password, values.confirmPassword),
  };
  return Object.fromEntries(Object.entries(errors).filter(([, message]) => message));
}

/** Fecha máxima seleccionable (hoy menos 18 años) para el control de fecha de nacimiento. */
export function maxBirthDate(today = new Date()) {
  const date = new Date(today.getFullYear() - MIN_AGE, today.getMonth(), today.getDate());
  return toIsoDate(date);
}

function toIsoDate(date) {
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  return `${date.getFullYear()}-${month}-${day}`;
}
