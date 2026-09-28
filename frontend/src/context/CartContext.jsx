import { createContext, useCallback, useContext, useEffect, useMemo, useState } from 'react';
import { cartApi } from '../api/services.js';
import { useAuth } from './AuthContext.jsx';

const CartContext = createContext(null);

const EMPTY_CART = {
  items: [], totalItems: 0, subtotal: 0, shippingCost: 0, total: 0, readyForCheckout: false,
};

/** Estado del carrito sincronizado con order-service (el carrito vive en el servidor, por usuario). */
export function CartProvider({ children }) {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState(EMPTY_CART);
  const [loading, setLoading] = useState(false);

  const refresh = useCallback(async () => {
    if (!isAuthenticated) {
      setCart(EMPTY_CART);
      return EMPTY_CART;
    }
    setLoading(true);
    try {
      const data = await cartApi.get();
      setCart(data);
      return data;
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    refresh().catch(() => setCart(EMPTY_CART));
  }, [refresh]);

  const addItem = useCallback(async (productId, quantity = 1) => {
    const data = await cartApi.add(productId, quantity);
    setCart(data);
    return data;
  }, []);

  const updateItem = useCallback(async (productId, quantity) => {
    const data = await cartApi.update(productId, quantity);
    setCart(data);
    return data;
  }, []);

  const removeItem = useCallback(async (productId) => {
    const data = await cartApi.remove(productId);
    setCart(data);
    return data;
  }, []);

  const clear = useCallback(async () => {
    await cartApi.clear();
    setCart(EMPTY_CART);
  }, []);

  const reset = useCallback(() => setCart(EMPTY_CART), []);

  const value = useMemo(() => ({
    cart, loading, refresh, addItem, updateItem, removeItem, clear, reset,
  }), [cart, loading, refresh, addItem, updateItem, removeItem, clear, reset]);

  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export function useCart() {
  const context = useContext(CartContext);
  if (!context) throw new Error('useCart debe usarse dentro de CartProvider');
  return context;
}
