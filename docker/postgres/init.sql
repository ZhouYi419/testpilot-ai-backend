CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS project (
                                       id BIGSERIAL PRIMARY KEY,
                                       project_name VARCHAR(128) NOT NULL,
    description TEXT,
    owner_id BIGINT DEFAULT 0,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS project_module (
                                              id BIGSERIAL PRIMARY KEY,
                                              project_id BIGINT NOT NULL,
                                              module_code VARCHAR(128) NOT NULL,
    module_name VARCHAR(128) NOT NULL,
    parent_module_id BIGINT DEFAULT NULL,
    description TEXT,
    status SMALLINT DEFAULT 1,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_project_module UNIQUE (project_id, module_code)
    );

CREATE TABLE IF NOT EXISTS prd_document (
                                            id BIGSERIAL PRIMARY KEY,
                                            project_id BIGINT NOT NULL,
                                            version_no VARCHAR(64) NOT NULL,
    doc_name VARCHAR(255) NOT NULL,
    doc_type VARCHAR(32) DEFAULT 'PRD',
    module_code VARCHAR(128),
    file_url VARCHAR(512),
    bucket_name VARCHAR(128),
    object_name VARCHAR(512),
    content_hash VARCHAR(128),
    parse_status VARCHAR(32) DEFAULT 'PENDING',
    raw_text TEXT,
    error_message TEXT,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS document_chunk (
                                              id BIGSERIAL PRIMARY KEY,
                                              project_id BIGINT NOT NULL,
                                              document_id BIGINT NOT NULL,
                                              version_no VARCHAR(64) NOT NULL,
    module_code VARCHAR(128),
    module_name VARCHAR(128),
    parent_chunk_id BIGINT DEFAULT NULL,
    chunk_type VARCHAR(32) NOT NULL,
    section_title VARCHAR(255),
    chunk_index INT NOT NULL,
    change_type VARCHAR(32) DEFAULT 'UNKNOWN',
    content TEXT NOT NULL,
    token_count INT DEFAULT 0,
    vector_id VARCHAR(128),
    metadata JSONB,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE TABLE IF NOT EXISTS knowledge_build_task (
                                                    id BIGSERIAL PRIMARY KEY,
                                                    task_id VARCHAR(64) NOT NULL UNIQUE,
    project_id BIGINT NOT NULL,
    document_id BIGINT NOT NULL,
    status VARCHAR(32) DEFAULT 'PENDING',
    total_chunks INT DEFAULT 0,
    success_chunks INT DEFAULT 0,
    fail_chunks INT DEFAULT 0,
    error_message TEXT,
    start_time TIMESTAMP,
    end_time TIMESTAMP,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP
    );

CREATE INDEX IF NOT EXISTS idx_project_module_project_id ON project_module(project_id);
CREATE INDEX IF NOT EXISTS idx_prd_project_version ON prd_document(project_id, version_no);
CREATE INDEX IF NOT EXISTS idx_prd_module_code ON prd_document(module_code);
CREATE INDEX IF NOT EXISTS idx_chunk_document_id ON document_chunk(document_id);
CREATE INDEX IF NOT EXISTS idx_chunk_project_version ON document_chunk(project_id, version_no);
CREATE INDEX IF NOT EXISTS idx_chunk_module_code ON document_chunk(module_code);