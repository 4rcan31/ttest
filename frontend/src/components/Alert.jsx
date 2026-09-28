import { AlertIcon, CheckIcon } from './Icons.jsx';

export default function Alert({ type = 'error', children }) {
  if (!children) return null;
  return (
    <div className={`alert alert--${type}`} role={type === 'error' ? 'alert' : 'status'}>
      {type === 'success' ? <CheckIcon size={18} /> : <AlertIcon size={18} />}
      <div>{children}</div>
    </div>
  );
}
