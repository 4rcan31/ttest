import { formatDateTime, STATUS_LABELS } from '../utils/format.js';
import { BoxIcon, CheckIcon, CloseIcon, TruckIcon, ReceiptIcon } from './Icons.jsx';

const FLOW = [
  { status: 'CONFIRMED', icon: ReceiptIcon },
  { status: 'PROCESSING', icon: BoxIcon },
  { status: 'SHIPPED', icon: TruckIcon },
  { status: 'DELIVERED', icon: CheckIcon },
];

/** Línea de tiempo del estado del pedido construida a partir del historial de cambios. */
export default function OrderTimeline({ status, history }) {
  const reachedAt = Object.fromEntries(history.map((entry) => [entry.status, entry.changedAt]));

  if (status === 'CANCELLED') {
    return (
      <div className="timeline timeline--cancelled">
        <div className="timeline__cancelled">
          <span className="timeline__icon"><CloseIcon size={18} /></span>
          <div>
            <strong>Pedido cancelado</strong>
            <p>{formatDateTime(reachedAt.CANCELLED)} · El inventario reservado fue liberado.</p>
          </div>
        </div>
      </div>
    );
  }

  const currentIndex = FLOW.findIndex((step) => step.status === status);
  return (
    <ol className="timeline" aria-label="Estado del pedido">
      {FLOW.map(({ status: stepStatus, icon: StepIcon }, index) => {
        const state = index < currentIndex ? 'done' : index === currentIndex ? 'current' : 'pending';
        return (
          <li key={stepStatus} className={`timeline__step timeline__step--${state}`}
            aria-current={state === 'current' ? 'step' : undefined}>
            <span className="timeline__icon"><StepIcon size={18} /></span>
            <span className="timeline__label">{STATUS_LABELS[stepStatus]}</span>
            {reachedAt[stepStatus] && <span className="timeline__date">{formatDateTime(reachedAt[stepStatus])}</span>}
          </li>
        );
      })}
    </ol>
  );
}
