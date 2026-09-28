import { describe, expect, it } from 'vitest';
import {
  calculateAge, maxBirthDate, passwordChecks, validateBirthDate, validateEmail, validateName,
  validatePassword, validateRegistration,
} from '../utils/validators.js';

const TODAY = new Date(2026, 8, 28); // 28 de septiembre de 2026

describe('validateEmail', () => {
  it.each(['cliente@sportshop.com', 'maria.perez+promo@correo.com.sv'])('acepta %s', (email) => {
    expect(validateEmail(email)).toBeNull();
  });

  it.each(['', 'sin-arroba', 'a@b', 'usuario@dominio.', 'con espacio@correo.com'])('rechaza "%s"', (email) => {
    expect(validateEmail(email)).not.toBeNull();
  });
});

describe('validateBirthDate (mayores de 18 años)', () => {
  it('acepta a quien cumple 18 años hoy', () => {
    expect(validateBirthDate('2008-09-28', TODAY)).toBeNull();
  });

  it('rechaza a quien cumple 18 años mañana', () => {
    expect(validateBirthDate('2008-09-29', TODAY)).toBe('Debe ser mayor de 18 años');
  });

  it('exige la fecha', () => {
    expect(validateBirthDate('', TODAY)).toBe('La fecha de nacimiento es obligatoria');
  });

  it('calcula la edad considerando si ya pasó el cumpleaños', () => {
    expect(calculateAge('2000-09-29', TODAY)).toBe(25);
    expect(calculateAge('2000-09-28', TODAY)).toBe(26);
  });

  it('limita el selector de fecha a hoy menos 18 años', () => {
    expect(maxBirthDate(TODAY)).toBe('2008-09-28');
  });
});

describe('validatePassword', () => {
  it('exige mayúscula, minúscula, número y 8 caracteres', () => {
    expect(validatePassword('Demo1234')).toBeNull();
    expect(validatePassword('demo1234')).not.toBeNull();
    expect(validatePassword('DEMO1234')).not.toBeNull();
    expect(validatePassword('Demoxxxx')).not.toBeNull();
    expect(validatePassword('De1')).not.toBeNull();
  });

  it('entrega el detalle de cada requisito', () => {
    expect(passwordChecks('abc').map((c) => c.ok)).toEqual([false, false, true, false]);
  });
});

describe('validateName', () => {
  it('acepta tildes, ñ y nombres compuestos', () => {
    expect(validateName('María José', 'Los nombres')).toBeNull();
    expect(validateName('Peña-Núñez', 'Los apellidos')).toBeNull();
  });

  it('rechaza números', () => {
    expect(validateName('Juan123', 'Los nombres')).toBe('Los nombres solo pueden contener letras');
  });
});

describe('validateRegistration', () => {
  it('marca todos los campos obligatorios', () => {
    const errors = validateRegistration({
      firstName: '', lastName: '', email: '', birthDate: '', shippingAddress: '', password: '', confirmPassword: '',
    });
    expect(Object.keys(errors).sort()).toEqual(
      ['birthDate', 'confirmPassword', 'email', 'firstName', 'lastName', 'password', 'shippingAddress'].sort(),
    );
  });

  it('valida que las contraseñas coincidan', () => {
    const errors = validateRegistration({
      firstName: 'Ana', lastName: 'López', email: 'ana@correo.com', birthDate: '1990-01-01',
      shippingAddress: 'Calle Principal 123, Ciudad', password: 'Demo1234', confirmPassword: 'Demo12345',
    });
    expect(errors).toEqual({ confirmPassword: 'Las contraseñas no coinciden' });
  });
});
