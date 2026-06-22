CREATE TABLE IF NOT EXISTS oauth2_authorized_client (
  client_registration_id varchar(100) NOT NULL,
  principal_name varchar(200) NOT NULL,
  access_token_type varchar(100) NOT NULL,
  access_token_value blob NOT NULL,
  access_token_issued_at timestamp NOT NULL,
  access_token_expires_at timestamp NOT NULL,
  access_token_scopes varchar(1000) DEFAULT NULL,
  refresh_token_value blob DEFAULT NULL,
  refresh_token_issued_at timestamp DEFAULT NULL,
  created_at timestamp DEFAULT CURRENT_TIMESTAMP NOT NULL,
  PRIMARY KEY (client_registration_id, principal_name)
);

CREATE TABLE IF NOT EXISTS experience_extraction_batches (
  id varchar(36) NOT NULL,
  user_id bigint NOT NULL,
  status varchar(20) NOT NULL,
  created_at timestamp NOT NULL,
  processed_at timestamp DEFAULT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS experience_duplicate_groups (
  id varchar(36) NOT NULL,
  batch_id varchar(36) NOT NULL,
  existing_experience_id varchar(36) NOT NULL,
  PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS experience_extraction_drafts (
  id varchar(36) NOT NULL,
  duplicate_group_id varchar(36) NOT NULL,
  title varchar(255) NOT NULL,
  experience_type varchar(50) NOT NULL,
  experience_group varchar(20) NOT NULL,
  status varchar(20) NOT NULL,
  document_content text DEFAULT NULL,
  attributes json NOT NULL,
  keywords json NOT NULL,
  resume_url text NOT NULL,
  similarity double DEFAULT NULL,
  PRIMARY KEY (id)
);
