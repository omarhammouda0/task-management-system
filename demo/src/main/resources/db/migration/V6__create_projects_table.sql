CREATE TABLE projects
(
    id          BIGSERIAL PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    team_id     BIGINT       NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    start_date  TIMESTAMP(6),
    end_date    TIMESTAMP(6),
    created_by  BIGINT       NOT NULL,
    created_at  TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),
    updated_at  TIMESTAMP(6) DEFAULT CURRENT_TIMESTAMP(6),


    CONSTRAINT uk_project_name_team UNIQUE (team_id, name),
    CONSTRAINT fk_projects_team FOREIGN KEY (team_id) REFERENCES teams (id),
    CONSTRAINT fk_projects_created_by FOREIGN KEY (created_by) REFERENCES users (id)
);


CREATE INDEX idx_projects_team_id ON projects (team_id);
CREATE INDEX idx_projects_status ON projects (status);
CREATE INDEX idx_projects_created_by ON projects (created_by);
CREATE INDEX idx_projects_start_date ON projects (start_date);
CREATE INDEX idx_projects_end_date ON projects (end_date);