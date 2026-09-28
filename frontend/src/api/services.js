import { api } from './client.js';

/** user-service: autenticación y perfil. */
export const authApi = {
  register: (data) => api.post('/auth/register', data, { auth: false }),
  login: (email, password) => api.post('/auth/login', { email, password }, { auth: false }),
  forgotPassword: (email) => api.post('/auth/forgot-password', { email }, { auth: false }),
  resetPassword: (token, newPassword) => api.post('/auth/reset-password', { token, newPassword }, { auth: false }),
};

export const userApi = {
  me: () => api.get('/users/me'),
  update: (data) => api.put('/users/me', data),
  updateAddress: (shippingAddress) => api.patch('/users/me/shipping-address', { shippingAddress }),
  changePassword: (currentPassword, newPassword) => api.put('/users/me/password', { currentPassword, newPassword }),
  remove: () => api.delete('/users/me'),
};

/** catalog-service: catálogo público. */
export const productApi = {
  search: (params, signal) => {
    const query = new URLSearchParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '' && value !== false) query.set(key, value);
    });
    return api.get(`/products?${query}`, { auth: false, signal });
  },
  get: (id) => api.get(`/products/${id}`, { auth: false }),
  categories: () => api.get('/products/categories', { auth: false }),
};

/** order-service: carrito y órdenes. */
export const cartApi = {
  get: () => api.get('/cart'),
  add: (productId, quantity) => api.post('/cart/items', { productId, quantity }),
  update: (productId, quantity) => api.put(`/cart/items/${productId}`, { quantity }),
  remove: (productId) => api.delete(`/cart/items/${productId}`),
  clear: () => api.delete('/cart'),
};

export const orderApi = {
  create: (shippingAddress) => api.post('/orders', { shippingAddress }),
  list: (page = 0, size = 10) => api.get(`/orders?page=${page}&size=${size}`),
  get: (orderNumber) => api.get(`/orders/${encodeURIComponent(orderNumber)}`),
  cancel: (orderNumber) => api.post(`/orders/${encodeURIComponent(orderNumber)}/cancel`),
};

export const adminApi = {
  orders: (status, page = 0, size = 20) =>
    api.get(`/admin/orders?page=${page}&size=${size}${status ? `&status=${status}` : ''}`),
  updateStatus: (orderNumber, status) =>
    api.patch(`/admin/orders/${encodeURIComponent(orderNumber)}/status`, { status }),
};
