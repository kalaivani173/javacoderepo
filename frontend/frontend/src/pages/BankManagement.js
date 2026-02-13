import React, { useState, useEffect } from 'react';
import { 
  Building2, 
  Plus, 
  Edit, 
  Trash2, 
  Save, 
  X,
  Search,
  Filter
} from 'lucide-react';
import { bankApi } from '../services/api';

const BankManagement = () => {
  const [banks, setBanks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [showModal, setShowModal] = useState(false);
  const [editingBank, setEditingBank] = useState(null);
  const [searchTerm, setSearchTerm] = useState('');
  const [error, setError] = useState(null);
  const [success, setSuccess] = useState(null);

  const [formData, setFormData] = useState({
    name: '',
    orgId: '',
    iin: '',
    ifsc: '',
    handle: '',
    bankUrl: '',
    bankCode: '',
  });

  useEffect(() => {
    fetchBanks();
  }, []);

  const fetchBanks = async () => {
    try {
      setLoading(true);
      const response = await bankApi.getAllBanks();
      setBanks(response.data || []);
    } catch (error) {
      setError('Failed to fetch banks');
      console.error('Error fetching banks:', error);
    } finally {
      setLoading(false);
    }
  };

  const handleInputChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: value,
    }));
  };

  const resetForm = () => {
    setFormData({
      name: '',
      orgId: '',
      iin: '',
      ifsc: '',
      handle: '',
      bankUrl: '',
      bankCode: '',
    });
    setEditingBank(null);
    setError(null);
    setSuccess(null);
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError(null);
    setSuccess(null);

    try {
      if (editingBank) {
        await bankApi.updateBank(editingBank.id, formData);
        setSuccess('Bank updated successfully!');
      } else {
        await bankApi.createBank(formData);
        setSuccess('Bank created successfully!');
      }
      
      resetForm();
      setShowModal(false);
      fetchBanks();
    } catch (error) {
      setError(error.response?.data?.message || 'Failed to save bank');
    }
  };

  const handleEdit = (bank) => {
    setEditingBank(bank);
    setFormData({
      name: bank.name || '',
      orgId: bank.orgId || '',
      iin: bank.iin || '',
      ifsc: bank.ifsc || '',
      handle: bank.handle || '',
      bankUrl: bank.bankUrl || '',
      bankCode: bank.bankCode || '',
    });
    setShowModal(true);
  };

  const handleDelete = async (id) => {
    if (window.confirm('Are you sure you want to delete this bank?')) {
      try {
        await bankApi.deleteBank(id);
        setSuccess('Bank deleted successfully!');
        fetchBanks();
      } catch (error) {
        setError('Failed to delete bank');
      }
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
    let statusClass = 'up';
    let displayText = status;
    
    // Success states
    if (statusUpper === 'UP' || statusUpper === 'COMPLETED') {
      statusClass = 'status-success';
      displayText = 'UP';
    }
    // Failure states
    else if (statusUpper === 'DOWN' || statusUpper === 'FAILED' || statusUpper === 'ERROR' || statusUpper === 'DECLINED') {
      statusClass = 'status-error';
      displayText = 'DOWN';
    }
    // Processing states
    
    return (
      <span className={`status-badge ${statusClass}`}>
        {displayText}
      </span>
    );
  };

  const filteredBanks = banks;

  if (loading) {
    return (
      <div className="container">
        <div className="loading">
          <div className="spinner"></div>
        </div>
      </div>
    );
  }
console.log(filteredBanks, "filteredBanks")
  return (
    <div className="container">
      <div className="page-header">
        <h1 className="page-title">Bank Management</h1>
        <p className="page-subtitle">
          Manage registered banks and payment service providers
        </p>
      </div>

      {error && (
        <div className="alert alert-error">
          {error}
        </div>
      )}

      {success && (
        <div className="alert alert-success">
          {success}
        </div>
      )}

      {/* Controls */}
      <div className="card">
        <div className="flex flex-between">
          <div className="flex gap-20">
            <div style={{ position: 'relative' }}>
              <Search size={16} style={{ 
                position: 'absolute', 
                left: '12px', 
                top: '50%', 
                transform: 'translateY(-50%)',
                color: '#666'
              }} />
              <input
                type="text"
                placeholder="Search banks..."
                value={searchTerm}
                onChange={(e) => setSearchTerm(e.target.value)}
                className="form-input"
                style={{ paddingLeft: '40px', width: '300px' }}
              />
            </div>
          </div>
          <button
            onClick={() => {
              resetForm();
              setShowModal(true);
            }}
            className="btn"
          >
            <Plus size={16} style={{ marginRight: '8px' }} />
            Add Bank
          </button>
        </div>
      </div>

      {/* Banks Table */}
      <div className="card">
        <h2 style={{ marginBottom: '20px', color: '#333' }}>
          <Building2 size={20} style={{ marginRight: '8px' }} />
          Registered Banks ({filteredBanks.length})
        </h2>

        {filteredBanks.length === 0 ? (
          <div className="text-center" style={{ padding: '40px', color: '#666' }}>
            <Building2 size={48} style={{ marginBottom: '16px', opacity: 0.5 }} />
            <p>No banks found</p>
            <p style={{ fontSize: '14px', marginTop: '8px' }}>
              {searchTerm ? 'Try adjusting your search terms' : 'Add your first bank to get started'}
            </p>
          </div>
        ) : (
          <div className="table-container">
            <table className="table">
              <thead>
                <tr>
                  <th>OrgId</th>
                  <th>BankName</th>
                  <th>lastHeartBeat</th>
                  <th>Status</th>
                  {/* <th>Org ID</th>
                  <th>URL</th>
                  <th>Actions</th> */}
                </tr>
              </thead>
              <tbody>
                {filteredBanks.map((bank) => (
                  <tr key={bank.orgId}>
                    <td>
                      <code style={{ fontSize: '12px' }}>{bank.orgId}</code>
                    </td>
                    <td>
                      <div>
                        <div style={{ fontWeight: '600' }}>{bank.name}</div>
                        <div style={{ fontSize: '12px', color: '#666' }}>
                          "SBI"
                        </div>
                      </div>
                    </td>
                    <td>
                      <code style={{ fontSize: '12px' }}>{bank.lastHeartbeatAt}</code>
                    </td>
                    <td>
                      <code style={{ fontSize: '12px' }}>{getStatusBadge(bank.status)}</code>
                    </td>
                    {/* <td>
                      <code style={{ fontSize: '12px' }}>{bank.iin}</code>
                    </td>
                    <td>
                      <code style={{ fontSize: '12px' }}>{bank.orgId}</code>
                    </td>
                    <td>
                      <div style={{ fontSize: '12px', color: '#666', maxWidth: '150px', overflow: 'hidden', textOverflow: 'ellipsis' }}>
                        {bank.bankUrl}
                      </div>
                    </td> */}
                    {/* <td>
                      <div className="flex gap-10">
                        <button
                          onClick={() => handleEdit(bank)}
                          className="btn btn-secondary"
                          style={{ padding: '6px 12px', fontSize: '12px' }}
                        >
                          <Edit size={12} style={{ marginRight: '4px' }} />
                          Edit
                        </button>
                        <button
                          onClick={() => handleDelete(bank.id)}
                          className="btn btn-danger"
                          style={{ padding: '6px 12px', fontSize: '12px' }}
                        >
                          <Trash2 size={12} style={{ marginRight: '4px' }} />
                          Delete
                        </button>
                      </div>
                    </td> */}
                  </tr>
                ))}
              </tbody>
            </table>
          </div>
        )}
      </div>

      {/* Add/Edit Bank Modal */}
      {showModal && (
        <div className="modal-overlay" onClick={() => setShowModal(false)}>
          <div className="modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">
                {editingBank ? 'Edit Bank' : 'Add New Bank'}
              </h2>
              <button
                onClick={() => setShowModal(false)}
                className="modal-close"
              >
                <X size={20} />
              </button>
            </div>

            <form onSubmit={handleSubmit}>
              <div className="form-group">
                <label className="form-label">Bank Name *</label>
                <input
                  type="text"
                  name="name"
                  value={formData.name}
                  onChange={handleInputChange}
                  className="form-input"
                  placeholder="e.g., State Bank of India"
                  required
                />
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">Organization ID *</label>
                  <input
                    type="text"
                    name="orgId"
                    value={formData.orgId}
                    onChange={handleInputChange}
                    className="form-input"
                    placeholder="e.g., SBI001"
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">Bank Code *</label>
                  <input
                    type="text"
                    name="bankCode"
                    value={formData.bankCode}
                    onChange={handleInputChange}
                    className="form-input"
                    placeholder="e.g., SBI"
                    required
                  />
                </div>
              </div>

              <div className="form-row">
                <div className="form-group">
                  <label className="form-label">IIN (Issuer Identification Number) *</label>
                  <input
                    type="text"
                    name="iin"
                    value={formData.iin}
                    onChange={handleInputChange}
                    className="form-input"
                    placeholder="e.g., 123456"
                    required
                  />
                </div>
                <div className="form-group">
                  <label className="form-label">IFSC Code *</label>
                  <input
                    type="text"
                    name="ifsc"
                    value={formData.ifsc}
                    onChange={handleInputChange}
                    className="form-input"
                    placeholder="e.g., SBIN0001234"
                    required
                  />
                </div>
              </div>

              <div className="form-group">
                <label className="form-label">VPA Handle *</label>
                <input
                  type="text"
                  name="handle"
                  value={formData.handle}
                  onChange={handleInputChange}
                  className="form-input"
                  placeholder="e.g., @sbi"
                  required
                />
              </div>

              <div className="form-group">
                <label className="form-label">Bank URL *</label>
                <input
                  type="url"
                  name="bankUrl"
                  value={formData.bankUrl}
                  onChange={handleInputChange}
                  className="form-input"
                  placeholder="e.g., http://localhost:8083"
                  required
                />
              </div>

              <div className="flex gap-10" style={{ marginTop: '20px' }}>
                <button
                  type="submit"
                  className="btn"
                >
                  <Save size={16} style={{ marginRight: '8px' }} />
                  {editingBank ? 'Update Bank' : 'Create Bank'}
                </button>
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="btn btn-secondary"
                >
                  Cancel
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default BankManagement;


