CREATE TABLE IF NOT EXISTS apartments (
    id BIGSERIAL PRIMARY KEY,
    block_name VARCHAR(20) NOT NULL,
    apartment_no VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE (block_name, apartment_no)
);

CREATE TABLE IF NOT EXISTS residents (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT REFERENCES apartments(id),
    full_name VARCHAR(120) NOT NULL,
    phone VARCHAR(30),
    rfid_id VARCHAR(80),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS gate_logs (
    id BIGSERIAL PRIMARY KEY,
    event_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    method VARCHAR(40) NOT NULL,
    door_id VARCHAR(40) NOT NULL DEFAULT 'main',
    resident_id BIGINT REFERENCES residents(id),
    note TEXT
);

CREATE TABLE IF NOT EXISTS alarms (
    id BIGSERIAL PRIMARY KEY,
    alarm_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    apartment_id BIGINT REFERENCES apartments(id),
    alarm_type VARCHAR(40) NOT NULL,
    source_label VARCHAR(120),
    is_resolved BOOLEAN NOT NULL DEFAULT FALSE,
    resolved_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS chat_messages (
    id BIGSERIAL PRIMARY KEY,
    apartment_id BIGINT REFERENCES apartments(id),
    sender_type VARCHAR(30) NOT NULL,
    message_text TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    delivery_status VARCHAR(30) NOT NULL DEFAULT 'queued'
);

CREATE TABLE IF NOT EXISTS intercom_devices (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(80) NOT NULL UNIQUE,
    ip_address VARCHAR(80) NOT NULL,
    command_port INTEGER NOT NULL DEFAULT 5432,
    location VARCHAR(120),
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE gate_logs
    ADD COLUMN IF NOT EXISTS device_id BIGINT REFERENCES intercom_devices(id);

INSERT INTO intercom_devices (name, ip_address, command_port, location)
VALUES ('Main Door Panel', '192.168.1.100', 5432, 'Main entrance')
ON CONFLICT (name) DO NOTHING;

CREATE INDEX IF NOT EXISTS idx_gate_logs_event_time ON gate_logs(event_time);
CREATE INDEX IF NOT EXISTS idx_gate_logs_device_id ON gate_logs(device_id);
CREATE INDEX IF NOT EXISTS idx_alarms_alarm_time ON alarms(alarm_time);
CREATE INDEX IF NOT EXISTS idx_chat_messages_apartment ON chat_messages(apartment_id);



CREATE TABLE IF NOT EXISTS visitors (
                                        id BIGSERIAL PRIMARY KEY,
                                        visitor_name VARCHAR(120) NOT NULL,
    visitor_type VARCHAR(40) NOT NULL,
    block_name VARCHAR(20),
    apartment_no VARCHAR(20),
    visit_reason TEXT,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    entry_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    exit_time TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_visitors_entry_time ON visitors(entry_time);
CREATE INDEX IF NOT EXISTS idx_visitors_status ON visitors(status);