import { useEffect, useState } from 'react';
import { Link, NavLink, useLocation, useNavigate } from 'react-router';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { CartIcon, CloseIcon, LogoutIcon, MenuIcon, ShieldIcon, UserIcon, ReceiptIcon } from './Icons.jsx';

function Logo() {
  return (
    <Link to="/" className="logo" aria-label="SportShop, ir al catálogo">
      <span className="logo__mark" aria-hidden="true">S</span>
      <span className="logo__text">Sport<strong>Shop</strong></span>
    </Link>
  );
}

function Header() {
  const { isAuthenticated, isAdmin, user, logout } = useAuth();
  const { cart, reset } = useCart();
  const [menuOpen, setMenuOpen] = useState(false);
  const location = useLocation();
  const navigate = useNavigate();

  useEffect(() => setMenuOpen(false), [location.pathname]);

  const handleLogout = () => {
    logout();
    reset();
    navigate('/');
  };

  return (
    <header className="header">
      <div className="container header__inner">
        <Logo />

        <nav className={`nav ${menuOpen ? 'nav--open' : ''}`} aria-label="Navegación principal">
          <NavLink to="/" end className="nav__link">Catálogo</NavLink>
          {isAuthenticated ? (
            <>
              <NavLink to="/pedidos" className="nav__link"><ReceiptIcon size={18} /> Mis pedidos</NavLink>
              <NavLink to="/perfil" className="nav__link"><UserIcon size={18} /> {user.firstName.split(' ')[0]}</NavLink>
              {isAdmin && (
                <NavLink to="/admin/pedidos" className="nav__link"><ShieldIcon size={18} /> Administración</NavLink>
              )}
              <button type="button" className="nav__link nav__button" onClick={handleLogout}>
                <LogoutIcon size={18} /> Salir
              </button>
            </>
          ) : (
            <>
              <NavLink to="/login" className="nav__link">Iniciar sesión</NavLink>
              <Link to="/registro" className="btn btn--primary btn--sm">Crear cuenta</Link>
            </>
          )}
        </nav>

        <div className="header__actions">
          <Link to="/carrito" className="cart-button" aria-label={`Carrito, ${cart.totalItems} artículos`}>
            <CartIcon size={22} />
            {cart.totalItems > 0 && <span className="cart-button__badge">{cart.totalItems}</span>}
          </Link>
          <button type="button" className="icon-button menu-toggle" onClick={() => setMenuOpen((open) => !open)}
            aria-label={menuOpen ? 'Cerrar menú' : 'Abrir menú'} aria-expanded={menuOpen}>
            {menuOpen ? <CloseIcon /> : <MenuIcon />}
          </button>
        </div>
      </div>
    </header>
  );
}

function Footer() {
  return (
    <footer className="footer">
      <div className="container footer__inner">
        <span>© {new Date().getFullYear()} SportShop · Artículos deportivos</span>
        <span className="footer__muted">Envío gratis en compras desde $75.00</span>
      </div>
    </footer>
  );
}

export default function Layout({ children }) {
  return (
    <div className="app">
      <a href="#contenido" className="skip-link">Saltar al contenido</a>
      <Header />
      <main id="contenido" className="main">
        <div className="container">{children}</div>
      </main>
      <Footer />
    </div>
  );
}
