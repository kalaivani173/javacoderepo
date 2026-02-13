# UPIVerse
UPI AI Hackathon

Set up code Run all services PayerPSP,PAyeePSP,RemitterBank,UPISim,BeneficiaryBank
Open as Maven project

To initiate a transaction use below from Postman:

http://localhost:8080/upi/Pay?payerVpa=toshu@pyr&payeeVpa=kalai@pye&amount=900.00

Use handle pyr for Payer and pye for Payee.Other path params will change.This will hit PayerPSP and hit request to UPI

You will be able to find application logs in upisim->logs path

For UPISim,DB is required with 2 tables.Use below queries to create in DB
psp_bank table:

CREATE DATABASE upisim;
CREATE TABLE psp_bank (
    id BIGINT PRIMARY KEY,
    bank_code VARCHAR(255),
    bank_url VARCHAR(255),
    handle VARCHAR(255),
    ifsc VARCHAR(255),
    iin VARCHAR(255),
    name VARCHAR(255),
    org_id VARCHAR(255)
);
INSERT INTO psp_bank (id, bank_code, bank_url, handle, ifsc, iin, name, org_id) VALUES
(1, 'PYE', 'http://localhost:8082', 'pye', 'ABCD', '7777', 'PayeeBank', '157777'),
(2, 'BEN', 'http://localhost:8084', 'idbibank', 'IBKL0000694', '8888', 'BeneficiaryBank', '158888'),
(3, 'REM', 'http://localhost:8083', 'pyr', 'AABF0000460', '9999', 'Remitter Bank', '159999');


transaction_logs table:

CREATE TABLE transaction_logs (
    id BIGINT PRIMARY KEY,
    api VARCHAR(255),
    created_at TIMESTAMP WITHOUT TIME ZONE,   -- proper timestamp
    direction VARCHAR(50),                    -- INBOUND / OUTBOUND
    payload TEXT,                             -- XML/JSON payload
    txn_id VARCHAR(255),
    uri VARCHAR(500)
);





