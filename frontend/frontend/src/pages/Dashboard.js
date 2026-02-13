import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  CreditCard, 
  Building2, 
  FileText, 
  TrendingUp,
  Users,
  DollarSign,
  Activity,
  BarChart3,
  MessageSquare
} from 'lucide-react';
import { transactionApi, bankApi } from '../services/api';
import { 
  LineChart, 
  Line, 
  AreaChart, 
  Area, 
  XAxis, 
  YAxis, 
  CartesianGrid, 
  Tooltip, 
  Legend, 
  ResponsiveContainer,
  BarChart,
  Bar,
  PieChart,
  Pie,
  Cell
} from 'recharts';

const Dashboard = () => {
  const [stats, setStats] = useState({
    totalTransactions: 0,
    successTransactions: 0,
    failureTransactions: 0,
    totalBanks: 0,
  });
  const [recentTransactions, setRecentTransactions] = useState([]);
  const [loading, setLoading] = useState(true);
  const [kafkaMessages, setKafkaMessages] = useState([]);
  const [chartData, setChartData] = useState({
    dailyData: [],
    monthlyData: [],
    statusDistribution: []
  });
  const [transactionStatuses, setTransactionStatuses] = useState({});
  const [statusLoading, setStatusLoading] = useState({});

  useEffect(() => {
    fetchDashboardData();
    generateChartData();
    simulateKafkaMessages();
  }, []);

  const generateChartData = () => {
    // Generate daily data for last 7 days
    const dailyData = [];
    for (let i = 6; i >= 0; i--) {
      const date = new Date();
      date.setDate(date.getDate() - i);
      const dayName = date.toLocaleDateString('en-US', { weekday: 'short' });
      
      dailyData.push({
        day: dayName,
        success: Math.floor(Math.random() * 50) + 20,
        failure: Math.floor(Math.random() * 10) + 2,
        total: Math.floor(Math.random() * 60) + 25
      });
    }

    // Generate monthly data for last 6 months
    const monthlyData = [];
    for (let i = 5; i >= 0; i--) {
      const date = new Date();
      date.setMonth(date.getMonth() - i);
      const monthName = date.toLocaleDateString('en-US', { month: 'short' });
      
      monthlyData.push({
        month: monthName,
        success: Math.floor(Math.random() * 500) + 200,
        failure: Math.floor(Math.random() * 50) + 20,
        total: Math.floor(Math.random() * 600) + 250
      });
    }

    setChartData({
      dailyData,
      monthlyData,
      statusDistribution: [
        { name: 'Success', value: 85, color: '#28a745' },
        { name: 'Failure', value: 10, color: '#dc3545' },
        { name: 'Processing', value: 5, color: '#ffc107' }
      ]
    });
  };

  const simulateKafkaMessages = () => {
    const topics = ['upi-payments', 'transaction-logs', 'bank-notifications', 'fraud-detection'];
    const messages = [];
    
    for (let i = 0; i < 10; i++) {
      const topic = topics[Math.floor(Math.random() * topics.length)];
      const timestamp = new Date(Date.now() - Math.random() * 60000).toISOString();
      
      messages.push({
        id: i + 1,
        topic,
        message: `Transaction ${Math.random().toString(36).substr(2, 9).toUpperCase()} processed`,
        timestamp,
        partition: Math.floor(Math.random() * 3),
        offset: Math.floor(Math.random() * 1000)
      });
    }
    
    setKafkaMessages(messages);
  };

  const fetchDashboardData = async () => {
    console.log("fetching");
    try {
      setLoading(true);
      
      // Fetch all transactions and banks in parallel
      const [transactionResponse, banksResponse] = await Promise.all([
        transactionApi.getAllTransactionIds(),
        bankApi.getAllBanks()
      ]);
      
      const transactions = transactionResponse.data || [];
      const banks = banksResponse.data || [];
      
      console.log('Fetched transactions:', transactions);
      const transactionsData = transactions.map(txn => ({
          txnId: txn.txnId || txn.id || txn,
          status: txn.respPayStatus || 'DONNO',
          timestamp: txn.createdAt || new Date().toISOString()
        }));
console.log(transactionsData, "check")
      // If transactions is an array of strings (just IDs), convert to objects
      // let transactionsData = transactions;
      // if (transactions.length > 0 && typeof transactions[0] === 'string') {
      //   // Old format: array of IDs only
      //   transactionsData = transactions.map(id => ({
      //     txnId: id.txnId,
      //     status: id.respPayStatus,
      //     timestamp: id.createdAt
      //   }));
      // } else {
      //   // New format: array of objects with txnId, status, timestamp
      //   transactionsData = transactions.map(txn => ({
      //     txnId: txn.txnId || txn.id || txn,
      //     status: txn.respPayStatus || 'PROCESSING',
      //     timestamp: txn.createdAt || new Date().toISOString()
      //   }));
      // }
      console.log(transactionsData, "data");
      // Calculate transaction status counts
      let successCount = 0;
      let failureCount = 0;
      
      // transactionsData.forEach(txn => {
      //   const status = txn.status?.toUpperCase();
      //   if (status === 'SUCCESS' || status === 'COMPLETED') {
      //     successCount++;
      //   } else if (status === 'FAILURE' || status === 'FAILED' || status === 'DECLINED') {
      //     failureCount++;
      //   }
      // });
      
      // Calculate stats

      setStats({
        totalTransactions: transactionsData.length,
        successTransactions: successCount,
        failureTransactions: failureCount,
        totalBanks: banks.length,
      });

      // Get recent transactions (limit to 5 most recent)
      const recentTxnData = transactionsData
        .sort((a, b) => new Date(b.timestamp) - new Date(a.timestamp))
        .slice(0, 5)
        .map(txn => ({
          id: txn.txnId,
          status: txn.status,
          timestamp: txn.timestamp,
        }));
      
      setRecentTransactions(recentTxnData);
      
      // Pre-populate transaction statuses for any future refresh needs
      const statusesMap = {};
      recentTxnData.forEach(txn => {
        statusesMap[txn.id] = {
          status: txn.status,
          timestamp: txn.timestamp
        };
      });
      setTransactionStatuses(statusesMap);
      
      console.log('Recent transactions loaded:', recentTxnData.length);
      
    } catch (error) {
      console.error('Error fetching dashboard data:', error);
    } finally {
      setLoading(false);
    }
  };

  const fetchTransactionStatus = async (txnId) => {
    try {
      // Set loading state for this specific transaction
      setStatusLoading(prev => ({ ...prev, [txnId]: true }));
      
      // Fetch status from API
      const response = await transactionApi.getTransactionStatus(txnId);
      const statusData = response.data;
      
      // Update the status for this transaction
      setTransactionStatuses(prev => ({
        ...prev,
        [txnId]: {
          status: statusData.status || 'UNKNOWN',
          timestamp: statusData.timestamp,
          payerVpa: statusData.payerVpa,
          payeeVpa: statusData.payeeVpa,
          amount: statusData.amount
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

  const getStatusBadge = (status) => {
    if (!status) {
      return (
        <span className="status-badge status-pending">
          Unknown
        </span>
      );
    }
    
    const statusUpper = status.toUpperCase();
    let statusClass = 'status-pending';
    let displayText = status;
    
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

  const statCards = [
    {
      title: 'Total Transactions',
      value: stats.totalTransactions,
      icon: CreditCard,
      color: '#667eea',
    },
    {
      title: 'Registered Banks',
      value: stats.totalBanks,
      icon: Building2,
      color: '#ffc107',
    },
  ];

  if (loading) {
    return (
      <div className="container">
        <div className="loading">
          <div className="spinner"></div>
        </div>
      </div>
    );
  }
console.log(recentTransactions, "recents");
  return (
    <div className="container">
      <div className="page-header">
        <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4">
          <div>
            <h1 className="page-title">Dashboard</h1>
            <p className="page-subtitle">Monitor your UPI transaction activity</p>
          </div>
          <button
            onClick={fetchDashboardData}
            className="btn btn-secondary"
            disabled={loading}
          >
            <Activity size={16} className="mr-2" />
            Refresh
          </button>
        </div>
      </div>

      {/* Main Dashboard Layout - Two Columns */}
      <div className="dashboard-layout">
        {/* Left Column - Main Dashboard */}
        <div className="dashboard-left">
          {/* Stats Grid */}
          <div className="stats-grid">
            {statCards.map((stat, index) => {
              const IconComponent = stat.icon;
              return (
                <div key={index} className="stat-card">
                  <div className="stat-icon" style={{ background: stat.color }}>
                    <IconComponent size={24} />
                  </div>
                  <div className="stat-value">{stat.value}</div>
                  <div className="stat-label">{stat.title}</div>
                </div>
              );
            })}
          </div>

          {/* Quick Actions */}
          <div className="card">
            <h2 className="text-xl font-bold text-gray-800 mb-6">Quick Actions</h2>
            <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              <Link to="/payment" className="btn w-full justify-center">
                <CreditCard size={20} className="mr-2" />
                Make Payment
              </Link>
              <Link to="/banks" className="btn btn-secondary w-full justify-center">
                <Building2 size={20} className="mr-2" />
                Manage Banks
              </Link>
              <Link to="/transactions" className="btn btn-success w-full justify-center">
                <FileText size={20} className="mr-2" />
                View Logs
              </Link>
            </div>
          </div>

          {/* Recent Transactions */}
          <div className="card">
            <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
              <h2 className="text-xl font-bold text-gray-800">Recent Transactions</h2>
              <Link to="/transactions" className="btn btn-secondary btn-sm">
                View All
              </Link>
            </div>
            
            {recentTransactions.length === 0 ? (
              <div className="text-center py-12">
                <FileText size={48} className="mx-auto mb-4 text-gray-400" />
                <p className="text-gray-600 font-medium">No transactions found</p>
                <p className="text-sm text-gray-500 mt-2">
                  Start by making a payment to see transaction logs
                </p>
              </div>
            ) : (
              <div className="overflow-x-auto">
                <table className="table">
                  <thead>
                    <tr>
                      <th>Transaction ID</th>
                      <th className="hidden sm:table-cell">Status</th>
                      <th className="hidden md:table-cell">Timestamp</th>
                      <th>Actions</th>
                    </tr>
                  </thead>
                  <tbody>
                    {recentTransactions.map((txn) => (
                      <tr key={txn.id}>
                        <td>
                          <code className="text-xs font-mono text-gray-700 block truncate max-w-xs">
                            {txn.id}
                          </code>
                        </td>
                        <td className="hidden sm:table-cell">
                          {getStatusBadge(txn.status)}
                        </td>
                        <td className="hidden md:table-cell text-sm text-gray-600">
                          {txn.timestamp}
                        </td>
                        <td>
                          <Link 
                            to={`/transactions/${txn.id}`}
                            className="btn btn-sm btn-primary"
                          >
                            <FileText size={14} className="sm:mr-1" />
                            <span className="hidden sm:inline">View</span>
                          </Link>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </div>

        </div>

      </div>
    </div>
  );
};

export default Dashboard;


