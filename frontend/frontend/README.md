# UPI Simulator Frontend

A modern React frontend for the UPI (Unified Payments Interface) simulation system. This application provides a comprehensive interface to manage banks, initiate payments, and monitor transaction logs.

## Features

- **Dashboard**: Overview of system status, statistics, and recent transactions
- **Payment Form**: Initiate UPI payments with validation and sample VPAs
- **Bank Management**: CRUD operations for registered banks and PSPs
- **Transaction Logs**: Monitor and analyze transaction logs with detailed views
- **Responsive Design**: Modern UI that works on desktop and mobile devices

## Technology Stack

- **React 18**: Modern React with hooks and functional components
- **React Router**: Client-side routing
- **Axios**: HTTP client for API communication
- **Lucide React**: Modern icon library
- **CSS3**: Custom styling with modern design patterns

## Getting Started

### Prerequisites

- Node.js (v16 or higher)
- npm or yarn
- Backend services running (UPISim, PayerPSP, PayeePSP, etc.)

### Installation

1. Navigate to the frontend directory:
   ```bash
   cd UPIVerse/frontend
   ```

2. Install dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

4. Open [http://localhost:3000](http://localhost:3000) to view it in the browser.

### Backend Services

Make sure the following backend services are running:

- **UPISim**: http://localhost:8081 (Central UPI switch)
- **PayerPSP**: http://localhost:8080 (Payer's PSP)
- **PayeePSP**: http://localhost:8082 (Payee's PSP)
- **RemitterBank**: http://localhost:8083 (Payer's bank)
- **BeneficiaryBank**: http://localhost:8084 (Payee's bank)

## Project Structure

```
src/
├── components/          # Reusable components
│   └── Navbar.js       # Navigation component
├── pages/              # Page components
│   ├── Dashboard.js    # Main dashboard
│   ├── PaymentForm.js  # Payment initiation form
│   ├── BankManagement.js # Bank CRUD operations
│   └── TransactionLogs.js # Transaction monitoring
├── services/           # API services
│   └── api.js         # API client configuration
├── App.js             # Main app component
├── App.css            # App-specific styles
├── index.js           # App entry point
└── index.css          # Global styles
```

## API Integration

The frontend integrates with the following backend APIs:

### UPISim APIs (Port 8081)
- `GET /psp-banks` - Get all registered banks
- `POST /psp-banks` - Create new bank
- `PUT /psp-banks/{id}` - Update bank
- `DELETE /psp-banks/{id}` - Delete bank
- `GET /dashboard/all` - Get all transaction IDs
- `GET /dashboard/txn/{txnId}` - Get transaction logs
- `GET /dashboard/logs/{txnId}` - Get formatted logs

### PayerPSP APIs (Port 8080)
- `POST /upi/Pay` - Initiate payment

## Usage

### Making a Payment

1. Navigate to the "Make Payment" page
2. Enter payer VPA (e.g., `user@okaxis`)
3. Enter payee VPA (e.g., `merchant@paytm`)
4. Enter amount in rupees
5. Click "Initiate Payment"

### Managing Banks

1. Go to "Bank Management" page
2. Click "Add Bank" to register a new bank
3. Fill in bank details (name, handle, IFSC, etc.)
4. Use edit/delete buttons to manage existing banks

### Monitoring Transactions

1. Visit "Transaction Logs" page
2. Select a transaction ID from the dropdown
3. View detailed logs and status information
4. Download logs for offline analysis

## Sample Data

The application includes sample VPAs for testing:
- `user@okaxis` - Sample payer VPA
- `merchant@paytm` - Sample payee VPA
- `customer@phonepe` - Alternative VPA
- `shop@googlepay` - Merchant VPA

## Styling

The application uses a modern design system with:
- Gradient backgrounds and buttons
- Card-based layouts
- Responsive grid system
- Status badges and icons
- Smooth animations and transitions

## Development

### Available Scripts

- `npm start` - Runs the app in development mode
- `npm build` - Builds the app for production
- `npm test` - Launches the test runner
- `npm eject` - Ejects from Create React App

### Customization

- Modify `src/services/api.js` to change API endpoints
- Update `src/index.css` for global styling changes
- Customize components in `src/components/` and `src/pages/`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Test thoroughly
5. Submit a pull request

## License

This project is part of the UPI Simulator system and follows the same licensing terms.


