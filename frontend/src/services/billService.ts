import api from '../api/axiosConfig';
import type { Bill, BillStatus, UtilityType } from '../types';

export async function uploadBill(file: File): Promise<Bill> {
  const fd = new FormData();
  fd.append('file', file);
  const resp = await api.post('/api/bills/upload-pdf', fd, {
    headers: { 'Content-Type': 'multipart/form-data' },
  });
  return resp.data as Bill;
}

export async function getBills(params?: {
  month?: string;
  status?: BillStatus;
  utilityType?: UtilityType;
}): Promise<Bill[]> {
  const resp = await api.get('/api/bills', { params });
  return resp.data as Bill[];
}

export async function getBillById(id: number): Promise<Bill> {
  const resp = await api.get(`/api/bills/${id}`);
  return resp.data as Bill;
}

export async function updateBillStatus(id: number, status: BillStatus) {
  const resp = await api.patch(`/api/bills/${id}/status`, null, {
    params: { status },
  });
  return resp.data as Bill;
}

export async function deleteBill(id: number) {
  await api.delete(`/api/bills/${id}`);
}

export async function getSumByUtility() {
  const resp = await api.get('/api/bills/summary/by-utility');
  return resp.data as Array<[string, number]>;
}

export async function getSumByStatus() {
  const resp = await api.get('/api/bills/summary/by-status');
  return resp.data as Array<[string, number]>;
}

export async function getMonthlyTotals() {
  const resp = await api.get('/api/bills/summary/monthly');
  return resp.data as Array<[string, number]>;
}

export async function getDueNextMonth() {
  const resp = await api.get('/api/bills/due-next-month');
  return resp.data as Bill[];
}
