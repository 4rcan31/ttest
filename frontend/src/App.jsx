import { lazy, Suspense } from 'react';
import { Route, Routes } from 'react-router';
import Layout from './components/Layout.jsx';
import ProtectedRoute from './components/ProtectedRoute.jsx';
import Spinner from './components/Spinner.jsx';
import CatalogPage from './pages/CatalogPage.jsx';

// Carga diferida por ruta: el catálogo (80% del tráfico, anónimo) descarga el mínimo de JavaScript.
const ProductDetailPage = lazy(() => import('./pages/ProductDetailPage.jsx'));
const LoginPage = lazy(() => import('./pages/LoginPage.jsx'));
const RegisterPage = lazy(() => import('./pages/RegisterPage.jsx'));
const ForgotPasswordPage = lazy(() => import('./pages/ForgotPasswordPage.jsx'));
const ResetPasswordPage = lazy(() => import('./pages/ResetPasswordPage.jsx'));
const ProfilePage = lazy(() => import('./pages/ProfilePage.jsx'));
const CartPage = lazy(() => import('./pages/CartPage.jsx'));
const CheckoutPage = lazy(() => import('./pages/CheckoutPage.jsx'));
const OrderConfirmationPage = lazy(() => import('./pages/OrderConfirmationPage.jsx'));
const OrdersPage = lazy(() => import('./pages/OrdersPage.jsx'));
const OrderDetailPage = lazy(() => import('./pages/OrderDetailPage.jsx'));
const AdminOrdersPage = lazy(() => import('./pages/AdminOrdersPage.jsx'));
const NotFoundPage = lazy(() => import('./pages/NotFoundPage.jsx'));

export default function App() {
  return (
    <Layout>
      <Suspense fallback={<Spinner label="Cargando…" />}>
        <Routes>
          <Route path="/" element={<CatalogPage />} />
          <Route path="/productos/:id" element={<ProductDetailPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/registro" element={<RegisterPage />} />
          <Route path="/recuperar-password" element={<ForgotPasswordPage />} />
          <Route path="/restablecer-password" element={<ResetPasswordPage />} />

          <Route element={<ProtectedRoute />}>
            <Route path="/perfil" element={<ProfilePage />} />
            <Route path="/carrito" element={<CartPage />} />
            <Route path="/checkout" element={<CheckoutPage />} />
            <Route path="/pedido-confirmado/:orderNumber" element={<OrderConfirmationPage />} />
            <Route path="/pedidos" element={<OrdersPage />} />
            <Route path="/pedidos/:orderNumber" element={<OrderDetailPage />} />
          </Route>

          <Route element={<ProtectedRoute role="ADMIN" />}>
            <Route path="/admin/pedidos" element={<AdminOrdersPage />} />
          </Route>

          <Route path="*" element={<NotFoundPage />} />
        </Routes>
      </Suspense>
    </Layout>
  );
}
