import api from '@/lib/axios';
import type { ApiResponse, StockTransaction } from '@/types';

export interface StockAdjustmentPayload {
  medicineId: string;
  quantityChange: number;
  type: 'PURCHASE' | 'SALE' | 'ADJUSTMENT' | 'BULK_UPLOAD' | 'EXPIRED_REMOVAL';
  referenceNote?: string;
  unitPrice?: number;
}

export async function adjustStock(payload: StockAdjustmentPayload) {
  const { data } = await api.post<ApiResponse<StockTransaction>>('/stocks/adjustments', payload);
  return data.data;
}

export async function getStockHistory(medicineId: string) {
  const { data } = await api.get<ApiResponse<StockTransaction[]>>(`/stocks/medicines/${medicineId}/history`);
  return data.data;
}
