import { useCallback, useEffect, useState } from 'react';
import { adminApi } from '../api/services.js';
import Alert from '../components/Alert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { ReceiptIcon } from '../components/Icons.jsx';
import Pagination from '../components/Pagination.jsx';
import Spinner from '../components/Spinner.jsx';
import StatusBadge from '../components/StatusBadge.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { formatDateTime, formatMoney, STATUS_LABELS } from '../utils/format.js';

const NEXT = {
  CONFIRMED: ['PROCESSING', 'CANCELLED'],
  PROCESSING: ['SHIPPED', 'CANCELLED'],
  SHIPPED: ['DELIVERED'],
  DELIVERED: [],
  CANCELLED: [],
};

export default function AdminOrdersPage() {
  const toast = useToast();
  const [status, setStatus] = useState('');
  const [page, setPage] = useState(0);
  const [data, setData] = useState(null);
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(null);

  const load = useCallback(() => {
    setError('');
    return adminApi.orders(status, page).then(setData).catch((err) => setError(err.message));
  }, [status, page]);

  useEffect(() => {
    load();
  }, [load]);

  const changeStatus = async (orderNumber, next) => {
    setBusy(orderNumber);
    try {
      await adminApi.updateStatus(orderNumber, next);
      toast.success(`Orden ${orderNumber}: ${STATUS_LABELS[next]}`);
      await load();
    } catch (err) {
      toast.error(err.message);
    } finally {
      setBusy(null);
    }
  };

  return (
    <div className="page">
      <header className="page__header page__header--row">
        <div>
          <h1 className="page__title">Gestión de pedidos</h1>
          <p className="page__subtitle">Actualiza el estado de las órdenes de los clientes.</p>
        </div>
        <label className="select">
          <span className="sr-only">Filtrar por estado</span>
          <select value={status} onChange={(e) => { setStatus(e.target.value); setPage(0); }}>
            <option value="">Todos los estados</option>
            {Object.entries(STATUS_LABELS).map(([value, label]) => <option key={value} value={value}>{label}</option>)}
          </select>
        </label>
      </header>

      <Alert>{error}</Alert>
      {!data && !error && <Spinner />}
      {data && data.content.length === 0 && (
        <EmptyState icon={<ReceiptIcon size={40} />} title="No hay pedidos con este filtro" />
      )}
      {data && data.content.length > 0 && (
        <div className="card table-wrapper">
          <table className="table">
            <thead>
              <tr>
                <th scope="col">Orden</th><th scope="col">Cliente</th><th scope="col">Fecha</th>
                <th scope="col">Total</th><th scope="col">Estado</th><th scope="col">Acciones</th>
              </tr>
            </thead>
            <tbody>
              {data.content.map((order) => (
                <tr key={order.orderNumber}>
                  <td data-label="Orden"><strong>{order.orderNumber}</strong></td>
                  <td data-label="Cliente">{order.customerName}</td>
                  <td data-label="Fecha">{formatDateTime(order.createdAt)}</td>
                  <td data-label="Total">{formatMoney(order.total)}</td>
                  <td data-label="Estado"><StatusBadge status={order.status} /></td>
                  <td data-label="Acciones">
                    <div className="table__actions">
                      {NEXT[order.status].length === 0 && <span className="muted">Sin acciones</span>}
                      {NEXT[order.status].map((next) => (
                        <button key={next} type="button" disabled={busy === order.orderNumber}
                          className={`btn btn--sm ${next === 'CANCELLED' ? 'btn--ghost-danger' : 'btn--ghost'}`}
                          onClick={() => changeStatus(order.orderNumber, next)}>
                          {next === 'CANCELLED' ? 'Cancelar' : `Marcar ${STATUS_LABELS[next].toLowerCase()}`}
                        </button>
                      ))}
                    </div>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
      {data && <Pagination page={data.page} totalPages={data.totalPages} onChange={setPage} />}
    </div>
  );
}
