export type LoginRequest = { username: string; password: string };
export type SignupRequest = { username: string; password: string };

export type UtilityType =
  | 'ELECTRICITY'
  | 'WATER'
  | 'GAS'
  | 'INTERNET'
  | 'SEWER'
  | 'OTHER'
  | 'PROCESSING_FEE';

export type BillStatus = 'PAID' | 'UNPAID' | 'OVERDUE' | 'PARTIAL';

export interface Bill {
  id?: number;
  utilityType?: UtilityType | null;
  serviceMonth?: string | null;
  dueDate?: string | null;
  amount?: string | null;
  status?: BillStatus | null;
  notes?: string | null;
  createdAt?: string | null;
  updatedAt?: string | null;
}
