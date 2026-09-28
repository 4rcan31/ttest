export default function EmptyState({ icon, title, children, action }) {
  return (
    <div className="empty-state">
      {icon && <div className="empty-state__icon">{icon}</div>}
      <h2 className="empty-state__title">{title}</h2>
      {children && <p className="empty-state__text">{children}</p>}
      {action}
    </div>
  );
}
