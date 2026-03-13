INSERT INTO PSP_BANK
(BANK_CODE, BANK_URL, HANDLE, IFSC, IIN, NAME, ORG_ID, STATUS)
VALUES
    ('HDF', 'http://localhost:9999', 'hdfc', 'HDFC', '238933', 'HDFC', '11111', 'ACTIVE'),

    ('SBI', 'http://localhost:8085', 'sbi', 'SBIN0001234', '123456', 'State Bank of India', 'SBI001', 'ACTIVE'),

    ('PYR', 'http://localhost:8080', 'pyr', 'PYRB', '2333', 'PayerBank', '156666', 'ACTIVE'),

    ('REM', 'http://localhost:8083', 'rem', 'AABF', '9999', 'RemitterBank', '159999', 'ACTIVE'),

    ('PYE', 'http://localhost:8082', 'pye', 'ABCD', '7777', 'PayeeBank', '157777', 'ACTIVE'),

    ('BEN', 'http://localhost:8084', 'idbibank', 'INDB', '8888', 'BeneficiaryBank', '158888', 'ACTIVE');
