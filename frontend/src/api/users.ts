import api from '@/lib/axios';
import type { ApiResponse, UserProfile } from '@/types';

export interface ProfilePayload {
  fullName?: string;
  phone?: string;
  emailNotificationsEnabled?: boolean;
  smsNotificationsEnabled?: boolean;
}

export async function getProfile() {
  const { data } = await api.get<ApiResponse<UserProfile>>('/users/me');
  return data.data;
}

export async function updateProfile(payload: ProfilePayload) {
  const { data } = await api.put<ApiResponse<UserProfile>>('/users/me', payload);
  return data.data;
}
