import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router';
import { productApi } from '../api/services.js';
import Alert from '../components/Alert.jsx';
import EmptyState from '../components/EmptyState.jsx';
import { SearchIcon, TruckIcon, ShieldIcon, BoxIcon } from '../components/Icons.jsx';
import Pagination from '../components/Pagination.jsx';
import ProductCard from '../components/ProductCard.jsx';
import Spinner from '../components/Spinner.jsx';
import useAddToCart from '../utils/useAddToCart.js';

const SORT_OPTIONS = [
  { value: 'featured', label: 'Destacados' },
  { value: 'price_asc', label: 'Precio: menor a mayor' },
  { value: 'price_desc', label: 'Precio: mayor a menor' },
  { value: 'name', label: 'Nombre (A-Z)' },
];

export default function CatalogPage() {
  const [params, setParams] = useSearchParams();
  const q = params.get('q') ?? '';
  const category = params.get('categoria') ?? '';
  const sort = params.get('orden') ?? 'featured';
  const inStock = params.get('disponibles') === '1';
  const page = Number(params.get('pagina') ?? 0);

  const [searchText, setSearchText] = useState(q);
  const [categories, setCategories] = useState([]);
  const [result, setResult] = useState(null);
  const [error, setError] = useState('');
  const { add, addingId } = useAddToCart();

  useEffect(() => setSearchText(q), [q]);

  useEffect(() => {
    productApi.categories().then(setCategories).catch(() => setCategories([]));
  }, []);

  useEffect(() => {
    const controller = new AbortController();
    setError('');
    productApi.search({ q, category, sort, inStock, page, size: 12 }, controller.signal)
      .then(setResult)
      .catch((err) => {
        if (err.name !== 'AbortError') setError(err.message);
      });
    return () => controller.abort();
  }, [q, category, sort, inStock, page]);

  const updateParams = (changes) => {
    const next = new URLSearchParams(params);
    Object.entries(changes).forEach(([key, value]) => {
      if (value === '' || value === null || value === false || value === undefined) next.delete(key);
      else next.set(key, value);
    });
    if (!('pagina' in changes)) next.delete('pagina');
    setParams(next);
  };

  const onSearch = (event) => {
    event.preventDefault();
    updateParams({ q: searchText.trim() });
  };

  const changePage = (nextPage) => {
    updateParams({ pagina: nextPage || '' });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  return (
    <>
      <section className="hero">
        <div className="hero__content">
          <p className="hero__eyebrow">Temporada 2026</p>
          <h1 className="hero__title">Equípate para dar lo mejor en cada entrenamiento</h1>
          <p className="hero__text">Artículos para fútbol, running, básquetbol, tenis, fitness y más.</p>
          <form className="search" role="search" onSubmit={onSearch}>
            <SearchIcon className="search__icon" />
            <input type="search" className="search__input" placeholder="Buscar balones, zapatillas, raquetas…"
              value={searchText} onChange={(e) => setSearchText(e.target.value)} aria-label="Buscar artículos" />
            <button type="submit" className="btn btn--accent">Buscar</button>
          </form>
        </div>
        <ul className="hero__perks">
          <li><TruckIcon /> Envío gratis desde $75</li>
          <li><ShieldIcon /> Compra segura</li>
          <li><BoxIcon /> Inventario en tiempo real</li>
        </ul>
      </section>

      <section className="filters" aria-label="Filtros">
        <div className="chips" role="group" aria-label="Categorías">
          <button type="button" className={`chip ${category === '' ? 'chip--active' : ''}`}
            aria-pressed={category === ''} onClick={() => updateParams({ categoria: '' })}>Todos</button>
          {categories.map((cat) => (
            <button key={cat.slug} type="button"
              className={`chip ${category === cat.slug ? 'chip--active' : ''}`}
              aria-pressed={category === cat.slug}
              onClick={() => updateParams({ categoria: cat.slug })}>
              {cat.name}
            </button>
          ))}
        </div>
        <div className="filters__row">
          <label className="checkbox">
            <input type="checkbox" checked={inStock} onChange={(e) => updateParams({ disponibles: e.target.checked ? '1' : '' })} />
            Solo disponibles
          </label>
          <label className="select">
            <span className="sr-only">Ordenar por</span>
            <select value={sort} onChange={(e) => updateParams({ orden: e.target.value === 'featured' ? '' : e.target.value })}>
              {SORT_OPTIONS.map((option) => <option key={option.value} value={option.value}>{option.label}</option>)}
            </select>
          </label>
        </div>
      </section>

      <Alert>{error}</Alert>

      {!result && !error && <Spinner label="Cargando catálogo…" />}

      {result && (
        <>
          <p className="results-count">
            {result.totalElements} {result.totalElements === 1 ? 'artículo encontrado' : 'artículos encontrados'}
            {q && <> para “<strong>{q}</strong>”</>}
          </p>
          {result.content.length === 0 ? (
            <EmptyState icon={<SearchIcon size={40} />} title="No encontramos artículos"
              action={<button type="button" className="btn btn--ghost" onClick={() => setParams({})}>Ver todo el catálogo</button>}>
              Intenta con otra palabra o elimina algunos filtros.
            </EmptyState>
          ) : (
            <div className="product-grid">
              {result.content.map((product) => (
                <ProductCard key={product.id} product={product} onAdd={(p) => add(p.id, 1, p.name)}
                  adding={addingId === product.id} />
              ))}
            </div>
          )}
          <Pagination page={result.page} totalPages={result.totalPages} onChange={changePage} />
        </>
      )}
    </>
  );
}
