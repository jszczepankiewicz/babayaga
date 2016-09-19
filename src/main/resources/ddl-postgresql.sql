--	create initial structure
CREATE TABLE IF NOT EXISTS entities (
    added_id SERIAL NOT NULL PRIMARY KEY,
    id BYTEA NOT NULL UNIQUE,
    updated TIMESTAMP WITHOUT TIME ZONE NOT NULL,
    body BYTEA
);

CREATE INDEX IF NOT EXISTS entities_updated_idx ON entities (updated);