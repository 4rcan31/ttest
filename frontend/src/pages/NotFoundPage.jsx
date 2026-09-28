import { Link } from 'react-router';
import EmptyState from '../components/EmptyState.jsx';

export default function NotFoundPage() {
  return (
    <EmptyState title="Página no encontrada" action={<Link to="/" className="btn btn--primary">Volver al catálogo</Link>}>
      La página que buscas no existe o fue movida.
    </EmptyState>
  );
}
