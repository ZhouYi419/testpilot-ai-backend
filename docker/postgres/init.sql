CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS project (
                                       id BIGSERIAL PRIMARY KEY,
                                       name VARCHAR(100) NOT NULL,
    description TEXT,
    owner_name VARCHAR(100),
    status SMALLINT NOT NULL DEFAULT 1,
    deleted BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_project_name ON project(name);
CREATE INDEX IF NOT EXISTS idx_project_deleted ON project(deleted);
CREATE INDEX IF NOT EXISTS idx_project_created_at ON project(created_at);