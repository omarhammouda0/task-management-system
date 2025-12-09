CREATE TABLE comments (
    id BIGSERIAL PRIMARY KEY,
    content TEXT NOT NULL,
    task_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
    created_by BIGINT NOT NULL,
    updated_by BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT fk_comment_task FOREIGN KEY (task_id)
        REFERENCES tasks(id) ON DELETE CASCADE,

    CONSTRAINT fk_comment_user FOREIGN KEY (user_id)
        REFERENCES users(id) ON DELETE CASCADE,

    CONSTRAINT fk_comment_created_by FOREIGN KEY (created_by)
        REFERENCES users(id) ON DELETE RESTRICT,

    CONSTRAINT fk_comment_updated_by FOREIGN KEY (updated_by)
        REFERENCES users(id) ON DELETE SET NULL,

    CONSTRAINT chk_comment_status CHECK (status IN ('ACTIVE', 'DELETED'))
);

CREATE INDEX idx_comment_task_id ON comments(task_id);
CREATE INDEX idx_comment_user_id ON comments(user_id);
CREATE INDEX idx_comment_status ON comments(status);
CREATE INDEX idx_comment_created_by ON comments(created_by);
CREATE INDEX idx_comment_created_at ON comments(created_at);