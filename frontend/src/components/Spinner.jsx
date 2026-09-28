export default function Spinner({ label = 'Cargando…', inline = false }) {
  return (
    <div className={inline ? 'spinner spinner--inline' : 'spinner'} role="status">
      <span className="spinner__circle" aria-hidden="true" />
      <span className={inline ? 'sr-only' : 'spinner__label'}>{label}</span>
    </div>
  );
}
