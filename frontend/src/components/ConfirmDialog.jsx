import { useEffect, useRef } from 'react';

/** Diálogo modal de confirmación accesible (cierra con Escape y devuelve el foco). */
export default function ConfirmDialog({
  open, title, children, confirmLabel = 'Confirmar', cancelLabel = 'Cancelar', danger = false, busy = false,
  onConfirm, onCancel,
}) {
  const cancelRef = useRef(null);

  useEffect(() => {
    if (!open) return undefined;
    const previous = document.activeElement;
    cancelRef.current?.focus();
    const onKey = (event) => {
      if (event.key === 'Escape') onCancel();
    };
    document.addEventListener('keydown', onKey);
    return () => {
      document.removeEventListener('keydown', onKey);
      previous?.focus?.();
    };
  }, [open, onCancel]);

  if (!open) return null;
  return (
    <div className="dialog-backdrop" onClick={onCancel}>
      <div className="dialog" role="alertdialog" aria-modal="true" aria-labelledby="dialog-title"
        onClick={(event) => event.stopPropagation()}>
        <h2 id="dialog-title" className="dialog__title">{title}</h2>
        <div className="dialog__body">{children}</div>
        <div className="dialog__actions">
          <button ref={cancelRef} type="button" className="btn btn--ghost" onClick={onCancel} disabled={busy}>
            {cancelLabel}
          </button>
          <button type="button" className={`btn ${danger ? 'btn--danger' : 'btn--primary'}`} onClick={onConfirm}
            disabled={busy}>
            {busy ? 'Procesando…' : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
}
