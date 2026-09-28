import { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router';
import { productApi } from '../api/services.js';
import Alert from '../components/Alert.jsx';
import { ArrowLeftIcon, CartIcon, TruckIcon } from '../components/Icons.jsx';
import QuantitySelector from '../components/QuantitySelector.jsx';
import Spinner from '../components/Spinner.jsx';
import StockBadge from '../components/StockBadge.jsx';
import { formatMoney } from '../utils/format.js';
import useAddToCart from '../utils/useAddToCart.js';

export default function ProductDetailPage() {
  const { id } = useParams();
  const [product, setProduct] = useState(null);
  const [error, setError] = useState('');
  const [quantity, setQuantity] = useState(1);
  const { add, addingId } = useAddToCart();

  useEffect(() => {
    setProduct(null);
    setError('');
    setQuantity(1);
    productApi.get(id).then(setProduct).catch((err) => setError(err.message));
  }, [id]);

  if (error) {
    return (
      <>
        <Link to="/" className="back-link"><ArrowLeftIcon size={18} /> Volver al catálogo</Link>
        <Alert>{error}</Alert>
      </>
    );
  }
  if (!product) return <Spinner label="Cargando artículo…" />;

  const maxQuantity = Math.min(product.stock, 10);

  return (
    <>
      <Link to="/" className="back-link"><ArrowLeftIcon size={18} /> Volver al catálogo</Link>
      <article className="product-detail">
        <div className="product-detail__media">
          <img src={product.imageUrl} alt={product.name} width="400" height="400" />
        </div>
        <div className="product-detail__info">
          <p className="product-detail__meta">{product.category.name} · {product.brand} · SKU {product.sku}</p>
          <h1 className="product-detail__title">{product.name}</h1>
          <p className="product-detail__price">{formatMoney(product.price)}</p>
          <StockBadge stock={product.stock} />
          <p className="product-detail__description">{product.description}</p>

          {product.available ? (
            <div className="product-detail__actions">
              <QuantitySelector value={quantity} max={maxQuantity} onChange={setQuantity} />
              <button type="button" className="btn btn--primary btn--lg"
                onClick={() => add(product.id, quantity, product.name)} disabled={addingId === product.id}>
                <CartIcon size={18} /> {addingId === product.id ? 'Agregando…' : 'Agregar al carrito'}
              </button>
            </div>
          ) : (
            <Alert type="info">Este artículo está agotado por el momento.</Alert>
          )}
          <p className="product-detail__note"><TruckIcon size={18} /> Envío gratis en compras desde $75.00</p>
        </div>
      </article>
    </>
  );
}
