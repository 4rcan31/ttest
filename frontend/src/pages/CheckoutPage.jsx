import { useEffect, useState } from 'react';
import { Link, Navigate, useNavigate } from 'react-router';
import { orderApi, userApi } from '../api/services.js';
import Alert from '../components/Alert.jsx';
import FormField from '../components/FormField.jsx';
import { EditIcon, PinIcon } from '../components/Icons.jsx';
import OrderSummary from '../components/OrderSummary.jsx';
import Spinner from '../components/Spinner.jsx';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';
import { formatMoney } from '../utils/format.js';
import { validateAddress } from '../utils/validators.js';

export default function CheckoutPage() {
  const { cart, refresh } = useCart();
  const { user, updateUser } = useAuth();
  const toast = useToast();
  const navigate = useNavigate();

  const [address, setAddress] = useState(null);
  const [editing, setEditing] = useState(false);
  const [draft, setDraft] = useState('');
  const [draftError, setDraftError] = useState('');
  const [saveAsDefault, setSaveAsDefault] = useState(true);
  const [savingAddress, setSavingAddress] = useState(false);
  const [placing, setPlacing] = useState(false);
  const [error, setError] = useState('');
  const [loaded, setLoaded] = useState(false);

  useEffect(() => {
    Promise.all([userApi.me(), refresh()])
      .then(([profile]) => setAddress(profile.shippingAddress))
      .catch((err) => setError(err.message))
      .finally(() => setLoaded(true));
  }, [refresh]);

  if (!loaded) return <Spinner label="Preparando tu pedido…" />;
  if (!placing && cart.items.length === 0) return <Navigate to="/carrito" replace />;

  const startEdit = () => {
    setDraft(address ?? '');
    setDraftError('');
    setEditing(true);
  };

  const applyAddress = async () => {
    const validation = validateAddress(draft);
    if (validation) {
      setDraftError(validation);
      return;
    }
    const newAddress = draft.trim();
    if (saveAsDefault && newAddress !== address) {
      setSavingAddress(true);
      try {
        const updated = await userApi.updateAddress(newAddress);
        updateUser(updated);
        toast.success('Dirección guardada en tu perfil');
      } catch (err) {
        setDraftError(err.fieldErrors?.shippingAddress ?? err.message);
        setSavingAddress(false);
        return;
      }
      setSavingAddress(false);
    }
    setAddress(newAddress);
    setEditing(false);
  };

  const placeOrder = async () => {
    setError('');
    setPlacing(true);
    try {
      const order = await orderApi.create(address);
      await refresh();
      navigate(`/pedido-confirmado/${order.orderNumber}`, { replace: true, state: { order } });
    } catch (err) {
      setError(err.message);
      setPlacing(false);
      refresh().catch(() => {});
    }
  };

  return (
    <div className="page">
      <header className="page__header">
        <h1 className="page__title">Confirmar pedido</h1>
        <p className="page__subtitle">Revisa la dirección de envío y los artículos antes de confirmar.</p>
      </header>

      <Alert>{error}</Alert>

      <div className="checkout-layout">
        <div className="checkout-main">
          <section className="card section-card" aria-labelledby="shipping-title">
            <div className="section-card__header">
              <h2 id="shipping-title" className="section-card__title"><PinIcon size={18} /> Dirección de envío</h2>
              {!editing && (
                <button type="button" className="btn btn--ghost btn--sm" onClick={startEdit}>
                  <EditIcon size={16} /> Editar
                </button>
              )}
            </div>

            {editing ? (
              <div>
                <FormField label="Dirección de envío" name="shippingAddress" as="textarea" rows={3} value={draft}
                  onChange={(e) => { setDraft(e.target.value); setDraftError(''); }} error={draftError} />
                <label className="checkbox">
                  <input type="checkbox" checked={saveAsDefault} onChange={(e) => setSaveAsDefault(e.target.checked)} />
                  Guardar también como mi dirección principal
                </label>
                <div className="form-actions">
                  <button type="button" className="btn btn--ghost" onClick={() => setEditing(false)} disabled={savingAddress}>Cancelar</button>
                  <button type="button" className="btn btn--primary" onClick={applyAddress} disabled={savingAddress}>
                    {savingAddress ? 'Guardando…' : 'Usar esta dirección'}
                  </button>
                </div>
              </div>
            ) : (
              <div className="address">
                <p className="address__name">{user.firstName} {user.lastName}</p>
                <p className="address__line">{address}</p>
              </div>
            )}
          </section>

          <section className="card section-card" aria-labelledby="items-title">
            <h2 id="items-title" className="section-card__title">Artículos ({cart.totalItems})</h2>
            <ul className="review-list">
              {cart.items.map((item) => (
                <li key={item.productId} className="review-item">
                  <img src={item.imageUrl} alt="" width="56" height="56" />
                  <span className="review-item__name">{item.name}<small>{item.quantity} × {formatMoney(item.unitPrice)}</small></span>
                  <strong>{formatMoney(item.lineTotal)}</strong>
                </li>
              ))}
            </ul>
            <Link to="/carrito" className="link">Modificar carrito</Link>
          </section>
        </div>

        <OrderSummary subtotal={cart.subtotal} shippingCost={cart.shippingCost} total={cart.total} itemCount={cart.totalItems}>
          <button type="button" className="btn btn--primary btn--block btn--lg" onClick={placeOrder}
            disabled={placing || editing || !cart.readyForCheckout || !address}>
            {placing ? 'Confirmando…' : `Confirmar pedido · ${formatMoney(cart.total)}`}
          </button>
          <p className="summary__legal">El pago se realiza contra entrega.</p>
        </OrderSummary>
      </div>
    </div>
  );
}
