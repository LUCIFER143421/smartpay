CREATE TABLE audit_logs (
    id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id        UUID         REFERENCES users(id),
    action         VARCHAR(100) NOT NULL,
    entity_type    VARCHAR(50)  NOT NULL,
    entity_id      UUID         NOT NULL,
    previous_value TEXT,
    new_value      TEXT,
    ip_address     VARCHAR(45),
    created_at     TIMESTAMP    NOT NULL DEFAULT now()
);