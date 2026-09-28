export default function StockBadge({ stock }) {
  if (stock <= 0) return <span className="stock stock--out">Agotado</span>;
  if (stock <= 5) return <span className="stock stock--low">¡Últimas {stock} unidades!</span>;
  return <span className="stock stock--ok">{stock} disponibles</span>;
}
