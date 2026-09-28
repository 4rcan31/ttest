import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router';
import { useAuth } from '../context/AuthContext.jsx';
import { useCart } from '../context/CartContext.jsx';
import { useToast } from '../context/ToastContext.jsx';

/** Agrega al carrito; si el usuario es anónimo lo lleva al login y lo regresa a la misma página. */
export default function useAddToCart() {
  const { isAuthenticated } = useAuth();
  const { addItem } = useCart();
  const toast = useToast();
  const navigate = useNavigate();
  const location = useLocation();
  const [addingId, setAddingId] = useState(null);

  const add = async (productId, quantity, name) => {
    if (!isAuthenticated) {
      toast.error('Inicia sesión para agregar artículos a tu carrito');
      navigate('/login', { state: { from: location.pathname + location.search } });
      return false;
    }
    setAddingId(productId);
    try {
      await addItem(productId, quantity);
      toast.success(`${name} se agregó al carrito`);
      return true;
    } catch (error) {
      toast.error(error.message);
      return false;
    } finally {
      setAddingId(null);
    }
  };

  return { add, addingId };
}
