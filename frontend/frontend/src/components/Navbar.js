import React, { useState } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { CreditCard, Building2, FileText, BarChart3, Menu, X } from 'lucide-react';

const Navbar = () => {
  const location = useLocation();
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);

  const navItems = [
    { path: '/', label: 'Dashboard', icon: BarChart3 },
    { path: '/payment', label: 'Make Payment', icon: CreditCard },
    { path: '/banks', label: 'Bank Management', icon: Building2 },
    { path: '/transactions', label: 'Transaction Logs', icon: FileText },
  ];

  const handleNavClick = () => {
    setIsMobileMenuOpen(false);
  };

  return (
    <nav className="navbar">
      <div className="navbar-content">
        <Link to="/" className="navbar-brand">
          <CreditCard size={24} style={{ marginRight: '8px', verticalAlign: 'middle' }} />
          <span>UPI Simulator</span>
        </Link>

        {/* Desktop Navigation */}
        <ul className="navbar-nav navbar-nav-desktop">
          {navItems.map(({ path, label, icon: Icon }) => (
            <li key={path}>
              <Link
                to={path}
                className={`nav-link ${location.pathname === path ? 'active' : ''}`}
              >
                <Icon size={18} style={{ marginRight: '8px', verticalAlign: 'middle' }} />
                <span>{label}</span>
              </Link>
            </li>
          ))}
        </ul>

        {/* Mobile Menu Toggle */}
        <button 
          className="mobile-menu-toggle"
          onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
          aria-label="Toggle menu"
        >
          {isMobileMenuOpen ? <X size={24} /> : <Menu size={24} />}
        </button>
      </div>

      {/* Mobile Navigation */}
      {isMobileMenuOpen && (
        <div className="navbar-mobile-menu">
          <ul className="navbar-nav-mobile">
            {navItems.map(({ path, label, icon: Icon }) => (
              <li key={path}>
                <Link
                  to={path}
                  className={`nav-link-mobile ${location.pathname === path ? 'active' : ''}`}
                  onClick={handleNavClick}
                >
                  <Icon size={20} style={{ marginRight: '12px' }} />
                  <span>{label}</span>
                </Link>
              </li>
            ))}
          </ul>
        </div>
      )}
    </nav>
  );
};

export default Navbar;


