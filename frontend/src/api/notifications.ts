import api from '@/lib/axios';
import type { ApiResponse, AppNotification } from '@/types';

export async function getNotifications() {
  const { data } = await api.get<ApiResponse<AppNotification[]>>('/notifications');
  return data.data;
}

export async function markNotificationRead(id: string) {
  const { data } = await api.patch<ApiResponse<AppNotification>>(`/notifications/${id}/read`);
  return data.data;
}
