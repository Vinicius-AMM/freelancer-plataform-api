ALTER TABLE projects
ADD COLUMN accepted_freelancer_id UUID,
ADD CONSTRAINT fk_project_accepted_freelancer FOREIGN KEY (accepted_freelancer_id) REFERENCES users(id);

CREATE INDEX idx_project_accepted_freelancer_id ON projects(accepted_freelancer_id);