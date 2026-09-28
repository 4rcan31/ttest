import { createContext, useCallback, useContext, useMemo, useRef, useState } from 'react';
import { CheckIcon, AlertIcon, CloseIcon } from '../components/Icons.jsx';

const ToastContext = createContext(null);

export function ToastProvider({ children }) {
  const [toasts, setToasts] = useState([]);
  const nextId = useRef(1);

  const dismiss = useCallback((id) => setToasts((list) => list.filter((toast) => toast.id !== id)), []);

  const show = useCallback((message, type = 'success') => {
    const id = nextId.current++;
    setToasts((list) => [...list.slice(-2), { id, message, type }]);
    setTimeout(() => dismiss(id), 3500);
  }, [dismiss]);

  const value = useMemo(() => ({
    success: (message) => show(message, 'success'),
    error: (message) => show(message, 'error'),
  }), [show]);

  return (
    <ToastContext.Provider value={value}>
      {children}
      <div className="toast-region" role="status" aria-live="polite">
        {toasts.map((toast) => (
          <div key={toast.id} className={`toast toast--${toast.type}`}>
            {toast.type === 'success' ? <CheckIcon /> : <AlertIcon />}
            <span>{toast.message}</span>
            <button type="button" className="icon-button" onClick={() => dismiss(toast.id)} aria-label="Cerrar">
              <CloseIcon />
            </button>
          </div>
        ))}
      </div>
    </ToastContext.Provider>
  );
}

export function useToast() {
  const context = useContext(ToastContext);
  if (!context) throw new Error('useToast debe usarse dentro de ToastProvider');
  return context;
}
