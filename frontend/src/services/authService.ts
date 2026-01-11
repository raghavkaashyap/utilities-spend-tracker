import api from '../api/axiosConfig';
import type { LoginRequest, SignupRequest } from '../types';

export async function login(request: LoginRequest) {
  const resp = await api.post('/auth/login', request);
  const token = resp.data?.token;
  if (!token) {
    throw new Error('No token received from server');
  }
  localStorage.setItem('token', token);
  return token;
}

export async function signup(request: SignupRequest) {
  const resp = await api.post('/auth/signup', request);
  return resp.data;
}

export function logout() {
  localStorage.removeItem('token');
}
