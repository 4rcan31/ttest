import { formatMoney } from '../utils/format.js';

const FREE_SHIPPING_THRESHOLD = 75;

/** Resumen de montos (subtotal, envío y total) reutilizado en carrito, checkout y órdenes. */
export default function OrderSummary({ subtotal, shippingCost, total, itemCount, showFreeShippingHint = false, children }) {
  const missing = FREE_SHIPPING_THRESHOLD - Number(subtotal);
  return (
    <aside className="card summary" aria-label="Resumen del pedido">
      <h2 className="summary__title">Resumen</h2>
      <dl className="summary__rows">
        <div><dt>Artículos ({itemCount})</dt><dd>{formatMoney(subtotal)}</dd></div>
        <div>
          <dt>Envío</dt>
          <dd>{Number(shippingCost) === 0 ? <span className="summary__free">Gratis</span> : formatMoney(shippingCost)}</dd>
        </div>
        <div className="summary__total"><dt>Total</dt><dd>{formatMoney(total)}</dd></div>
      </dl>
      {showFreeShippingHint && missing > 0 && Number(subtotal) > 0 && (
        <p className="summary__hint">Agrega {formatMoney(missing)} más para obtener envío gratis.</p>
      )}
      {children}
    </aside>
  );
}
