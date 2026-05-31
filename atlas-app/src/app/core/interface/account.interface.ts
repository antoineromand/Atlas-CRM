export interface AccountResponse {
  id: string;
  credentialsId: string;
  firstName: string;
  lastName: string;
  companyName: string | null;
  siretNumber: string | null;
  vatNumber: string | null;
  billingEmail: string | null;
  billingAddressLine1: string | null;
  billingAddressLine2: string | null;
  billingPostalCode: string | null;
  billingCity: string | null;
  billingCountry: string | null;
  createdAt: string;
  updatedAt: string | null;
}

export interface UpdateAccountPayload {
  firstName?: string | null;
  lastName?: string | null;
  companyName?: string | null;
  siretNumber?: string | null;
  vatNumber?: string | null;
  billingEmail?: string | null;
  billingAddressLine1?: string | null;
  billingAddressLine2?: string | null;
  billingPostalCode?: string | null;
  billingCity?: string | null;
  billingCountry?: string | null;
}
