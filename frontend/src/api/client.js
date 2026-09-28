const API_BASE = import.meta.env.VITE_API_BASE_URL ?? '/api';
const SESSION_KEY = 'sportshop.session';

/** Error de API con el formato ApiError devuelto por los microservicios. */
export class ApiError extends Error {
  constructor(status, body) {
    super(body?.message ?? 'Ocurrió un error inesperado. Intente nuevamente.');
    this.name = 'ApiError';
    this.status = status;
    this.code = body?.code ?? null;
    this.fieldErrors = body?.fieldErrors ?? {};
  }
}

export function readSession() {
  try {
    const raw = localStorage.getItem(SESSION_KEY);
    if (!raw) return null;
    const session = JSON.parse(raw);
    if (!session?.token || Date.now() >= session.expiresAt) {
      localStorage.removeItem(SESSION_KEY);
      return null;
    }
    return session;
  } catch {
    return null;
  }
}

export function writeSession(session) {
  try {
    if (session) {
      localStorage.setItem(SESSION_KEY, JSON.stringify(session));
    } else {
      localStorage.removeItem(SESSION_KEY);
    }
  } catch {
    /* almacenamiento no disponible (modo privado): la sesión vive solo en memoria */
  }
}

/**
 * Cliente HTTP mínimo sobre fetch: agrega el token JWT, serializa JSON y normaliza errores.
 * Si una llamada autenticada responde 401, se notifica para cerrar la sesión.
 */
export async function request(path, { method = 'GET', body, auth = true, signal } = {}) {
  const headers = { Accept: 'application/json' };
  const session = auth ? readSession() : null;
  if (session) headers.Authorization = `Bearer ${session.token}`;
  if (body !== undefined) headers['Content-Type'] = 'application/json';

  let response;
  try {
    response = await fetch(`${API_BASE}${path}`, {
      method,
      headers,
      body: body !== undefined ? JSON.stringify(body) : undefined,
      signal,
    });
  } catch (error) {
    if (error.name === 'AbortError') throw error;
    throw new ApiError(0, { message: 'No hay conexión con el servidor. Verifique su conexión a internet.' });
  }

  if (response.status === 204) return null;

  const text = await response.text();
  let data = null;
  if (text) {
    try {
      data = JSON.parse(text);
    } catch {
      data = null;
    }
  }

  if (!response.ok) {
    if (response.status === 401 && session) {
      window.dispatchEvent(new CustomEvent('sportshop:unauthorized'));
    }
    throw new ApiError(response.status, data);
  }
  return data;
}

export const api = {
  get: (path, options) => request(path, { ...options, method: 'GET' }),
  post: (path, body, options) => request(path, { ...options, method: 'POST', body }),
  put: (path, body, options) => request(path, { ...options, method: 'PUT', body }),
  patch: (path, body, options) => request(path, { ...options, method: 'PATCH', body }),
  delete: (path, options) => request(path, { ...options, method: 'DELETE' }),
};
