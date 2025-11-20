-- Migration V13: Create payment terminals and terminal transactions tables

-- Payment Terminals table
CREATE TABLE IF NOT EXISTS payment_terminals (
    id BIGSERIAL PRIMARY KEY,
    terminal_id VARCHAR(50) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    manufacturer VARCHAR(50),
    model VARCHAR(50),
    serial_number VARCHAR(100),
    terminal_type VARCHAR(20) NOT NULL,
    connection_type VARCHAR(20) NOT NULL,
    ip_address VARCHAR(45),
    port INTEGER,
    com_port VARCHAR(20),
    merchant_id VARCHAR(50),
    location VARCHAR(100),
    cash_register_id BIGINT,
    supports_contactless BOOLEAN DEFAULT FALSE,
    supports_chip BOOLEAN DEFAULT TRUE,
    supports_magnetic_stripe BOOLEAN DEFAULT TRUE,
    supports_mobile_payment BOOLEAN DEFAULT FALSE,
    status VARCHAR(20) NOT NULL DEFAULT 'OFFLINE',
    last_heartbeat TIMESTAMP,
    firmware_version VARCHAR(50),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    notes VARCHAR(500),
    CONSTRAINT fk_terminal_cash_register FOREIGN KEY (cash_register_id)
        REFERENCES cash_registers(id) ON DELETE SET NULL
);

-- Terminal Transactions table
CREATE TABLE IF NOT EXISTS terminal_transactions (
    id BIGSERIAL PRIMARY KEY,
    transaction_id VARCHAR(100) NOT NULL UNIQUE,
    terminal_id BIGINT NOT NULL,
    payment_id BIGINT,
    transaction_type VARCHAR(20) NOT NULL,
    amount DECIMAL(19, 2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'XOF',
    status VARCHAR(20) NOT NULL,
    card_type VARCHAR(20),
    card_number_masked VARCHAR(20),
    card_holder_name VARCHAR(100),
    authorization_code VARCHAR(50),
    reference_number VARCHAR(100),
    acquirer_id VARCHAR(50),
    merchant_id VARCHAR(50),
    terminal_receipt TEXT,
    customer_receipt TEXT,
    error_code VARCHAR(20),
    error_message VARCHAR(500),
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    response_time_ms BIGINT,
    signature_required BOOLEAN DEFAULT FALSE,
    signature_verified BOOLEAN,
    pin_verified BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(50),
    notes VARCHAR(500),
    CONSTRAINT fk_terminal_transaction_terminal FOREIGN KEY (terminal_id)
        REFERENCES payment_terminals(id) ON DELETE CASCADE,
    CONSTRAINT fk_terminal_transaction_payment FOREIGN KEY (payment_id)
        REFERENCES payments(id) ON DELETE SET NULL
);

-- Indexes for payment_terminals
CREATE INDEX IF NOT EXISTS idx_terminal_id ON payment_terminals(terminal_id);
CREATE INDEX IF NOT EXISTS idx_terminal_status ON payment_terminals(status);
CREATE INDEX IF NOT EXISTS idx_terminal_active ON payment_terminals(active);
CREATE INDEX IF NOT EXISTS idx_terminal_deleted ON payment_terminals(deleted);
CREATE INDEX IF NOT EXISTS idx_terminal_cash_register ON payment_terminals(cash_register_id);
CREATE INDEX IF NOT EXISTS idx_terminal_type ON payment_terminals(terminal_type);

-- Indexes for terminal_transactions
CREATE INDEX IF NOT EXISTS idx_terminal_transaction_id ON terminal_transactions(transaction_id);
CREATE INDEX IF NOT EXISTS idx_terminal_transaction_terminal ON terminal_transactions(terminal_id);
CREATE INDEX IF NOT EXISTS idx_terminal_transaction_payment ON terminal_transactions(payment_id);
CREATE INDEX IF NOT EXISTS idx_terminal_transaction_status ON terminal_transactions(status);
CREATE INDEX IF NOT EXISTS idx_terminal_transaction_type ON terminal_transactions(transaction_type);
CREATE INDEX IF NOT EXISTS idx_terminal_transaction_created ON terminal_transactions(created_at);
CREATE INDEX IF NOT EXISTS idx_terminal_transaction_completed ON terminal_transactions(completed_at);

-- Trigger for payment_terminals updated_at
CREATE OR REPLACE FUNCTION update_payment_terminals_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_payment_terminals_updated_at
    BEFORE UPDATE ON payment_terminals
    FOR EACH ROW
    EXECUTE FUNCTION update_payment_terminals_updated_at();

-- Comments
COMMENT ON TABLE payment_terminals IS 'Terminaux de paiement électronique (TPE)';
COMMENT ON COLUMN payment_terminals.terminal_id IS 'Identifiant unique du terminal';
COMMENT ON COLUMN payment_terminals.terminal_type IS 'Type: FIXED, PORTABLE, MOBILE, PINPAD, VIRTUAL';
COMMENT ON COLUMN payment_terminals.connection_type IS 'Type de connexion: ETHERNET, WIFI, BLUETOOTH, SERIAL, GPRS, CLOUD_API';
COMMENT ON COLUMN payment_terminals.status IS 'Statut: ONLINE, OFFLINE, BUSY, ERROR, MAINTENANCE';

COMMENT ON TABLE terminal_transactions IS 'Transactions effectuées via les terminaux de paiement';
COMMENT ON COLUMN terminal_transactions.transaction_type IS 'Type: SALE, REFUND, CANCELLATION, PREAUTH, COMPLETION, VOID, REVERSAL';
COMMENT ON COLUMN terminal_transactions.status IS 'Statut: PENDING, PROCESSING, APPROVED, DECLINED, TIMEOUT, ERROR, CANCELLED, REVERSED';
COMMENT ON COLUMN terminal_transactions.card_number_masked IS 'Numéro de carte masqué (ex: ****1234)';
COMMENT ON COLUMN terminal_transactions.response_time_ms IS 'Temps de réponse en millisecondes';
