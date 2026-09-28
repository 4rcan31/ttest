import { ChevronLeftIcon, ChevronRightIcon } from './Icons.jsx';

export default function Pagination({ page, totalPages, onChange }) {
  if (totalPages <= 1) return null;
  return (
    <nav className="pagination" aria-label="Paginación">
      <button type="button" className="btn btn--ghost btn--sm" onClick={() => onChange(page - 1)}
        disabled={page === 0} aria-label="Página anterior">
        <ChevronLeftIcon size={18} /> Anterior
      </button>
      <span className="pagination__info">Página {page + 1} de {totalPages}</span>
      <button type="button" className="btn btn--ghost btn--sm" onClick={() => onChange(page + 1)}
        disabled={page >= totalPages - 1} aria-label="Página siguiente">
        Siguiente <ChevronRightIcon size={18} />
      </button>
    </nav>
  );
}
