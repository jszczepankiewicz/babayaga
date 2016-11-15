--	create initial structure
CREATE TABLE IF NOT EXISTS entities (
    added_id BIGSERIAL NOT NULL PRIMARY KEY,
    id UUID NOT NULL UNIQUE,
    updated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    body BYTEA
);

CREATE INDEX IF NOT EXISTS entities_updated_idx ON entities (updated);