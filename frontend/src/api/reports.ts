import api from '@/lib/axios';

export async function downloadReport(type: string, format: 'csv' | 'pdf', params: Record<string, string | undefined>) {
  const { data } = await api.get(`/reports/${type}/export`, {
    params: { format, ...params },
    responseType: 'blob'
  });
  return data as Blob;
}
