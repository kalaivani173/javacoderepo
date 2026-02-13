import axios from 'axios';

// Base URLs for different services - using environment variables with fallback
const UPISIM_BASE_URL = process.env.REACT_APP_UPISIM_BASE_URL || 'http://localhost:8081';
const PAYER_PSP_BASE_URL = process.env.REACT_APP_PAYER_PSP_BASE_URL || 'http://localhost:8080';

// Create axios instances for different services
const upiSimApi = axios.create({
  baseURL: UPISIM_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

const payerPspApi = axios.create({
  baseURL: PAYER_PSP_BASE_URL,
  headers: {
    'Content-Type': 'application/xml',
  },
});

// PSP Bank Management APIs
export const bankApi = {
  // Get all banks
  getAllBanks: () => upiSimApi.get('/api/nodes'), 
  
  // Create new bank
  createBank: (bankData) => upiSimApi.post('/psp-banks', bankData),
  
  // Update bank
  updateBank: (id, bankData) => upiSimApi.put(`/psp-banks/${id}`, bankData),
  
  // Delete bank
  deleteBank: (id) => upiSimApi.delete(`/psp-banks/${id}`),
};

// Transaction APIs
export const transactionApi = {
  // Get all transaction IDs
  getAllTransactionIds: () => upiSimApi.get('/dashboard/all'),
  
  // Get transaction logs by ID
  getTransactionLogs: (txnId) => upiSimApi.get(`/dashboard/txn/${txnId}`),
  
  // Get formatted logs
  getFormattedLogs: (txnId) => upiSimApi.get(`/dashboard/logs/${txnId}`),
  
  // Get transaction status by ID
  getTransactionStatus: (txnId) => upiSimApi.get(`/dashboard/status/${txnId}`),
};

// Payment APIs
export const paymentApi = {
  // Initiate payment
  initiatePayment: (payerVpa, payeeVpa, amount) => {
    const params = new URLSearchParams({
      payerVpa,
      payeeVpa,
      amount: amount.toString(),
    });
    
    return payerPspApi.post(`/upi/Pay?${params}`, null, {
      headers: {
        'Content-Type': 'application/xml',
      },
      responseType: 'text', // Ensure we get the raw response as text
    });
  },
};

// Error handling interceptor for UPISim API
upiSimApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Server responded with error status
      console.error('UPISim API Error:', {
        status: error.response.status,
        statusText: error.response.statusText,
        data: error.response.data,
        url: error.config?.url
      });
    } else if (error.request) {
      // Request made but no response received
      console.error('UPISim API Error: No response received. Service may be unavailable:', {
        url: error.config?.url,
        baseURL: error.config?.baseURL
      });
      error.message = 'UPISim service unavailable. Please ensure the service is running on port 8081.';
    } else {
      // Error setting up request
      console.error('UPISim API Error:', error.message);
    }
    return Promise.reject(error);
  }
);

// Error handling interceptor for PayerPSP API
payerPspApi.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response) {
      // Server responded with error status
      const status = error.response.status;
      const data = error.response.data;
      
      console.error('Payment API Error:', {
        status: status,
        statusText: error.response.statusText,
        data: data,
        url: error.config?.url
      });
      
      // Provide user-friendly error messages
      if (status === 500) {
        error.userMessage = 'Internal server error. Please check if UPISim service is running.';
      } else if (status === 503) {
        error.userMessage = data || 'UPISim service unavailable. Please ensure UPISim is running on port 8081.';
      } else if (status === 502) {
        error.userMessage = data || 'Bad gateway. UPISim service returned an error.';
      } else {
        error.userMessage = data || `Payment request failed with status ${status}`;
      }
    } else if (error.request) {
      // Request made but no response received
      console.error('Payment API Error: No response received. Service may be unavailable:', {
        url: error.config?.url,
        baseURL: error.config?.baseURL
      });
      error.userMessage = 'PayerPSP service unavailable. Please ensure the service is running on port 8080.';
    } else {
      // Error setting up request
      console.error('Payment API Error:', error.message);
      error.userMessage = 'Failed to initiate payment request.';
    }
    return Promise.reject(error);
  }
);

export default {
  bankApi,
  transactionApi,
  paymentApi,
};


