import React, { useState, useEffect, useRef } from 'react';
import { Link } from 'react-router-dom';
import { CreditCard, User, ArrowRight, CheckCircle, AlertCircle, Lock, X, Eye, Check } from 'lucide-react';
import { paymentApi } from '../services/api';

const PaymentForm = () => {
  const [formData, setFormData] = useState({
    payerVpa: '',
    payeeVpa: '',
    amount: '',
    note: '',
  });
  const [loading, setLoading] = useState(false);
  const [paymentResult, setPaymentResult] = useState(null);
  const [error, setError] = useState(null);
  const [showPinModal, setShowPinModal] = useState(false);
  const [pin, setPin] = useState('');
  const [pinError, setPinError] = useState('');
  const [showPin, setShowPin] = useState(false);
  const pinModalRef = useRef(null);

  // Auto-focus the PIN modal when it opens
  useEffect(() => {
    if (showPinModal && pinModalRef.current) {
      pinModalRef.current.focus();
    }
  }, [showPinModal]);

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const validateForm = () => {
    if (!formData.payerVpa.trim()) {
      setError('Payer VPA is required');
      return false;
    }
    if (!formData.payeeVpa.trim()) {
      setError('Payee VPA is required');
      return false;
    }
    if (!formData.amount || parseFloat(formData.amount) <= 0) {
      setError('Valid amount is required');
      return false;
    }
    if (!formData.payerVpa.includes('@')) {
      setError('Payer VPA must contain @ symbol');
      return false;
    }
    if (!formData.payeeVpa.includes('@')) {
      setError('Payee VPA must contain @ symbol');
      return false;
    }
    return true;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setPaymentResult(null);

    if (!validateForm()) {
      return;
    }

    // Show PIN modal instead of directly processing payment
    setShowPinModal(true);
    setPin('');
    setPinError('');
  };

  const processPayment = async () => {
    // Validate PIN (4 digits)
    if (pin !== '1234') {
      setPinError('Incorrect PIN. Please try again.');
      return;
    }

    // Close PIN modal and proceed with payment
    setShowPinModal(false);
    setLoading(true);
    
    try {
      const response = await paymentApi.initiatePayment(
        formData.payerVpa,
        formData.payeeVpa,
        parseFloat(formData.amount)
      );
      
      // Debug: Log the full response to understand the structure
      console.log('Payment API Response:', {
        status: response.status,
        statusText: response.statusText,
        headers: response.headers,
        data: response.data,
        dataType: typeof response.data
      });
      
      // Parse response - can be plain string (transaction ID) or XML
      const responseData = response.data;
      
      // Handle null/undefined response
      if (!responseData) {
        throw new Error('Empty response received from payment API');
      }
      
      let result = 'SUCCESS';
      let responseCode = '00';
      let txnId = '';
      
      // Check if response is a plain string (transaction ID) or XML
      if (typeof responseData === 'string') {
        // Check if it's XML format
        if (responseData.trim().startsWith('<') || responseData.includes('</')) {
          // It's XML - parse it
          const resultMatch = responseData.match(/<result>(.*?)<\/result>/);
          const responseCodeMatch = responseData.match(/<responseCode>(.*?)<\/responseCode>/);
          const txnIdMatch = responseData.match(/<txnId>(.*?)<\/txnId>/);
          
          result = resultMatch ? resultMatch[1] : 'SUCCESS';
          responseCode = responseCodeMatch ? responseCodeMatch[1] : '00';
          txnId = txnIdMatch ? txnIdMatch[1] : '';
        } else {
          // It's a plain string - use it directly as transaction ID
          txnId = responseData.trim();
          result = 'SUCCESS';
          responseCode = '00';
        }
      } else {
        // Response is not a string, try to extract txnId from object
        txnId = responseData?.txnId || responseData?.id || '';
      }
      
      // Validate that we have a transaction ID
      if (!txnId && result === 'SUCCESS') {
        // If we got success but no txnId, try to extract from response
        console.warn('Transaction ID not found in response, attempting to extract from response data:', responseData);
        // If responseData itself looks like a transaction ID (alphanumeric string)
        if (typeof responseData === 'string' && responseData.trim().length > 0 && !responseData.includes('<')) {
          txnId = responseData.trim();
        }
      }
      
      // Determine message based on result and response code
      let message = '';
      let isSuccess = result === 'SUCCESS' && txnId.length > 0;
      
      if (isSuccess) {
        message = `Payment initiated successfully! Transaction ID: ${txnId}`;
      } else if (!txnId) {
        message = 'Payment request received but failed to fetch transaction ID. Please check the response.';
        isSuccess = false;
      } else {
        // Handle different error codes
        switch(responseCode) {
          case 'ZM':
            message = `Payment declined by payee bank (Code: ${responseCode}). Transaction ID: ${txnId}`;
            break;
          case 'U30':
            message = `Payment failed - Insufficient funds (Code: ${responseCode}). Transaction ID: ${txnId}`;
            break;
          default:
            message = `Payment failed (Code: ${responseCode}). Transaction ID: ${txnId}`;
        }
      }
      
      setPaymentResult({
        success: isSuccess,
        message: message,
        response: responseData,
        txnId: txnId,
        responseCode: responseCode
      });
      
      // Only reset form on success
      if (isSuccess) {
        setFormData({
          payerVpa: '',
          payeeVpa: '',
          amount: '',
          note: '',
        });
      }
      
    } catch (error) {
      // Use user-friendly error message from interceptor if available
      const errorMessage = error.userMessage || error.response?.data || error.message || 'Payment request failed. Please try again.';
      setPaymentResult({
        success: false,
        message: errorMessage,
        error: error.response?.data || error.message,
      });
    } finally {
      setLoading(false);
    }
  };

  const handleClosePinModal = () => {
    setShowPinModal(false);
    setPin('');
    setPinError('');
    setShowPin(false);
  };

  const handlePinKeypadPress = (value) => {
    setPinError('');
    
    if (value === 'backspace') {
      setPin(pin.slice(0, -1));
    } else if (value === 'confirm' || value === 'enter') {
      if (pin.length === 4) {
        processPayment();
      }
    } else if (pin.length < 4 && /^[0-9]$/.test(value)) {
      setPin(pin + value);
    }
  };

  const handleKeyboardInput = (e) => {
    setPinError('');
    
    if (e.key >= '0' && e.key <= '9' && pin.length < 4) {
      setPin(pin + e.key);
    } else if (e.key === 'Backspace') {
      setPin(pin.slice(0, -1));
    } else if (e.key === 'Enter' && pin.length === 4) {
      processPayment();
    } else if (e.key === 'Escape') {
      handleClosePinModal();
    }
  };

  const sampleVpas = [
    'user@okaxis',
    'merchant@paytm',
    'customer@phonepe',
    'shop@googlepay',
    'decline@upi', // Simulator VPA - Always fails with ZM response code
  ];

  return (
    <div className="container">
      <div className="page-header">
        <h1 className="page-title">Make Payment</h1>
        <p className="page-subtitle">
          Initiate a UPI payment transaction securely
        </p>
      </div>

      <div className="grid grid-2" style={{ marginBottom: '30px' }}>
        {/* Payment Form */}
        <div className="card" style={{ height: 'fit-content' }}>
          <h2 className="text-xl font-bold text-gray-800 mb-6 flex items-center">
            <CreditCard size={20} className="mr-2" />
            Payment Details
          </h2>

          {error && (
            <div className="alert alert-error">
              <AlertCircle size={16} style={{ marginRight: '8px' }} />
              {error}
            </div>
          )}

          {paymentResult && (
            <div>
              <div className={`alert ${paymentResult.success ? 'alert-success' : 'alert-error'}`}>
                {paymentResult.success ? (
                  <CheckCircle size={16} style={{ marginRight: '8px' }} />
                ) : (
                  <AlertCircle size={16} style={{ marginRight: '8px' }} />
                )}
                {paymentResult.message}
              </div>
              {paymentResult.txnId && (
                <div style={{ marginTop: '10px', padding: '10px', backgroundColor: '#f8f9fa', borderRadius: '4px' }}>
                  <p style={{ margin: '0 0 8px 0', fontSize: '14px', color: '#666' }}>
                    <strong>Transaction Details:</strong>
                  </p>
                  <p style={{ margin: '0 0 5px 0', fontSize: '13px' }}>
                    <strong>Transaction ID:</strong> <code style={{ fontSize: '12px' }}>{paymentResult.txnId}</code>
                  </p>
                  {paymentResult.responseCode && (
                    <p style={{ margin: '0 0 5px 0', fontSize: '13px' }}>
                      <strong>Response Code:</strong> <code style={{ fontSize: '12px' }}>{paymentResult.responseCode}</code>
                    </p>
                  )}
                  <Link 
                    to={`/transactions/${paymentResult.txnId}`}
                    className="btn btn-sm"
                    style={{ 
                      marginTop: '8px', 
                      padding: '6px 12px', 
                      fontSize: '12px',
                      textDecoration: 'none',
                      display: 'inline-block'
                    }}
                  >
                    View Transaction Logs
                  </Link>
                </div>
              )}
            </div>
          )}

          <form onSubmit={handleSubmit} className="payment-form">
            <div className="form-group">
              <label className="form-label">
                <User size={16} style={{ marginRight: '8px' }} />
                Payer VPA (Virtual Payment Address)
              </label>
              <input
                type="text"
                name="payerVpa"
                value={formData.payerVpa}
                onChange={handleInputChange}
                className="form-input"
                placeholder="e.g., user@okaxis"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">
                <User size={16} style={{ marginRight: '8px' }} />
                Payee VPA (Virtual Payment Address)
              </label>
              <input
                type="text"
                name="payeeVpa"
                value={formData.payeeVpa}
                onChange={handleInputChange}
                className="form-input"
                placeholder="e.g., merchant@paytm"
                required
              />
            </div>

            <div className="form-group">
              <label className="form-label">Amount (₹)</label>
              <div className="amount-input">
                <span className="currency-symbol">₹</span>
                <input
                  type="number"
                  name="amount"
                  value={formData.amount}
                  onChange={handleInputChange}
                  className="form-input"
                  placeholder="0.00"
                  step="0.01"
                  min="0.01"
                  required
                />
              </div>
            </div>

            <div className="form-group">
              <label className="form-label">Note (Optional)</label>
              <input
                type="text"
                name="note"
                value={formData.note}
                onChange={handleInputChange}
                className="form-input"
                placeholder="Payment for..."
              />
            </div>

            <button
              type="submit"
              className="btn"
              disabled={loading}
              style={{ width: '100%', marginTop: '20px' }}
            >
              {loading ? (
                <>
                  <div className="spinner" style={{ width: '16px', height: '16px', marginRight: '8px' }}></div>
                  Processing...
                </>
              ) : (
                <>
                  <ArrowRight size={16} style={{ marginRight: '8px' }} />
                  Initiate Payment
                </>
              )}
            </button>
          </form>
        </div>

        {/* Sample VPAs and Bank Info */}
        <div className="card" style={{ height: 'fit-content' }}>
          <h2 className="text-xl font-bold text-gray-800 mb-6">
            Sample VPAs
          </h2>
          
          <div className="mb-8">
            <p className="mb-4 text-gray-600 text-sm">
              Try these sample VPAs for testing:
            </p>
            <div className="grid grid-cols-1 gap-3">
              {sampleVpas.map((vpa, index) => {
                const isDeclineVpa = vpa === 'decline@upi';
                return (
                  <div
                    key={index}
                    className="bank-item"
                    onClick={() => {
                      if (isDeclineVpa) {
                        // decline@upi should only be used as Payee
                        setFormData(prev => ({ ...prev, payeeVpa: vpa }));
                      } else if (index % 2 === 0) {
                        setFormData(prev => ({ ...prev, payerVpa: vpa }));
                      } else {
                        setFormData(prev => ({ ...prev, payeeVpa: vpa }));
                      }
                    }}
                    style={{ 
                      cursor: 'pointer',
                      borderColor: isDeclineVpa ? '#dc3545' : undefined,
                      borderWidth: isDeclineVpa ? '2px' : undefined
                    }}
                  >
                    <div className="bank-name">
                      {vpa}
                      {isDeclineVpa && <span style={{ marginLeft: '8px', fontSize: '10px', color: '#dc3545' }}>⚠️ Test VPA</span>}
                    </div>
                    <div className="bank-details">
                      {isDeclineVpa 
                        ? 'Click to use as Payee (Always fails with ZM)' 
                        : (index % 2 === 0 ? 'Click to use as Payer' : 'Click to use as Payee')
                      }
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </div>
      </div>

      {/* PIN Entry Modal */}
      {showPinModal && (
        <div 
          style={{
            position: 'fixed',
            top: 0,
            left: 0,
            right: 0,
            bottom: 0,
            backgroundColor: 'rgba(0, 0, 0, 0.5)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            zIndex: 1000,
            animation: 'fadeIn 0.3s ease'
          }}
          onClick={(e) => {
            if (e.target === e.currentTarget) {
              handleClosePinModal();
            }
          }}
        >
          <div 
            ref={pinModalRef}
            style={{
              maxWidth: '420px',
              width: '90%',
              backgroundColor: '#ffffff',
              borderRadius: '16px',
              overflow: 'hidden',
              boxShadow: '0 10px 40px rgba(0, 0, 0, 0.2)',
              display: 'flex',
              flexDirection: 'column',
              height: '600px',
              outline: 'none'
            }}
            onKeyDown={handleKeyboardInput}
            tabIndex={0}
          >
            {/* Header - Dark Blue */}
            <div style={{
              backgroundColor: '#1e3a8a',
              padding: '16px 20px',
              display: 'flex',
              justifyContent: 'space-between',
              alignItems: 'center',
              color: 'white'
            }}>
              <div style={{ fontSize: '18px', fontWeight: '600' }}>UPI Payment</div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                <div style={{
                  backgroundColor: 'white',
                  padding: '4px 12px',
                  borderRadius: '4px',
                  fontSize: '12px',
                  fontWeight: 'bold',
                  color: '#1e3a8a'
                }}>
                  UPI
                </div>
                <button
                  onClick={handleClosePinModal}
                  style={{
                    background: 'none',
                    border: 'none',
                    color: 'white',
                    cursor: 'pointer',
                    padding: '4px',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    borderRadius: '4px',
                    transition: 'background 0.2s'
                  }}
                  onMouseOver={(e) => e.currentTarget.style.background = 'rgba(255,255,255,0.2)'}
                  onMouseOut={(e) => e.currentTarget.style.background = 'none'}
                >
                  <X size={24} />
                </button>
              </div>
            </div>

            {/* PIN Input Section */}
            <div style={{
              flex: '1',
              backgroundColor: 'white',
              padding: '40px 20px 20px',
              display: 'flex',
              flexDirection: 'column',
              alignItems: 'center'
            }}>
              {/* Enter UPI PIN Header */}
              <div style={{
                display: 'flex',
                alignItems: 'center',
                gap: '12px',
                marginBottom: '24px'
              }}>
                <span style={{
                  fontSize: '14px',
                  color: '#666',
                  fontWeight: '500',
                  letterSpacing: '1px'
                }}>
                  ENTER UPI PIN
                </span>
                <button
                  onClick={() => setShowPin(!showPin)}
                  style={{
                    background: 'none',
                    border: 'none',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    gap: '4px',
                    color: '#3b82f6',
                    fontSize: '12px',
                    fontWeight: '600'
                  }}
                >
                  <Eye size={18} />
                  {showPin ? 'HIDE' : 'SHOW'}
                </button>
              </div>

              {/* PIN Dots */}
              <div style={{
                display: 'flex',
                gap: '20px',
                marginBottom: '16px'
              }}>
                {[0, 1, 2, 3].map((index) => (
                  <div
                    key={index}
                    style={{
                      width: showPin ? 'auto' : '14px',
                      minWidth: showPin ? '24px' : '14px',
                      height: '4px',
                      backgroundColor: index < pin.length ? '#1e3a8a' : '#cbd5e1',
                      borderRadius: '2px',
                      fontSize: showPin ? '28px' : '0',
                      fontWeight: 'bold',
                      color: '#1e3a8a',
                      textAlign: 'center',
                      lineHeight: '1'
                    }}
                  >
                    {showPin && pin[index] ? pin[index] : ''}
                  </div>
                ))}
              </div>

              {/* Error Message */}
              {pinError && (
                <div style={{
                  color: '#dc2626',
                  fontSize: '13px',
                  marginTop: '8px',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px'
                }}>
                  <AlertCircle size={16} />
                  {pinError}
                </div>
              )}

              {/* Payment Summary */}
              <div style={{
                marginTop: '24px',
                padding: '16px',
                backgroundColor: '#f1f5f9',
                borderRadius: '8px',
                width: '100%',
                fontSize: '13px'
              }}>
                <div style={{ marginBottom: '8px', color: '#64748b' }}>
                  <strong>Payment Details:</strong>
                </div>
                <div style={{ marginBottom: '4px', color: '#475569' }}>
                  To: <code style={{ backgroundColor: 'white', padding: '2px 6px', borderRadius: '4px' }}>{formData.payeeVpa}</code>
                </div>
                <div style={{ color: '#475569' }}>
                  Amount: <strong style={{ color: '#1e3a8a', fontSize: '16px' }}>₹{formData.amount}</strong>
                </div>
              </div>
            </div>

            {/* Numeric Keypad */}
            <div style={{
              backgroundColor: '#e2e8f0',
              padding: '16px',
              borderTop: '1px solid #cbd5e1'
            }}>
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(4, 1fr)',
                gap: '12px'
              }}>
                {/* Row 1 */}
                {['1', '2', '3', '-'].map((key) => (
                  <button
                    key={key}
                    onClick={() => key !== '-' && handlePinKeypadPress(key)}
                    disabled={key === '-'}
                    style={{
                      padding: '16px',
                      fontSize: '20px',
                      fontWeight: '600',
                      backgroundColor: key === '-' ? '#cbd5e1' : 'white',
                      border: 'none',
                      borderRadius: '8px',
                      cursor: key === '-' ? 'default' : 'pointer',
                      color: key === '-' ? '#94a3b8' : '#1e293b',
                      boxShadow: key === '-' ? 'none' : '0 2px 4px rgba(0,0,0,0.1)',
                      transition: 'all 0.2s'
                    }}
                    onMouseOver={(e) => {
                      if (key !== '-') {
                        e.currentTarget.style.backgroundColor = '#f1f5f9';
                        e.currentTarget.style.transform = 'scale(0.98)';
                      }
                    }}
                    onMouseOut={(e) => {
                      if (key !== '-') {
                        e.currentTarget.style.backgroundColor = 'white';
                        e.currentTarget.style.transform = 'scale(1)';
                      }
                    }}
                  >
                    {key}
                  </button>
                ))}

                {/* Row 2 */}
                {['4', '5', '6', ']'].map((key) => (
                  <button
                    key={key}
                    onClick={() => key !== ']' && handlePinKeypadPress(key)}
                    disabled={key === ']'}
                    style={{
                      padding: '16px',
                      fontSize: '20px',
                      fontWeight: '600',
                      backgroundColor: key === ']' ? '#cbd5e1' : 'white',
                      border: 'none',
                      borderRadius: '8px',
                      cursor: key === ']' ? 'default' : 'pointer',
                      color: key === ']' ? '#94a3b8' : '#1e293b',
                      boxShadow: key === ']' ? 'none' : '0 2px 4px rgba(0,0,0,0.1)',
                      transition: 'all 0.2s'
                    }}
                    onMouseOver={(e) => {
                      if (key !== ']') {
                        e.currentTarget.style.backgroundColor = '#f1f5f9';
                        e.currentTarget.style.transform = 'scale(0.98)';
                      }
                    }}
                    onMouseOut={(e) => {
                      if (key !== ']') {
                        e.currentTarget.style.backgroundColor = 'white';
                        e.currentTarget.style.transform = 'scale(1)';
                      }
                    }}
                  >
                    {key}
                  </button>
                ))}

                {/* Row 3 */}
                {['7', '8', '9'].map((key) => (
                  <button
                    key={key}
                    onClick={() => handlePinKeypadPress(key)}
                    style={{
                      padding: '16px',
                      fontSize: '20px',
                      fontWeight: '600',
                      backgroundColor: 'white',
                      border: 'none',
                      borderRadius: '8px',
                      cursor: 'pointer',
                      color: '#1e293b',
                      boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                      transition: 'all 0.2s'
                    }}
                    onMouseOver={(e) => {
                      e.currentTarget.style.backgroundColor = '#f1f5f9';
                      e.currentTarget.style.transform = 'scale(0.98)';
                    }}
                    onMouseOut={(e) => {
                      e.currentTarget.style.backgroundColor = 'white';
                      e.currentTarget.style.transform = 'scale(1)';
                    }}
                  >
                    {key}
                  </button>
                ))}
                <button
                  onClick={() => handlePinKeypadPress('backspace')}
                  style={{
                    padding: '16px',
                    fontSize: '20px',
                    backgroundColor: '#93c5fd',
                    border: 'none',
                    borderRadius: '8px',
                    cursor: 'pointer',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
                    transition: 'all 0.2s'
                  }}
                  onMouseOver={(e) => {
                    e.currentTarget.style.backgroundColor = '#60a5fa';
                    e.currentTarget.style.transform = 'scale(0.98)';
                  }}
                  onMouseOut={(e) => {
                    e.currentTarget.style.backgroundColor = '#93c5fd';
                    e.currentTarget.style.transform = 'scale(1)';
                  }}
                >
                  <X size={24} color="#1e293b" />
                </button>

                {/* Row 4 */}
                {[',', '0', '.'].map((key) => (
                  <button
                    key={key}
                    onClick={() => (key === '0' || key === ',') && handlePinKeypadPress(key === ',' ? '' : key)}
                    disabled={key === ',' || key === '.'}
                    style={{
                      padding: '16px',
                      fontSize: '20px',
                      fontWeight: '600',
                      backgroundColor: (key === ',' || key === '.') ? '#cbd5e1' : 'white',
                      border: 'none',
                      borderRadius: '8px',
                      cursor: (key === ',' || key === '.') ? 'default' : 'pointer',
                      color: (key === ',' || key === '.') ? '#94a3b8' : '#1e293b',
                      boxShadow: (key === ',' || key === '.') ? 'none' : '0 2px 4px rgba(0,0,0,0.1)',
                      transition: 'all 0.2s'
                    }}
                    onMouseOver={(e) => {
                      if (key === '0') {
                        e.currentTarget.style.backgroundColor = '#f1f5f9';
                        e.currentTarget.style.transform = 'scale(0.98)';
                      }
                    }}
                    onMouseOut={(e) => {
                      if (key === '0') {
                        e.currentTarget.style.backgroundColor = 'white';
                        e.currentTarget.style.transform = 'scale(1)';
                      }
                    }}
                  >
                    {key}
                  </button>
                ))}
                <button
                  onClick={() => handlePinKeypadPress('confirm')}
                  disabled={pin.length !== 4}
                  style={{
                    padding: '16px',
                    fontSize: '20px',
                    backgroundColor: pin.length === 4 ? '#3b82f6' : '#cbd5e1',
                    border: 'none',
                    borderRadius: '8px',
                    cursor: pin.length === 4 ? 'pointer' : 'default',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    boxShadow: pin.length === 4 ? '0 2px 4px rgba(0,0,0,0.1)' : 'none',
                    transition: 'all 0.2s'
                  }}
                  onMouseOver={(e) => {
                    if (pin.length === 4) {
                      e.currentTarget.style.backgroundColor = '#2563eb';
                      e.currentTarget.style.transform = 'scale(0.98)';
                    }
                  }}
                  onMouseOut={(e) => {
                    if (pin.length === 4) {
                      e.currentTarget.style.backgroundColor = '#3b82f6';
                      e.currentTarget.style.transform = 'scale(1)';
                    }
                  }}
                >
                  <Check size={24} color={pin.length === 4 ? 'white' : '#94a3b8'} />
                </button>
              </div>

              {/* Cancel Button */}
              <button
                onClick={handleClosePinModal}
                style={{
                  marginTop: '12px',
                  width: '100%',
                  padding: '14px',
                  backgroundColor: 'white',
                  border: '1px solid #cbd5e1',
                  borderRadius: '8px',
                  cursor: 'pointer',
                  fontSize: '15px',
                  fontWeight: '600',
                  color: '#64748b',
                  transition: 'all 0.2s'
                }}
                onMouseOver={(e) => {
                  e.currentTarget.style.backgroundColor = '#f8fafc';
                }}
                onMouseOut={(e) => {
                  e.currentTarget.style.backgroundColor = 'white';
                }}
              >
                Cancel
              </button>

              {/* Enter Button */}
              <button
                onClick={() => handlePinKeypadPress('enter')}
                disabled={pin.length !== 4}
                style={{
                  marginTop: '12px',
                  width: '100%',
                  padding: '14px',
                  backgroundColor: pin.length === 4 ? '#10b981' : '#e5e7eb',
                  border: 'none',
                  borderRadius: '8px',
                  cursor: pin.length === 4 ? 'pointer' : 'default',
                  fontSize: '15px',
                  fontWeight: '600',
                  color: pin.length === 4 ? 'white' : '#9ca3af',
                  transition: 'all 0.2s',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'center',
                  gap: '8px'
                }}
                onMouseOver={(e) => {
                  if (pin.length === 4) {
                    e.currentTarget.style.backgroundColor = '#059669';
                  }
                }}
                onMouseOut={(e) => {
                  if (pin.length === 4) {
                    e.currentTarget.style.backgroundColor = '#10b981';
                  }
                }}
              >
                <Check size={18} />
                Enter (Confirm Payment)
              </button>

              {/* Test PIN Note */}
              <div style={{
                marginTop: '12px',
                textAlign: 'center',
                fontSize: '12px',
                color: '#64748b'
              }}>
                💡 Test PIN: 1234 | Press Enter or click ✓ to confirm
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default PaymentForm;


