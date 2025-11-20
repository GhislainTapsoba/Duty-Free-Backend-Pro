-- Migration V11: Create passenger_counts table for tracking daily passenger traffic

CREATE TABLE IF NOT EXISTS passenger_counts (
    id BIGSERIAL PRIMARY KEY,
    count_date DATE NOT NULL,
    total_passengers INTEGER NOT NULL DEFAULT 0,
    arriving_passengers INTEGER DEFAULT 0,
    departing_passengers INTEGER DEFAULT 0,
    flight_number VARCHAR(20),
    airline VARCHAR(100),
    destination VARCHAR(100),
    count_type VARCHAR(20) DEFAULT 'MANUAL',
    notes VARCHAR(500),
    created_by VARCHAR(50),
    updated_by VARCHAR(50),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Indexes
CREATE INDEX IF NOT EXISTS idx_passenger_count_date ON passenger_counts(count_date);
CREATE INDEX IF NOT EXISTS idx_passenger_count_flight ON passenger_counts(flight_number);
CREATE INDEX IF NOT EXISTS idx_passenger_count_airline ON passenger_counts(airline);

-- Trigger for updated_at
CREATE OR REPLACE FUNCTION update_passenger_counts_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_passenger_counts_updated_at
    BEFORE UPDATE ON passenger_counts
    FOR EACH ROW
    EXECUTE FUNCTION update_passenger_counts_updated_at();

-- Comments
COMMENT ON TABLE passenger_counts IS 'Comptage quotidien des passagers pour calculer le taux de capture';
COMMENT ON COLUMN passenger_counts.count_date IS 'Date du comptage';
COMMENT ON COLUMN passenger_counts.total_passengers IS 'Nombre total de passagers';
COMMENT ON COLUMN passenger_counts.arriving_passengers IS 'Passagers en arrivée internationale';
COMMENT ON COLUMN passenger_counts.departing_passengers IS 'Passagers en départ international';
COMMENT ON COLUMN passenger_counts.count_type IS 'Type de comptage: MANUAL, AUTOMATIC, ESTIMATED';
