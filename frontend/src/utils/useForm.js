import { useCallback, useState } from 'react';

/**
 * Manejo sencillo de formularios: valida al perder el foco y al enviar, y permite mostrar
 * los errores por campo que devuelve el backend (fieldErrors).
 */
export default function useForm(initialValues, validate) {
  const [values, setValues] = useState(initialValues);
  const [errors, setErrors] = useState({});
  const [touched, setTouched] = useState({});

  const handleChange = useCallback((event) => {
    const { name, value, type, checked } = event.target;
    const next = { ...values, [name]: type === 'checkbox' ? checked : value };
    setValues(next);
    // Solo se revalidan los campos ya visitados, para no mostrar errores mientras el usuario escribe por primera vez.
    const validation = validate(next);
    setErrors((current) => {
      const updated = { ...current };
      Object.keys(next).forEach((key) => {
        if (touched[key] || (key === name && current[key])) updated[key] = validation[key];
      });
      return updated;
    });
  }, [values, touched, validate]);

  const handleBlur = useCallback((event) => {
    const { name } = event.target;
    setTouched((current) => ({ ...current, [name]: true }));
    setErrors((current) => ({ ...current, [name]: validate(values)[name] }));
  }, [validate, values]);

  const validateAll = useCallback(() => {
    const validation = validate(values);
    setErrors(validation);
    setTouched(Object.fromEntries(Object.keys(values).map((key) => [key, true])));
    return Object.values(validation).every((message) => !message);
  }, [validate, values]);

  const setServerErrors = useCallback((fieldErrors = {}) => {
    setErrors((current) => ({ ...current, ...fieldErrors }));
  }, []);

  return { values, setValues, errors, handleChange, handleBlur, validateAll, setServerErrors };
}
