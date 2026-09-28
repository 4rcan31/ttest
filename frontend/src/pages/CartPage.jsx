import { useState } from 'react';
import { Link, useNavigate } from 'react-router';
import Alert from '../components/Alert.jsx';
import ConfirmDialog from '../components/ConfirmDialog.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { CartIcon, TrashIcon } from '../components/Icons.jsx';
import OrderSummary from '../components/OrderSummary.jsx';
import QuantitySelector from '../components/QuantitySelector.jsx';
import Spinner from '../components/Spinner.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { formatMoney } from '../utils/format.js';

export default function CartPage() {
  const { cart, loading, updateItem, removeItem, clear } = useCart();
  const toast = useToast();
  const navigate = useNavigate();
  const [busyId, setBusyId] = useState(null);
  const [confirmClear, setConfirmClear] = useState(false);

  const run = async (productId, action, successMessage) => {
    setBusyId(productId);
    try {
      await action();
      if (successMessage) toast.success(successMessage);
    } catch (error) {
      toast.error(error.message);
    } finally {
      setBusyId(null);
    }
  };

  const onClear = async () => {
    setConfirmClear(false);
    await run('all', clear, 'Carrito vaciado');
  };

  if (loading && cart.items.length === 0) return <Spinner label="Cargando carrito…" />;

  if (cart.items.length === 0) {
    return (
      <EmptyState icon={<CartIcon size={44} />} title="Tu carrito está vacío"
        action={<Link to="/" className="btn btn--primary">Explorar el catálogo</Link>}>
        Agrega artículos desde el catálogo para verlos aquí.
      </EmptyState>
    );
  }

  return (
    <div className="page">
      <header className="page__header page__header--row">
        <div>
          <h1 className="page__title">Mi carrito</h1>
          <p className="page__subtitle">{cart.totalItems} {cart.totalItems === 1 ? 'artículo' : 'artículos'}</p>
        </div>
        <button type="button" className="btn btn--ghost btn--sm" onClick={() => setConfirmClear(true)} disabled={busyId !== null}>
          <TrashIcon size={16} /> Vaciar carrito
        </button>
      </header>

      {!cart.readyForCheckout && (
        <Alert type="info">Algunos artículos no tienen inventario suficiente. Ajusta las cantidades o elimínalos para continuar.</Alert>
      )}

      <div className="checkout-layout">
        <ul className="cart-list">
          {cart.items.map((item) => (
            <li key={item.productId} className={`cart-item ${item.available ? '' : 'cart-item--unavailable'}`}>
              {item.imageUrl
                ? <img src={item.imageUrl} alt="" className="cart-item__image" width="96" height="96" />
                : <div className="cart-item__image cart-item__image--placeholder" />}
              <div className="cart-item__info">
                {item.sku ? <Link to={`/productos/${item.productId}`} className="cart-item__name">{item.name}</Link>
                  : <span className="cart-item__name">{item.name}</span>}
                <span className="cart-item__price">{formatMoney(item.unitPrice)} c/u</span>
                {!item.available && (
                  <span className="stock stock--out">
                    {item.availableStock > 0 ? `Solo quedan ${item.availableStock}` : 'Sin inventario'}
                  </span>
                )}
              </div>
              <div className="cart-item__controls">
                {item.sku && (
                  <QuantitySelector value={item.quantity} max={Math.max(1, Math.min(10, item.availableStock))}
                    disabled={busyId !== null} label={`Cantidad de ${item.name}`}
                    onChange={(quantity) => run(item.productId, () => updateItem(item.productId, quantity))} />
                )}
                <strong className="cart-item__total">{formatMoney(item.lineTotal)}</strong>
                <button type="button" className="icon-button icon-button--danger" disabled={busyId !== null}
                  onClick={() => run(item.productId, () => removeItem(item.productId), `${item.name} se eliminó del carrito`)}
                  aria-label={`Eliminar ${item.name} del carrito`}>
                  <TrashIcon size={18} />
                </button>
              </div>
            </li>
          ))}
        </ul>

        <OrderSummary subtotal={cart.subtotal} shippingCost={cart.shippingCost} total={cart.total}
          itemCount={cart.totalItems} showFreeShippingHint>
          <button type="button" className="btn btn--primary btn--block btn--lg" disabled={!cart.readyForCheckout}
            onClick={() => navigate('/checkout')}>
            Continuar con el pedido
          </button>
          <Link to="/" className="btn btn--ghost btn--block">Seguir comprando</Link>
        </OrderSummary>
      </div>

      <ConfirmDialog open={confirmClear} title="¿Vaciar el carrito?" confirmLabel="Vaciar" danger
        onConfirm={onClear} onCancel={() => setConfirmClear(false)}>
        Se eliminarán todos los artículos del carrito.
      </ConfirmDialog>
    </div>
  );
}
