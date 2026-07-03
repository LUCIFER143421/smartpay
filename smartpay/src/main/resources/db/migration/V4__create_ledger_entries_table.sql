CREATE TABLE ledger_entries (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    payment_id UUID          NOT NULL REFERENCES payments(id),
    wallet_id  UUID          NOT NULL REFERENCES wallets(id),
    amount     NUMERIC(19,4) NOT NULL,
    entry_type VARCHAR(10)   NOT NULL,
    created_at TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT chk_entry_type CHECK (entry_type IN ('DEBIT', 'CREDIT'))
);