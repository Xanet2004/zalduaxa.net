CREATE SCHEMA IF NOT EXISTS zalduaxanet;

SET search_path TO zalduaxanet;

-- Lookup tables
CREATE TABLE visibility (
  id SERIAL PRIMARY KEY,
  name VARCHAR(50) NOT NULL UNIQUE,
  description VARCHAR(100)
);

CREATE TABLE status (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100)
);

CREATE TABLE collaborator_role (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100)
);

CREATE TABLE resource_type (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100)
);

-- Core tables
CREATE TABLE role (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255),
  description VARCHAR(255),
  created_at TIMESTAMP
);

CREATE TABLE "user" (
  id SERIAL PRIMARY KEY,
  username VARCHAR(255) UNIQUE,
  full_name VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(255),
  profile_picture VARCHAR(255),
  linkedin VARCHAR(255),
  github VARCHAR(255),
  website VARCHAR(255),
  role_id INT REFERENCES role(id),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE TABLE session (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
  token VARCHAR(255) NOT NULL UNIQUE,
  ip_address VARCHAR(100),
  user_agent VARCHAR(500),
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  expires_at TIMESTAMP,
  last_activity TIMESTAMP
);

CREATE TABLE storage (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255),
  bucket VARCHAR(255),
  base_path VARCHAR(255),
  config JSON,
  description VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP
);

CREATE TABLE project_type (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255),
  description VARCHAR(255),
  slug VARCHAR(255),
  visibility_id INT REFERENCES visibility(id),
  UNIQUE (slug)
);

CREATE TABLE project (
  id SERIAL PRIMARY KEY,
  storage_id INT REFERENCES storage(id),
  owner_id INT REFERENCES "user"(id),
  name VARCHAR(255),
  slug VARCHAR(255),
  type_id INT REFERENCES project_type(id),
  description TEXT,
  visibility_id INT REFERENCES visibility(id),
  status_id INT REFERENCES status(id),
  version VARCHAR(255),
  metadata JSON,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP,
  UNIQUE (storage_id, slug)
);

CREATE TABLE project_collaborator (
  project_id INT REFERENCES project(id) ON DELETE CASCADE,
  user_id INT REFERENCES "user"(id) ON DELETE CASCADE,
  role_id INT REFERENCES collaborator_role(id),
  invited_by INT REFERENCES "user"(id),
  accepted_at TIMESTAMP,
  PRIMARY KEY (project_id, user_id)
);

CREATE TABLE recipe (
  id SERIAL PRIMARY KEY,
  storage_id INT REFERENCES storage(id),
  created_by INT REFERENCES "user"(id),
  title VARCHAR(255),
  description TEXT,
  path VARCHAR(255),
  visibility_id INT REFERENCES visibility(id),
  version VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE TABLE drawing (
  id SERIAL PRIMARY KEY,
  storage_id INT REFERENCES storage(id),
  created_by INT REFERENCES "user"(id),
  title VARCHAR(255),
  description TEXT,
  path VARCHAR(255),
  visibility_id INT REFERENCES visibility(id),
  version VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE TABLE content_item (
  id SERIAL PRIMARY KEY,
  resource_type_id INT REFERENCES resource_type(id),
  resource_id INT,
  created_by INT REFERENCES "user"(id),
  created_at TIMESTAMP
);

CREATE TABLE comment (
  id SERIAL PRIMARY KEY,
  content_item_id INT REFERENCES content_item(id) ON DELETE CASCADE,
  user_id INT REFERENCES "user"(id),
  text TEXT,
  created_at TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE TABLE "like" (
  id SERIAL PRIMARY KEY,
  content_item_id INT REFERENCES content_item(id) ON DELETE CASCADE,
  user_id INT REFERENCES "user"(id),
  created_at TIMESTAMP,
  UNIQUE (content_item_id, user_id)
);

CREATE TABLE audit_log (
  id SERIAL PRIMARY KEY,
  entity_type VARCHAR(255),
  entity_id INT,
  action VARCHAR(255),
  performed_by INT REFERENCES "user"(id),
  changes JSON,
  created_at TIMESTAMP
);

-- Structural lookup data
INSERT INTO visibility (name, description) VALUES
  ('public', 'Public'),
  ('private', 'Private');

INSERT INTO status (code, name) VALUES
  ('draft', 'Draft'),
  ('published', 'Published');

INSERT INTO collaborator_role (code, name) VALUES
  ('editor', 'Editor'),
  ('viewer', 'Viewer');

INSERT INTO resource_type (code, name) VALUES
  ('project', 'Project'),
  ('recipe', 'Recipe'),
  ('drawing', 'Drawing');

INSERT INTO role (name, description, created_at) VALUES
  ('admin', 'Full system access', CURRENT_TIMESTAMP),
  ('member', 'Registered user with access to private content', CURRENT_TIMESTAMP),
  ('guest', 'Public-only access', CURRENT_TIMESTAMP);

INSERT INTO storage (name, bucket, base_path, config, description, created_at, updated_at) VALUES
  (
    'local-storage',
    'local-bucket',
    '/storage',
    '{"provider":"local"}'::json,
    'Default local storage',
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
  );