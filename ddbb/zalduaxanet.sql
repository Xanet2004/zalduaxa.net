-- Drop old schema and create new one
DROP SCHEMA IF EXISTS zalduaxanetDB CASCADE;
CREATE SCHEMA zalduaxanetDB;

SET search_path TO zalduaxanetDB;

-- Lookup tables
CREATE TABLE visibilities (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100)
);

CREATE TABLE statuses (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100)
);

CREATE TABLE collaborator_roles (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100)
);

CREATE TABLE resource_types (
  id SERIAL PRIMARY KEY,
  code VARCHAR(50) NOT NULL UNIQUE,
  name VARCHAR(100)
);

-- Core tables
CREATE TABLE roles (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255),
  description VARCHAR(255),
  created_at TIMESTAMP
);

CREATE TABLE users (
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
  role_id INT REFERENCES roles(id),
  is_active BOOLEAN DEFAULT TRUE,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE TABLE sessions (
  id SERIAL PRIMARY KEY,
  user_id INT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
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

CREATE TABLE project_types (
  id SERIAL PRIMARY KEY,
  name VARCHAR(255),
  description VARCHAR(255),
  storage_path VARCHAR(255),
  image_path VARCHAR(255)
);

CREATE TABLE projects (
  id SERIAL PRIMARY KEY,
  storage_id INT REFERENCES storage(id),
  owner_id INT REFERENCES users(id),
  name VARCHAR(255),
  slug VARCHAR(255),
  type_id INT REFERENCES project_types(id),
  description TEXT,
  icon_path VARCHAR(255),
  path VARCHAR(255),
  visibility_id INT REFERENCES visibilities(id),
  status_id INT REFERENCES statuses(id),
  version VARCHAR(255),
  metadata JSON,
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP,
  UNIQUE (storage_id, slug)
);

CREATE TABLE project_collaborators (
  project_id INT REFERENCES projects(id) ON DELETE CASCADE,
  user_id INT REFERENCES users(id) ON DELETE CASCADE,
  role_id INT REFERENCES collaborator_roles(id),
  invited_by INT REFERENCES users(id),
  accepted_at TIMESTAMP,
  PRIMARY KEY (project_id, user_id)
);

CREATE TABLE recipes (
  id SERIAL PRIMARY KEY,
  storage_id INT REFERENCES storage(id),
  created_by INT REFERENCES users(id),
  title VARCHAR(255),
  description TEXT,
  path VARCHAR(255),
  visibility_id INT REFERENCES visibilities(id),
  version VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE TABLE drawings (
  id SERIAL PRIMARY KEY,
  storage_id INT REFERENCES storage(id),
  created_by INT REFERENCES users(id),
  title VARCHAR(255),
  description TEXT,
  path VARCHAR(255),
  visibility_id INT REFERENCES visibilities(id),
  version VARCHAR(255),
  created_at TIMESTAMP,
  updated_at TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE TABLE content_items (
  id SERIAL PRIMARY KEY,
  resource_type_id INT REFERENCES resource_types(id),
  resource_id INT,
  created_by INT REFERENCES users(id),
  created_at TIMESTAMP
);

CREATE TABLE comments (
  id SERIAL PRIMARY KEY,
  content_item_id INT REFERENCES content_items(id) ON DELETE CASCADE,
  user_id INT REFERENCES users(id),
  text TEXT,
  created_at TIMESTAMP,
  deleted_at TIMESTAMP
);

CREATE TABLE likes (
  id SERIAL PRIMARY KEY,
  content_item_id INT REFERENCES content_items(id) ON DELETE CASCADE,
  user_id INT REFERENCES users(id),
  created_at TIMESTAMP,
  UNIQUE (content_item_id, user_id)
);

CREATE TABLE audit_logs (
  id SERIAL PRIMARY KEY,
  entity_type VARCHAR(255),
  entity_id INT,
  action VARCHAR(255),
  performed_by INT REFERENCES users(id),
  changes JSON,
  created_at TIMESTAMP
);

-- Optional: seed common lookup values
INSERT INTO visibilities (code, name) VALUES ('public','Public'), ('private','Private');
INSERT INTO statuses (code, name) VALUES ('draft','Draft'), ('published','Published');
INSERT INTO collaborator_roles (code, name) VALUES ('editor','Editor'), ('viewer','Viewer');
INSERT INTO resource_types (code, name) VALUES ('project','Project'), ('recipe','Recipe'), ('drawing','Drawing');
INSERT INTO roles (name, description, created_at) VALUES
  ('admin', 'Full system access', CURRENT_TIMESTAMP),
  ('member', 'Registered user with access to private content', CURRENT_TIMESTAMP),
  ('guest', 'Public-only access', CURRENT_TIMESTAMP);

  

  -- Crear usuario administrador por defecto
INSERT INTO users (
    username,
    full_name,
    email,
    password_hash,
    role_id,
    created_at,
    updated_at
) VALUES (
    'admin',
    'Administrator',
    'admin@example.com',
    '$31$16$O6cdtH7WQf3CpYOb6EKbij5Wb4jnUrhuHi6x6udQgQQ',
    (SELECT id FROM roles WHERE name = 'admin'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);

  -- Crear usuario administrador por defecto
INSERT INTO users (
    username,
    full_name,
    email,
    password_hash,
    role_id,
    created_at,
    updated_at
) VALUES (
    'guest',
    'Guest',
    'guest@example.com',
    '$31$16$HnsSqZSXmB3vuh1YoB8z1nDtAGRNasNNe7C7wRL7q7s',
    (SELECT id FROM roles WHERE name = 'guest'),
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP
);