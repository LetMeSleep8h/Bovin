CREATE EXTENSION IF NOT EXISTS vector;

CREATE TABLE IF NOT EXISTS rag_document (
    id BIGSERIAL PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    source_name VARCHAR(255) NOT NULL,
    corpus_type VARCHAR(32) NOT NULL,
    document_type VARCHAR(32) NOT NULL,
    raw_markdown TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS rag_chunk (
    id BIGSERIAL PRIMARY KEY,
    document_id BIGINT NOT NULL REFERENCES rag_document(id) ON DELETE CASCADE,
    chunk_index INT NOT NULL,
    heading VARCHAR(255),
    chunk_text TEXT NOT NULL,
    fts_text TEXT NOT NULL,
    fts TSVECTOR GENERATED ALWAYS AS (to_tsvector('simple', COALESCE(fts_text, ''))) STORED,
    embedding VECTOR(1536) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS mock_record (
    id BIGSERIAL PRIMARY KEY,
    chunk_id BIGINT NOT NULL,
    document_id BIGINT,
    source_name VARCHAR(255) NOT NULL,
    question_text TEXT NOT NULL,
    answer_text TEXT NOT NULL,
    feedback_json TEXT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_rag_chunk_document_id ON rag_chunk (document_id);
CREATE INDEX IF NOT EXISTS idx_rag_chunk_fts ON rag_chunk USING GIN (fts);
CREATE INDEX IF NOT EXISTS idx_rag_chunk_embedding ON rag_chunk USING hnsw (embedding vector_cosine_ops);
CREATE INDEX IF NOT EXISTS idx_mock_record_created_at ON mock_record (created_at DESC);
