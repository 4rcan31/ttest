import { render, screen, waitFor } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { MemoryRouter, Route, Routes } from 'react-router';
import { afterEach, beforeEach, describe, expect, it, vi } from 'vitest';
import { AuthProvider } from '../context/AuthContext.jsx';
import { ToastProvider } from '../context/ToastContext.jsx';
import RegisterPage from '../pages/RegisterPage.jsx';

function renderPage() {
  return render(
    <MemoryRouter initialEntries={['/registro']}>
      <ToastProvider>
        <AuthProvider>
          <Routes>
            <Route path="/registro" element={<RegisterPage />} />
            <Route path="/" element={<h1>Catálogo</h1>} />
          </Routes>
        </AuthProvider>
      </ToastProvider>
    </MemoryRouter>,
  );
}

async function fillValidForm(user) {
  await user.type(screen.getByLabelText(/^Nombres/), 'Ana Sofía');
  await user.type(screen.getByLabelText(/^Apellidos/), 'López');
  await user.type(screen.getByLabelText(/^Correo electrónico/), 'ana@correo.com');
  await user.type(screen.getByLabelText(/^Fecha de nacimiento/), '1995-04-10');
  await user.type(screen.getByLabelText(/^Dirección de envío/), 'Colonia Escalón, Calle 1 #23');
  await user.type(screen.getByLabelText(/^Contraseña/), 'Demo1234');
  await user.type(screen.getByLabelText(/^Confirmar contraseña/), 'Demo1234');
}

describe('RegisterPage', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('muestra los errores de campos obligatorios y no llama a la API', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.click(screen.getByRole('button', { name: 'Crear cuenta' }));

    expect(screen.getByText('Los nombres son obligatorios')).toBeInTheDocument();
    expect(screen.getByText('Los apellidos son obligatorios')).toBeInTheDocument();
    expect(screen.getByText('El correo electrónico es obligatorio')).toBeInTheDocument();
    expect(screen.getByText('La fecha de nacimiento es obligatoria')).toBeInTheDocument();
    expect(screen.getByText('La dirección de envío es obligatoria')).toBeInTheDocument();
    expect(screen.getByText('La contraseña es obligatoria')).toBeInTheDocument();
    expect(fetch).not.toHaveBeenCalled();
  });

  it('valida formato de correo y mayoría de edad', async () => {
    const user = userEvent.setup();
    renderPage();

    await user.type(screen.getByLabelText(/^Correo electrónico/), 'correo-invalido');
    const underage = new Date();
    underage.setFullYear(underage.getFullYear() - 16);
    await user.type(screen.getByLabelText(/^Fecha de nacimiento/), underage.toISOString().slice(0, 10));
    await user.click(screen.getByRole('button', { name: 'Crear cuenta' }));

    expect(screen.getByText('El formato del correo electrónico no es válido')).toBeInTheDocument();
    expect(screen.getByText('Debe ser mayor de 18 años')).toBeInTheDocument();
  });

  it('registra al usuario, guarda la sesión y redirige al catálogo', async () => {
    fetch.mockResolvedValue(new Response(JSON.stringify({
      accessToken: 'jwt-token', tokenType: 'Bearer', expiresIn: 3600,
      user: { id: 5, firstName: 'Ana Sofía', lastName: 'López', email: 'ana@correo.com', role: 'CUSTOMER' },
    }), { status: 201, headers: { 'Content-Type': 'application/json' } }));
    const user = userEvent.setup();
    renderPage();

    await fillValidForm(user);
    await user.click(screen.getByRole('button', { name: 'Crear cuenta' }));

    await waitFor(() => expect(screen.getByRole('heading', { name: 'Catálogo' })).toBeInTheDocument());
    const [url, options] = fetch.mock.calls[0];
    expect(url).toBe('/api/auth/register');
    expect(JSON.parse(options.body)).toEqual({
      firstName: 'Ana Sofía', lastName: 'López', email: 'ana@correo.com', birthDate: '1995-04-10',
      shippingAddress: 'Colonia Escalón, Calle 1 #23', password: 'Demo1234',
    });
    expect(JSON.parse(localStorage.getItem('sportshop.session')).token).toBe('jwt-token');
  });

  it('muestra los errores devueltos por el backend', async () => {
    fetch.mockResolvedValue(new Response(JSON.stringify({
      status: 409, code: 'EMAIL_ALREADY_REGISTERED', message: 'Ya existe una cuenta registrada con ese correo',
    }), { status: 409, headers: { 'Content-Type': 'application/json' } }));
    const user = userEvent.setup();
    renderPage();

    await fillValidForm(user);
    await user.click(screen.getByRole('button', { name: 'Crear cuenta' }));

    expect(await screen.findByText('Ya existe una cuenta registrada con ese correo')).toBeInTheDocument();
  });
});
