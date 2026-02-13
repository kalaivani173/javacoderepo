import React, { useState, useEffect } from 'react';
import { useParams } from 'react-router-dom';
import { 
  FileText, 
  Clock, 
  CheckCircle, 
  XCircle, 
  AlertCircle,
  Download,
  RefreshCw,
  Search,
  List,
  History,
  Activity
} from 'lucide-react';
import { transactionApi } from '../services/api';

const TransactionLogs = () => {
  const { txnId } = useParams();
  const [transactionIds, setTransactionIds] = useState([]);
  const [selectedTxnId, setSelectedTxnId] = useState(txnId || '');
  const [searchTxnId, setSearchTxnId] = useState('');
  const [formattedLogs, setFormattedLogs] = useState([]);
  const [transactionStatus, setTransactionStatus] = useState(null);
  const [transactionStatuses, setTransactionStatuses] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);
  const [statusLoading, setStatusLoading] = useState({});

  useEffect(() => {
    fetchTransactionIds();
    if (txnId) {
      setSelectedTxnId(txnId);
      fetchTransactionDetails(txnId);
    }
  }, [txnId]);

  const fetchTransactionIds = async () => {
    try {
      const response = await transactionApi.getAllTransactionIds();
      const transactions = response.data || [];
      
      console.log('Fetched transactions for logs:', transactions);
      let transactionsData = transactions.map(txn => ({
          txnId: txn.txnId || txn.id || txn,
          status: txn.respPayStatus || 'PROCESSING',
          timestamp: txn.createdAt || new Date().toISOString()
        }));
      console.log(transactionsData, "DATA ")
      // Handle both formats: array of strings (old) or array of objects (new)
      // let transactionsData = transactions;
      // if (transactions.length > 0 && typeof transactions[0] === 'string') {
      //   // Old format: array of IDs only
      //   transactionsData = transactions.map(id => ({
      //     txnId: id,
      //     status: 'PROCESSING',
      //     timestamp: new Date().toISOString()
      //   }));
      // } else {
      //   // New format: array of objects with txnId, status, timestamp
      //   transactionsData = transactions.map(txn => ({
      //     txnId: txn.txnId || txn.id || txn,
      //     status: txn.status || 'PROCESSING',
      //     timestamp: txn.timestamp || new Date().toISOString(),
      //     payerVpa: txn.payerVpa,
      //     payeeVpa: txn.payeeVpa,
      //     amount: txn.amount
      //   }));
      // }
      
      // Sort by timestamp (most recent first)
      transactionsData.sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp));
      
      setTransactionIds(transactionsData);
      
      // Pre-populate transaction statuses
      const statusesMap = {};
      // transactionsData.forEach(txn => {
      //   statusesMap[txn.txnId] = {
      //     status: txn.status,
      //     timestamp: txn.timestamp,
      //     payerVpa: txn.payerVpa,
      //     payeeVpa: txn.payeeVpa,
      //     amount: txn.amount
      //   };
      // });
      // setTransactionStatuses(statusesMap);
      
      console.log('Transaction logs loaded:', transactionsData.length);
      
    } catch (error) {
      setError('Failed to fetch transaction IDs');
      console.error('Error fetching transaction IDs:', error);
    }
  };

  const fetchTransactionDetails = async (txnId) => {
    if (!txnId) return;
    
    setLoading(true);
    setError(null);
    
    try {
      const [logsResponse, formattedResponse, statusResponse] = await Promise.all([
        transactionApi.getTransactionLogs(txnId),
        transactionApi.getFormattedLogs(txnId),
        transactionApi.getTransactionStatus(txnId)
      ]);
      
      setFormattedLogs(formattedResponse.data || []);
      const logs = logsResponse.data || [];
      // Use status from /dashboard/status API (JSON { status } or legacy plain string)
      const statusStr = (typeof statusResponse.data === 'string' ? statusResponse.data : statusResponse.data?.status) || 'PENDING';
      
      setTransactionStatus({
        txnId,
        status: statusStr,
        logsCount: logs.length,
        timestamp: logs.length > 0 && logs[0].createdAt ? logs[0].createdAt : new Date().toISOString()
      });
    } catch (error) {
      setError('Failed to fetch transaction details');
      console.error('Error fetching transaction details:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchIndividualStatus = async (txnId) => {
    try {
      // Set loading state for this specific transaction
      setStatusLoading(prev => ({ ...prev, [txnId]: true }));
      
      // Fetch status from API
      const response = await transactionApi.getTransactionStatus(txnId);
      // /dashboard/status returns JSON { status } or legacy plain string
      const status = (typeof response.data === 'string' ? response.data : response.data?.status) || 'UNKNOWN';
      
      setTransactionStatuses(prev => ({
        ...prev,
        [txnId]: {
          status,
          timestamp: response.data?.timestamp,
          payerVpa: response.data?.payerVpa,
          payeeVpa: response.data?.payeeVpa,
          amount: response.data?.amount,
          logsCount: response.data?.logsCount
        }
      }));
      
    } catch (error) {
      console.error('Error fetching transaction status:', error);
      setTransactionStatuses(prev => ({
        ...prev,
        [txnId]: {
          status: 'ERROR',
          error: 'Failed to fetch status'
        }
      }));
    } finally {
      setStatusLoading(prev => ({ ...prev, [txnId]: false }));
    }
  };

  const handleTxnIdChange = (e) => {
    const value = e.target.value;
    setSelectedTxnId(value);
    setSearchTxnId(''); // Clear search when selecting from dropdown
    if (value) {
      fetchTransactionDetails(value);
    } else {
      setFormattedLogs([]);
      setTransactionStatus(null);
    }
  };

  const handleSearchTxnId = (e) => {
    const value = e.target.value;
    setSearchTxnId(value);
    setSelectedTxnId(''); // Clear dropdown selection when searching
    if (value) {
      fetchTransactionDetails(value);
    } else {
      setFormattedLogs([]);
      setTransactionStatus(null);
    }
  };

  const handleTransactionSelect = (txnId) => {
    setSelectedTxnId(txnId);
    setSearchTxnId(''); // Clear search when selecting from history
    fetchTransactionDetails(txnId);
  };

  const statusStr = (s) => typeof s === 'string' ? s : (s?.status || s?.respPayStatus || 'Unknown');

  const getStatusIcon = (status) => {
    const str = statusStr(status);
    switch (str?.toLowerCase()) {
      case 'success':
        return <CheckCircle size={16} className="status-success" />;
      case 'failure':
        return <XCircle size={16} className="status-error" />;
      case 'pending':
        return <Clock size={16} className="status-pending" />;
      default:
        return <AlertCircle size={16} />;
    }
  };

  const getStatusBadge = (status) => {
    const str = statusStr(status);
    if (!str) {
      return (
        <span className="status-badge status-pending">
          Unknown
        </span>
      );
    }
    
    const statusUpper = str.toUpperCase();
    let statusClass = 'status-pending';
    let displayText = str;
    
    // Success states
    if (statusUpper === 'SUCCESS' || statusUpper === 'COMPLETED') {
      statusClass = 'status-success';
      displayText = 'Success';
    }
    // Failure states
    else if (statusUpper === 'FAILURE' || statusUpper === 'FAILED' || statusUpper === 'ERROR' || statusUpper === 'DECLINED') {
      statusClass = 'status-error';
      displayText = 'Failed';
    }
    // Processing states
    else if (statusUpper === 'PROCESSING' || statusUpper === 'PENDING' || statusUpper === 'INITIATED') {
      statusClass = 'status-pending';
      displayText = statusUpper === 'INITIATED' ? 'Initiated' : 'Processing';
    }
    
    return (
      <span className={`status-badge ${statusClass}`}>
        {displayText}
      </span>
    );
  };

  const downloadLogs = () => {
    const logsText = formattedLogs.join('\n\n');
    const blob = new Blob([logsText], { type: 'text/plain' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = `transaction-${selectedTxnId}-logs.txt`;
    document.body.appendChild(a);
    a.click();
    document.body.removeChild(a);
    URL.revokeObjectURL(url);
  };
console.log(transactionIds, "transactionIds");
  return (
    <div className="container">
      <div className="page-header">
        <h1 className="page-title">Transaction Logs</h1>
        <p className="page-subtitle">
          Monitor and analyze UPI transaction logs
        </p>
      </div>

      {error && (
        <div className="alert alert-error">
          <AlertCircle size={16} style={{ marginRight: '8px' }} />
          {error}
        </div>
      )}

      {/* Search Transaction */}
      <div className="card">
        <h2 style={{ marginBottom: '20px', color: '#333' }}>
          <Search size={20} style={{ marginRight: '8px' }} />
          Search Transaction
        </h2>
        <div className="flex flex-between">
          <div style={{ flex: 1, marginRight: '20px' }}>
            <label className="form-label">Search by Transaction ID</label>
            <input
              type="text"
              value={searchTxnId}
              onChange={handleSearchTxnId}
              className="form-input"
              placeholder="Enter Transaction ID (e.g., TXN1759754168938PXKLS)"
            />
          </div>
          <div className="flex gap-10" style={{ alignItems: 'end' }}>
            <button
              onClick={() => fetchTransactionDetails(searchTxnId || selectedTxnId)}
              className="btn btn-secondary"
              disabled={(!searchTxnId && !selectedTxnId) || loading}
            >
              <RefreshCw size={16} style={{ marginRight: '8px' }} />
              Refresh
            </button>
            {formattedLogs.length > 0 && (
              <button
                onClick={downloadLogs}
                className="btn btn-success"
              >
                <Download size={16} style={{ marginRight: '8px' }} />
                Download
              </button>
            )}
          </div>
        </div>
      </div>

      {/* Transaction History */}
      <div className="card">
        <div className="flex flex-between" style={{ marginBottom: '20px' }}>
          <h2 style={{ color: '#333' }}>
            <History size={20} style={{ marginRight: '8px' }} />
            Transaction History
          </h2>
          <button
            onClick={fetchTransactionIds}
            className="btn btn-secondary"
            disabled={loading}
          >
            <RefreshCw size={16} style={{ marginRight: '8px' }} />
            Refresh
          </button>
        </div>
        {transactionIds.length === 0 ? (
          <div className="text-center" style={{ padding: '20px', color: '#666' }}>
            <List size={32} style={{ marginBottom: '12px', opacity: 0.5 }} />
            <p>No transactions available. Make a payment first.</p>
          </div>
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>Transaction ID</th>
                  <th>Status</th>
                  <th>Timestamp</th>
                  <th>Action</th>
                </tr>
              </thead>
              <tbody>
                {transactionIds.map((txn) => {
                  const id = txn.txnId || txn;
                  const txnStatus = txn.status || {};
                  const date=txn.timestamp;
                  return (
                    <tr key={id} className={selectedTxnId === id ? 'selected-row' : ''}>
                      <td>
                        <code style={{ fontSize: '12px' }}>{id}</code>
                      </td>
                      <td>
                        <div className="flex" style={{ alignItems: 'center', gap: '8px' }}>
                          {getStatusIcon(txnStatus)}
                          {getStatusBadge(txnStatus)}
                          {/* {txnStatus} */}
                        </div>
                      </td>
                      <td>
                        <div style={{ fontSize: '12px' }}>
                          {date}
                        </div>
                      </td>
                      <td>
                        <button
                          onClick={() => handleTransactionSelect(id)}
                          className="btn btn-sm btn-primary"
                          disabled={loading}
                        >
                          View Logs
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {(selectedTxnId || searchTxnId) && (
        <>
          {/* Transaction Summary */}
          <div className="card">
            <h2 style={{ marginBottom: '20px', color: '#333' }}>
              <FileText size={20} style={{ marginRight: '8px' }} />
              Transaction Summary
            </h2>
            
            <div className="grid grid-2">
              <div>
                <div className="transaction-detail">
                  <div className="transaction-detail-label">Transaction ID</div>
                  <div className="transaction-detail-value">
                    <code style={{ fontSize: '12px' }}>{selectedTxnId || searchTxnId}</code>
                  </div>
                </div>
                <div className="transaction-detail">
                  <div className="transaction-detail-label">Total Logs</div>
                  <div className="transaction-detail-value">
                    {formattedLogs.length} entries
                  </div>
                </div>
              </div>
              <div>
                <div className="transaction-detail">
                  <div className="transaction-detail-label">Status</div>
                  <div className="transaction-detail-value">
                    {transactionStatus ? getStatusBadge(transactionStatus.status) : getStatusBadge('PROCESSING')}
                  </div>
                </div>
                <div className="transaction-detail">
                  <div className="transaction-detail-label">Last Updated</div>
                  <div className="transaction-detail-value">
                    {transactionStatus?.timestamp ? new Date(transactionStatus.timestamp).toLocaleString() : new Date().toLocaleString()}
                  </div>
                </div>
              </div>
            </div>
          </div>

          {/* Logs */}
          <div className="card">
            <h2 style={{ marginBottom: '20px', color: '#333' }}>
              Logs
            </h2>
            
            {loading ? (
              <div className="loading">
                <div className="spinner"></div>
              </div>
            ) : formattedLogs.length === 0 ? (
              <div className="text-center" style={{ padding: '40px', color: '#666' }}>
                <FileText size={48} style={{ marginBottom: '16px', opacity: 0.5 }} />
                <p>No logs found for this transaction</p>
              </div>
            ) : (
              <div className="log-viewer">
                {formattedLogs.map((log, index) => (
                  <div key={index} className="log-entry">
                    {log}
                  </div>
                ))}
              </div>
            )}
          </div>
        </>
      )}

      {!selectedTxnId && !searchTxnId && (
        <div className="card">
          <div className="text-center" style={{ padding: '40px', color: '#666' }}>
            <FileText size={48} style={{ marginBottom: '16px', opacity: 0.5 }} />
            <p>No transaction selected</p>
            <p style={{ fontSize: '14px', marginTop: '8px' }}>
              {transactionIds.length === 0 
                ? 'No transactions available. Make a payment first.'
                : `Select a transaction from the history above or search by Transaction ID to view its logs`
              }
            </p>
          </div>
        </div>
      )}
    </div>
  );
};

export default TransactionLogs;


