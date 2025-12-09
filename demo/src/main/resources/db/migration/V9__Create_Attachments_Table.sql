CREATE TABLE attachments (
                             id BIGSERIAL PRIMARY KEY,
                             original_filename VARCHAR(255) NOT NULL,
                             stored_filename VARCHAR(255) NOT NULL UNIQUE,
                             bucket_name VARCHAR(100) NOT NULL,
                             object_key VARCHAR(500) NOT NULL UNIQUE,
                             file_size BIGINT NOT NULL,
                             content_type VARCHAR(100) NOT NULL,
                             task_id BIGINT NOT NULL,
                             user_id BIGINT NOT NULL,
                             status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                             created_by BIGINT NOT NULL,
                             updated_by BIGINT,
                             created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                             updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_attachment_task FOREIGN KEY (task_id)
                                 REFERENCES tasks(id) ON DELETE CASCADE,

                             CONSTRAINT fk_attachment_user FOREIGN KEY (user_id)
                                 REFERENCES users(id) ON DELETE CASCADE,

                             CONSTRAINT fk_attachment_created_by FOREIGN KEY (created_by)
                                 REFERENCES users(id) ON DELETE RESTRICT,

                             CONSTRAINT fk_attachment_updated_by FOREIGN KEY (updated_by)
                                 REFERENCES users(id) ON DELETE SET NULL,

                             CONSTRAINT chk_attachment_status CHECK (status IN ('ACTIVE', 'DELETED')),
                             CONSTRAINT chk_attachment_file_size CHECK (file_size > 0)
);

CREATE INDEX idx_attachment_task_id ON attachments(task_id);
CREATE INDEX idx_attachment_user_id ON attachments(user_id);
CREATE INDEX idx_attachment_status ON attachments(status);
CREATE INDEX idx_attachment_created_by ON attachments(created_by);
CREATE INDEX idx_attachment_created_at ON attachments(created_at);
CREATE INDEX idx_attachment_object_key ON attachments(object_key);