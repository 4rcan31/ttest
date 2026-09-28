import { useEffect, useState } from 'react';
import { Link } from 'react-router';
import { orderApi } from '../api/services.js';
import Alert from '../components/Alert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { ReceiptIcon } from '../components/Icons.jsx';
import Pagination from '../components/Pagination.jsx';
import Spinner from '../components/Spinner.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { formatDateTime, formatMoney } from '../utils/format.js';

export default function OrdersPage() {
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    setError('');
    orderApi.list(page, 10).then(setData).catch((err) => setError(err.message));
  }, [page]);

  if (error) return <Alert>{error}</Alert>;
  if (!data) return <Spinner label="Cargando pedidos…" />;

  if (data.totalElements === 0) {
    return (
      <EmptyState icon={<ReceiptIcon size={44} />} title="Aún no tienes pedidos"
        action={<Link to="/" className="btn btn--primary">Ir al catálogo</Link>}>
        Cuando confirmes un pedido podrás ver aquí su estado y detalle.
      </EmptyState>
    );
  }

  return (
    <div className="page">
      <header className="page__header">
        <h1 className="page__title">Mis pedidos</h1>
        <p className="page__subtitle">{data.totalElements} {data.totalElements === 1 ? 'pedido' : 'pedidos'} realizados</p>
      </header>

      <ul className="order-list">
        {data.content.map((order) => (
          <li key={order.orderNumber}>
            <Link to={`/pedidos/${order.orderNumber}`} className="card order-card">
              <div className="order-card__head">
                <span className="order-card__number">{order.orderNumber}</span>
                <StatusBadge status={order.status} />
              </div>
              <div className="order-card__body">
                <div className="order-card__images" aria-hidden="true">
                  {order.previewImages.map((src, index) => <img key={`${src}-${index}`} src={src} alt="" width="48" height="48" />)}
                </div>
                <div className="order-card__meta">
                  <span>{formatDateTime(order.createdAt)}</span>
                  <span>{order.itemCount} {order.itemCount === 1 ? 'artículo' : 'artículos'}</span>
                </div>
                <strong className="order-card__total">{formatMoney(order.total)}</strong>
              </div>
            </Link>
          </li>
        ))}
      </ul>
      <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />
    </div>
  );
}
