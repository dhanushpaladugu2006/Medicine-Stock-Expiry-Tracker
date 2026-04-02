import api from '@/lib/axios';
import type { ApiResponse, BulkUploadResult, Medicine, MedicinePayload, PageResponse } from '@/types';

export interface MedicineFilters {
  search?: string;
  category?: string;
  branchId?: string;
  stockStatus?: string;
  expiryFrom?: string;
  expiryTo?: string;
  page?: number;
  size?: number;
}

export async function getMedicines(filters: MedicineFilters) {
  const { data } = await api.get<ApiResponse<PageResponse<Medicine>>>('/medicines', { params: filters });
  return data.data;
}

export async function getMedicine(id: string) {
  const { data } = await api.get<ApiResponse<Medicine>>(`/medicines/${id}`);
  return data.data;
}

export async function createMedicine(payload: MedicinePayload) {
  const { data } = await api.post<ApiResponse<Medicine>>('/medicines', payload);
  return data.data;
}

export async function updateMedicine(id: string, payload: MedicinePayload) {
  const { data } = await api.put<ApiResponse<Medicine>>(`/medicines/${id}`, payload);
  return data.data;
}

export async function deleteMedicine(id: string) {
  await api.delete(`/medicines/${id}`);
}

export async function uploadMedicineImage(id: string, file: File) {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await api.post<ApiResponse<Medicine>>(`/medicines/${id}/image`, formData);
  return data.data;
}

export async function bulkUpload(file: File, branchId?: string) {
  const formData = new FormData();
  formData.append('file', file);
  if (branchId) {
    formData.append('branchId', branchId);
  }
  const { data } = await api.post<ApiResponse<BulkUploadResult>>('/medicines/bulk-upload', formData);
  return data.data;
}
