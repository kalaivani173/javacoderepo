import React from 'react';
import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Navbar from './components/Navbar';
import Dashboard from './pages/Dashboard';
import PaymentForm from './pages/PaymentForm';
import BankManagement from './pages/BankManagement';
import TransactionLogs from './pages/TransactionLogs';
import './App.css';

function App() {
  return (
    <Router>
      <div className="App">
        <Navbar />
        <main className="main-content">
          <Routes>
            <Route path="/" element={<Dashboard />} />
            <Route path="/payment" element={<PaymentForm />} />
            <Route path="/banks" element={<BankManagement />} />
            <Route path="/transactions" element={<TransactionLogs />} />
          </Routes>
        </main>
      </div>
    </Router>
  );
}

export default App;


