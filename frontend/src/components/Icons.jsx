// Íconos SVG en línea (sin dependencias). Heredan el color del texto con currentColor.
function Icon({ children, size = 20, ...props }) {
  return (
    <svg width={size} height={size} viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2"
      strokeLinecap="round" strokeLinejoin="round" aria-hidden="true" focusable="false" {...props}>
      {children}
    </svg>
  );
}

export const CartIcon = (p) => (
  <Icon {...p}><circle cx="9" cy="20" r="1.5" /><circle cx="18" cy="20" r="1.5" />
    <path d="M2 3h3l2.6 12.2a2 2 0 0 0 2 1.6h8.2a2 2 0 0 0 2-1.5L22 7H6" /></Icon>
);
export const UserIcon = (p) => (
  <Icon {...p}><circle cx="12" cy="8" r="4" /><path d="M4 21c0-4 4-6 8-6s8 2 8 6" /></Icon>
);
export const SearchIcon = (p) => (
  <Icon {...p}><circle cx="11" cy="11" r="7" /><path d="m20 20-3.5-3.5" /></Icon>
);
export const MenuIcon = (p) => <Icon {...p}><path d="M3 6h18M3 12h18M3 18h18" /></Icon>;
export const CloseIcon = (p) => <Icon {...p}><path d="M18 6 6 18M6 6l12 12" /></Icon>;
export const CheckIcon = (p) => <Icon {...p}><path d="M20 6 9 17l-5-5" /></Icon>;
export const AlertIcon = (p) => (
  <Icon {...p}><circle cx="12" cy="12" r="10" /><path d="M12 8v4M12 16h.01" /></Icon>
);
export const TrashIcon = (p) => (
  <Icon {...p}><path d="M3 6h18M8 6V4h8v2M19 6l-1 14H6L5 6M10 11v6M14 11v6" /></Icon>
);
export const PlusIcon = (p) => <Icon {...p}><path d="M12 5v14M5 12h14" /></Icon>;
export const MinusIcon = (p) => <Icon {...p}><path d="M5 12h14" /></Icon>;
export const TruckIcon = (p) => (
  <Icon {...p}><path d="M1 4h13v12H1zM14 9h4l4 4v3h-8z" /><circle cx="5.5" cy="18.5" r="2" /><circle cx="18.5" cy="18.5" r="2" /></Icon>
);
export const BoxIcon = (p) => (
  <Icon {...p}><path d="M21 8 12 3 3 8v8l9 5 9-5z" /><path d="m3 8 9 5 9-5M12 13v8" /></Icon>
);
export const PinIcon = (p) => (
  <Icon {...p}><path d="M12 22s7-7.3 7-12a7 7 0 0 0-14 0c0 4.7 7 12 7 12z" /><circle cx="12" cy="10" r="2.5" /></Icon>
);
export const EditIcon = (p) => (
  <Icon {...p}><path d="M12 20h9" /><path d="M16.5 3.5a2.1 2.1 0 0 1 3 3L7 19l-4 1 1-4z" /></Icon>
);
export const ShieldIcon = (p) => <Icon {...p}><path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z" /></Icon>;
export const LogoutIcon = (p) => (
  <Icon {...p}><path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4M16 17l5-5-5-5M21 12H9" /></Icon>
);
export const ArrowLeftIcon = (p) => <Icon {...p}><path d="M19 12H5M12 19l-7-7 7-7" /></Icon>;
export const ChevronLeftIcon = (p) => <Icon {...p}><path d="m15 18-6-6 6-6" /></Icon>;
export const ChevronRightIcon = (p) => <Icon {...p}><path d="m9 18 6-6-6-6" /></Icon>;
export const ReceiptIcon = (p) => (
  <Icon {...p}><path d="M4 2v20l3-2 3 2 3-2 3 2 3-2 1 1V2l-1 1-3-2-3 2-3-2-3 2-3-2z" /><path d="M8 8h8M8 12h8M8 16h5" /></Icon>
);
export const EyeIcon = (p) => (
  <Icon {...p}><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8S1 12 1 12z" /><circle cx="12" cy="12" r="3" /></Icon>
);
export const EyeOffIcon = (p) => (
  <Icon {...p}><path d="M17.9 17.9A10 10 0 0 1 12 20C5 20 1 12 1 12a18 18 0 0 1 5.1-5.9M9.9 4.2A9 9 0 0 1 12 4c7 0 11 8 11 8a18 18 0 0 1-2.2 3.2M1 1l22 22" /><path d="M14.1 14.1a3 3 0 1 1-4.2-4.2" /></Icon>
);
