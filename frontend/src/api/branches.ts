import api from '@/lib/axios';
import type { ApiResponse, Branch } from '@/types';

export async function getBranches() {
  const { data } = await api.get<ApiResponse<Branch[]>>('/branches');
  return data.data;
}
