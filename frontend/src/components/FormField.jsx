import { useId, useState } from 'react';
import { EyeIcon, EyeOffIcon } from './Icons.jsx';

/** Campo de formulario accesible con etiqueta, ayuda y mensaje de error asociados. */
export default function FormField({
  label, name, type = 'text', value, onChange, onBlur, error, hint, as = 'input', required = true, ...props
}) {
  const id = useId();
  const [showPassword, setShowPassword] = useState(false);
  const describedBy = [error ? `${id}-error` : null, hint ? `${id}-hint` : null].filter(Boolean).join(' ');
  const Component = as;
  const inputType = type === 'password' && showPassword ? 'text' : type;

  return (
    <div className={`field ${error ? 'field--error' : ''}`}>
      <label htmlFor={id} className="field__label">
        {label}{required && <span className="field__required" aria-hidden="true"> *</span>}
      </label>
      <div className="field__control">
        <Component
          id={id}
          name={name}
          type={as === 'input' ? inputType : undefined}
          value={value}
          onChange={onChange}
          onBlur={onBlur}
          aria-invalid={Boolean(error)}
          aria-describedby={describedBy || undefined}
          required={required}
          className="field__input"
          {...props}
        />
        {type === 'password' && (
          <button type="button" className="field__toggle" onClick={() => setShowPassword((v) => !v)}
            aria-label={showPassword ? 'Ocultar contraseña' : 'Mostrar contraseña'}>
            {showPassword ? <EyeOffIcon size={18} /> : <EyeIcon size={18} />}
          </button>
        )}
      </div>
      {hint && !error && <p id={`${id}-hint`} className="field__hint">{hint}</p>}
      {error && <p id={`${id}-error`} className="field__error" role="alert">{error}</p>}
    </div>
  );
}
