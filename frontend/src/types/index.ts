export type Role = 'ADMIN' | 'PHARMACIST' | 'STAFF';
export type MedicineStatus = 'SAFE' | 'NEAR_EXPIRY_30' | 'NEAR_EXPIRY_15' | 'NEAR_EXPIRY_7' | 'EXPIRED' | 'OUT_OF_STOCK';
export type StockTransactionType = 'PURCHASE' | 'SALE' | 'ADJUSTMENT' | 'BULK_UPLOAD' | 'EXPIRED_REMOVAL';
export type NotificationStatus = 'PENDING' | 'SENT' | 'FAILED' | 'READ';
export type NotificationType = 'EXPIRY_ALERT' | 'LOW_STOCK_ALERT' | 'STOCK_ACTIVITY' | 'SYSTEM';

export interface ApiResponse<T> {
  success: boolean;
  message: string;
  data: T;
  timestamp: string;
}

export interface PageResponse<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

export interface Branch {
  id: string;
  name: string;
  code: string;
  address: string;
  city: string;
  state: string;
  country: string;
  phone?: string;
  email?: string;
  active: boolean;
}

export interface UserProfile {
  id: string;
  fullName: string;
  email: string;
  phone?: string;
  role: Role;
  branchId?: string;
  branchName?: string;
  emailNotificationsEnabled: boolean;
  smsNotificationsEnabled: boolean;
}

export interface AuthPayload {
  accessToken: string;
  tokenType: string;
  user: UserProfile;
}

export interface Medicine {
  id: string;
  name: string;
  batchNumber: string;
  category: string;
  manufacturer: string;
  quantity: number;
  reorderLevel: number;
  price: number;
  expiryDate: string;
  manufactureDate: string;
  barcode?: string;
  imageUrl?: string;
  status: MedicineStatus;
  branchId: string;
  branchName: string;
  lowStock: boolean;
  predictedExpiryRiskScore: number;
}

export interface MedicinePayload {
  name: string;
  batchNumber: string;
  category: string;
  manufacturer: string;
  quantity: number;
  reorderLevel: number;
  price: number;
  expiryDate: string;
  manufactureDate: string;
  barcode?: string;
  branchId?: string;
}

export interface BulkUploadResult {
  created: number;
  updated: number;
  errors: string[];
}

export interface StockTransaction {
  id: string;
  medicineId: string;
  medicineName: string;
  branchId: string;
  branchName: string;
  performedBy?: string;
  type: StockTransactionType;
  quantityBefore: number;
  quantityChange: number;
  quantityAfter: number;
  referenceNote?: string;
  unitPrice?: number;
  transactionDate: string;
}

export interface DashboardChartPoint {
  label: string;
  value: number;
}

export interface PredictionInsight {
  medicineId: string;
  medicineName: string;
  batchNumber: string;
  estimatedDaysToExhaust: number;
  estimatedDaysToExpiry: number;
  recommendation: string;
}

export interface DashboardSummary {
  totalMedicines: number;
  expiringIn7Days: number;
  expiringIn15Days: number;
  expiringIn30Days: number;
  lowStockCount: number;
  outOfStockCount: number;
  expiryTrend: DashboardChartPoint[];
  stockTrend: DashboardChartPoint[];
  predictions: PredictionInsight[];
}

export interface AppNotification {
  id: string;
  title: string;
  message: string;
  type: NotificationType;
  status: NotificationStatus;
  channel: string;
  createdAt: string;
  readAt?: string;
}

export interface AuditLog {
  id: string;
  action: string;
  entityType: string;
  entityId: string;
  actorEmail: string;
  description: string;
  metadata?: string;
  createdAt: string;
}
