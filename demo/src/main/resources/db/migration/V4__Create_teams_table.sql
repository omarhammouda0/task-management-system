CREATE TABLE teams (
                       id BIGSERIAL PRIMARY KEY,
                       name VARCHAR(100) NOT NULL,
                       description VARCHAR(500),
                       owner_id BIGINT NOT NULL,
                       status VARCHAR(20) NOT NULL DEFAULT 'ACTIVE',
                       created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                       created_by BIGINT,
                       updated_by BIGINT,


                       CONSTRAINT fk_teams_owner
                           FOREIGN KEY (owner_id)
                               REFERENCES users(id)
                               ON DELETE CASCADE,


                       CONSTRAINT uk_teams_name
                           UNIQUE (name)
);


CREATE INDEX idx_teams_owner_id ON teams(owner_id);
CREATE INDEX idx_teams_status ON teams(status);
CREATE INDEX idx_teams_name ON teams(name);
