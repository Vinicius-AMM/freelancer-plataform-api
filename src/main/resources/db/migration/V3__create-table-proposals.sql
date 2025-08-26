CREATE TABLE proposals (
    id BIGSERIAL PRIMARY KEY,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    offered_value NUMERIC(19, 2) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    project_id BIGINT NOT NULL,
    user_id UUID NOT NULL,
    CONSTRAINT fk_proposal_project FOREIGN KEY (project_id) REFERENCES projects(id),
    CONSTRAINT fk_proposal_freelancer FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE INDEX idx_proposal_project_id ON proposals(project_id);
CREATE INDEX idx_proposal_user_id ON proposals(user_id);