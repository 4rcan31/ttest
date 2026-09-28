import { MinusIcon, PlusIcon } from './Icons.jsx';

export default function QuantitySelector({ value, min = 1, max = 10, onChange, disabled, label = 'Cantidad' }) {
  return (
    <div className="quantity" role="group" aria-label={label}>
      <button type="button" className="quantity__btn" onClick={() => onChange(value - 1)}
        disabled={disabled || value <= min} aria-label="Disminuir cantidad">
        <MinusIcon size={16} />
      </button>
      <span className="quantity__value" aria-live="polite">{value}</span>
      <button type="button" className="quantity__btn" onClick={() => onChange(value + 1)}
        disabled={disabled || value >= max} aria-label="Aumentar cantidad">
        <PlusIcon size={16} />
      </button>
    </div>
  );
}
