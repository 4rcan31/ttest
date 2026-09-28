import { Link } from 'react-router';
import { formatMoney } from '../utils/format.js';
import { CartIcon } from './Icons.jsx';
import StockBadge from './StockBadge.jsx';

export default function ProductCard({ product, onAdd, adding }) {
  return (
    <article className="product-card">
      <Link to={`/productos/${product.id}`} className="product-card__media" aria-label={`Ver ${product.name}`}>
        <img src={product.imageUrl} alt={product.name} loading="lazy" width="400" height="400" />
        <span className="product-card__category">{product.category.name}</span>
      </Link>
      <div className="product-card__body">
        <p className="product-card__brand">{product.brand}</p>
        <h3 className="product-card__title">
          <Link to={`/productos/${product.id}`}>{product.name}</Link>
        </h3>
        <p className="product-card__description">{product.description}</p>
        <div className="product-card__footer">
          <div>
            <p className="product-card__price">{formatMoney(product.price)}</p>
            <StockBadge stock={product.stock} />
          </div>
          <button type="button" className="btn btn--primary btn--sm" onClick={() => onAdd(product)}
            disabled={!product.available || adding} aria-label={`Agregar ${product.name} al carrito`}>
            <CartIcon size={16} /> {adding ? 'Agregando…' : 'Agregar'}
          </button>
        </div>
      </div>
    </article>
  );
}
