import api from '@/lib/axios';
import type { ApiResponse, AuthPayload } from '@/types';

export interface LoginPayload {
  email: string;
  password: string;
}

export interface RegisterPayload {
  fullName: string;
  email: string;
  password: string;
  phone?: string;
  role: 'ADMIN' | 'PHARMACIST' | 'STAFF';
  branchId?: string;
}

export async function login(payload: LoginPayload) {
  const { data } = await api.post<ApiResponse<AuthPayload>>('/auth/login', payload);
  return data.data;
}

export async function register(payload: RegisterPayload) {
  const { data } = await api.post<ApiResponse<AuthPayload>>('/auth/register', payload);
  return data.data;
}
