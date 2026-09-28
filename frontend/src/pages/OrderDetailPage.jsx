import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { orderApi } from '../api/services.js';
import Alert from '../components/Alert.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import { ArrowLeftIcon, PinIcon } from '../components/Icons.jsx';
import OrderSummary from '../components/OrderSummary.jsx';
import OrderTimeline from '../components/OrderTimeline.jsx';
import Spinner from '../components/Spinner.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { formatDateTime, formatMoney } from '../utils/format.js';

export default function OrderDetailPage() {
  const { orderNumber } = useParams();
  const toast = useToast();
  const [order, setOrder] = useState(null);
  const [error, setError] = useState('');
  const [confirmCancel, setConfirmCancel] = useState(false);
  const [cancelling, setCancelling] = useState(false);

  useEffect(() => {
    orderApi.get(orderNumber).then(setOrder).catch((err) => setError(err.message));
  }, [orderNumber]);

  const cancel = async () => {
    setCancelling(true);
    try {
      setOrder(await orderApi.cancel(orderNumber));
      toast.success('El pedido fue cancelado');
    } catch (err) {
      toast.error(err.message);
    } finally {
      setCancelling(false);
      setConfirmCancel(false);
    }
  };

  const back = <Link to="/pedidos" className="back-link"><ArrowLeftIcon size={18} /> Mis pedidos</Link>;
  if (error) return <>{back}<Alert>{error}</Alert></>;
  if (!order) return <Spinner label="Cargando pedido…" />;

  return (
    <div className="page">
      {back}
      <header className="page__header page__header--row">
        <div>
          <h1 className="page__title">Pedido {order.orderNumber}</h1>
          <p className="page__subtitle">Realizado el {formatDateTime(order.createdAt)}</p>
        </div>
        <StatusBadge status={order.status} />
      </header>

      <section className="card section-card">
        <h2 className="section-card__title">Estado del pedido</h2>
        <OrderTimeline status={order.status} history={order.history} />
      </section>

      <div className="checkout-layout">
        <div className="checkout-main">
          <section className="card section-card">
            <h2 className="section-card__title">Artículos comprados</h2>
            <div className="table-wrapper">
              <table className="table">
                <thead>
                  <tr><th scope="col">Artículo</th><th scope="col">Precio</th><th scope="col">Cantidad</th><th scope="col">Subtotal</th></tr>
                </thead>
                <tbody>
                  {order.items.map((item) => (
                    <tr key={item.productId}>
                      <td data-label="Artículo">
                        <span className="table__product">
                          <img src={item.imageUrl} alt="" width="44" height="44" />
                          <span>{item.name}<small>SKU {item.sku}</small></span>
                        </span>
                      </td>
                      <td data-label="Precio">{formatMoney(item.unitPrice)}</td>
                      <td data-label="Cantidad">{item.quantity}</td>
                      <td data-label="Subtotal"><strong>{formatMoney(item.lineTotal)}</strong></td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </section>

          <section className="card section-card">
            <h2 className="section-card__title"><PinIcon size={18} /> Dirección de envío</h2>
            <p className="address__name">{order.customerName}</p>
            <p className="address__line">{order.shippingAddress}</p>
          </section>
        </div>

        <OrderSummary subtotal={order.subtotal} shippingCost={order.shippingCost} total={order.total} itemCount={order.itemCount}>
          {order.cancellable && (
            <button type="button" className="btn btn--danger btn--block" onClick={() => setConfirmCancel(true)}>
              Cancelar pedido
            </button>
          )}
        </OrderSummary>
      </div>

      <ConfirmDialog open={confirmCancel} title="¿Cancelar este pedido?" confirmLabel="Sí, cancelar" cancelLabel="No"
        danger busy={cancelling} onConfirm={cancel} onCancel={() => setConfirmCancel(false)}>
        Los artículos volverán al inventario. Esta acción no se puede deshacer.
      </ConfirmDialog>
    </div>
  );
}
