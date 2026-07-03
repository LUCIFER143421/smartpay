CREATE TABLE payments (
    id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    sender_wallet_id   UUID          NOT NULL REFERENCES wallets(id),
    receiver_wallet_id UUID          NOT NULL REFERENCES wallets(id),
    amount             NUMERIC(19,4) NOT NULL,
    currency           VARCHAR(3)    NOT NULL,
    status             VARCHAR(30)   NOT NULL DEFAULT 'INITIATED',
    idempotency_key    VARCHAR(255)  NOT NULL UNIQUE,
    created_at         TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at         TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT chk_payment_status CHECK (status IN (
        'INITIATED', 'PROCESSING', 'SUCCESS',
        'FAILED', 'FRAUD_BLOCKED', 'INSUFFICIENT_BALANCE'
    ))
);