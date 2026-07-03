CREATE TABLE wallets (
    id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id    UUID NOT NULL,
    balance    NUMERIC(19,4) NOT NULL DEFAULT 0,
    currency   VARCHAR(3)    NOT NULL,
    version    INT           NOT NULL DEFAULT 0,
    created_at TIMESTAMP     NOT NULL DEFAULT now(),
    updated_at TIMESTAMP     NOT NULL DEFAULT now(),

    CONSTRAINT fk_wallet_user FOREIGN KEY (user_id) REFERENCES users(id),
    CONSTRAINT chk_balance_non_negative CHECK (balance >= 0)
);