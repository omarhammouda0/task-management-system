CREATE TABLE tasks
(
    id           BIGSERIAL PRIMARY KEY,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    status       VARCHAR(20)  NOT NULL DEFAULT 'TODO',
    priority     VARCHAR(20)  NOT NULL DEFAULT 'MEDIUM',
    project_id   BIGINT       NOT NULL,
    assigned_to  BIGINT,
    due_date     TIMESTAMP,
    completed_at TIMESTAMP,
    created_by   BIGINT       NOT NULL,
    updated_by   BIGINT,
    created_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,


    CONSTRAINT fk_task_project FOREIGN KEY (project_id)
        REFERENCES projects (id) ON DELETE CASCADE,

    CONSTRAINT fk_task_assigned_to FOREIGN KEY (assigned_to)
        REFERENCES users (id) ON DELETE SET NULL,

    CONSTRAINT fk_task_created_by FOREIGN KEY (created_by)
        REFERENCES users (id) ON DELETE RESTRICT,

    CONSTRAINT fk_task_updated_by FOREIGN KEY (updated_by)
        REFERENCES users (id) ON DELETE SET NULL,


    CONSTRAINT uk_task_title_project UNIQUE (project_id, title),


    CONSTRAINT chk_task_status CHECK (status IN ('TODO', 'IN_PROGRESS', 'IN_REVIEW', 'DONE', 'BLOCKED', 'DELETED')),
    CONSTRAINT chk_task_priority CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'URGENT'))
);


CREATE INDEX idx_task_project_id ON tasks (project_id);
CREATE INDEX idx_task_assigned_to ON tasks (assigned_to);
CREATE INDEX idx_task_status ON tasks (status);
CREATE INDEX idx_task_priority ON tasks (priority);
CREATE INDEX idx_task_due_date ON tasks (due_date);
CREATE INDEX idx_task_created_by ON tasks (created_by);


