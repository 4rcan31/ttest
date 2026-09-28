import { useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router';
import { orderApi } from '../api/services.js';
import Alert from '../components/Alert.jsx';
import { CheckIcon } from '../components/Icons.jsx';
import Spinner from '../components/Spinner.jsx';
import { formatDateTime, formatMoney } from '../utils/format.js';

export default function OrderConfirmationPage() {
  const { orderNumber } = useParams();
  const location = useLocation();
  const [order, setOrder] = useState(location.state?.order ?? null);
  const [error, setError] = useState('');

  useEffect(() => {
    if (!order) orderApi.get(orderNumber).then(setOrder).catch((err) => setError(err.message));
  }, [order, orderNumber]);

  if (error) return <Alert>{error}</Alert>;
  if (!order) return <Spinner />;

  return (
    <div className="confirmation">
      <div className="confirmation__icon"><CheckIcon size={40} /></div>
      <h1 className="confirmation__title">¡Pedido confirmado!</h1>
      <p className="confirmation__text">Gracias por tu compra, {order.customerName.split(' ')[0]}. Te avisaremos cuando tu pedido sea enviado.</p>

      <div className="card confirmation__card">
        <p className="confirmation__label">Número de orden</p>
        <p className="confirmation__number">{order.orderNumber}</p>
        <dl className="details details--compact">
          <div><dt>Fecha</dt><dd>{formatDateTime(order.createdAt)}</dd></div>
          <div><dt>Estado</dt><dd>{order.statusLabel}</dd></div>
          <div><dt>Artículos</dt><dd>{order.itemCount}</dd></div>
          <div><dt>Total</dt><dd><strong>{formatMoney(order.total)}</strong></dd></div>
          <div className="details__wide"><dt>Envío a</dt><dd>{order.shippingAddress}</dd></div>
        </dl>
      </div>

      <div className="confirmation__actions">
        <Link to={`/pedidos/${order.orderNumber}`} className="btn btn--primary">Ver detalle del pedido</Link>
        <Link to="/" className="btn btn--ghost">Seguir comprando</Link>
      </div>
    </div>
  );
}
