const money = new Intl.NumberFormat('es-SV', { style: 'currency', currency: 'USD' });
const dateTime = new Intl.DateTimeFormat('es', { dateStyle: 'medium', timeStyle: 'short' });
const dateOnly = new Intl.DateTimeFormat('es', { dateStyle: 'long', timeZone: 'UTC' });

export const formatMoney = (value) => money.format(Number(value ?? 0));
export const formatDateTime = (iso) => (iso ? dateTime.format(new Date(iso)) : '');
export const formatDate = (isoDate) => (isoDate ? dateOnly.format(new Date(`${isoDate}T00:00:00Z`)) : '');

export const STATUS_LABELS = {
  CONFIRMED: 'Confirmada',
  PROCESSING: 'En preparación',
  SHIPPED: 'Enviada',
  DELIVERED: 'Entregada',
  CANCELLED: 'Cancelada',
};
