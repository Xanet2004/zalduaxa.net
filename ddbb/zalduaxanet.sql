DROP SCHEMA IF EXISTS `zalduaxanetDB`;
CREATE SCHEMA IF NOT EXISTS `zalduaxanetDB` DEFAULT CHARACTER SET utf8;
USE `zalduaxanetDB`;

CREATE TABLE visibilities (
  id INT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL UNIQUE, -- eg: 'public', 'private'
  name VARCHAR(100) NULL
);

CREATE TABLE statuses (
  id INT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL UNIQUE, -- eg: 'draft', 'published'
  name VARCHAR(100) NULL
);

CREATE TABLE collaborator_roles (
  id INT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL UNIQUE, -- eg: 'editor', 'viewer'
  name VARCHAR(100) NULL
);

CREATE TABLE resource_types (
  id INT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(50) NOT NULL UNIQUE, -- eg: 'project','recipe','drawing'
  name VARCHAR(100) NULL
);

-- Core tables
CREATE TABLE roles (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255),
  description VARCHAR(255),
  created_at DATETIME
);

CREATE TABLE users (
  id INT PRIMARY KEY AUTO_INCREMENT,
  username VARCHAR(255) UNIQUE,
  full_name VARCHAR(255),
  email VARCHAR(255) UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  phone VARCHAR(255),
  profile_picture VARCHAR(255),
  linkedin VARCHAR(255),
  github VARCHAR(255),
  website VARCHAR(255),
  role_id INT,
  is_active TINYINT(1) DEFAULT 1,
  created_at DATETIME,
  updated_at DATETIME,
  deleted_at DATETIME,
  CONSTRAINT fk_users_role FOREIGN KEY (role_id) REFERENCES roles(id)
);

CREATE TABLE sessions (
  id INT PRIMARY KEY AUTO_INCREMENT,
  user_id INT NOT NULL,
  token VARCHAR(255) NOT NULL UNIQUE,
  ip_address VARCHAR(100),
  user_agent VARCHAR(500),
  created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
  expires_at DATETIME,
  last_activity DATETIME,
  CONSTRAINT fk_sessions_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
);

CREATE TABLE storage (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255),
  bucket VARCHAR(255),
  base_path VARCHAR(255),
  config JSON,
  description VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME
);

CREATE TABLE project_types (
  id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255),
  description VARCHAR(255),
  image_path VARCHAR(255)
);

CREATE TABLE projects (
  id INT PRIMARY KEY AUTO_INCREMENT,
  storage_id INT,
  owner_id INT,
  name VARCHAR(255),
  slug VARCHAR(255),
  type_id INT,
  description TEXT,
  icon_path VARCHAR(255),
  path VARCHAR(255),
  visibility_id INT,   -- FK to visibilities
  status_id INT,       -- FK to statuses
  version VARCHAR(255),
  metadata JSON,
  created_at DATETIME,
  updated_at DATETIME,
  deleted_at DATETIME,
  CONSTRAINT fk_projects_storage FOREIGN KEY (storage_id) REFERENCES storage(id),
  CONSTRAINT fk_projects_owner FOREIGN KEY (owner_id) REFERENCES users(id),
  CONSTRAINT fk_projects_type FOREIGN KEY (type_id) REFERENCES project_types(id),
  CONSTRAINT fk_projects_visibility FOREIGN KEY (visibility_id) REFERENCES visibilities(id),
  CONSTRAINT fk_projects_status FOREIGN KEY (status_id) REFERENCES statuses(id),
  UNIQUE KEY ux_projects_storage_slug (storage_id, slug)
);

CREATE TABLE project_collaborators (
  project_id INT,
  user_id INT,
  role_id INT,         -- FK to collaborator_roles
  invited_by INT,
  accepted_at DATETIME,
  PRIMARY KEY (project_id, user_id),
  CONSTRAINT fk_pc_project FOREIGN KEY (project_id) REFERENCES projects(id) ON DELETE CASCADE,
  CONSTRAINT fk_pc_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE,
  CONSTRAINT fk_pc_role FOREIGN KEY (role_id) REFERENCES collaborator_roles(id),
  CONSTRAINT fk_pc_invited_by FOREIGN KEY (invited_by) REFERENCES users(id)
);

CREATE TABLE recipes (
  id INT PRIMARY KEY AUTO_INCREMENT,
  storage_id INT,
  created_by INT,
  title VARCHAR(255),
  description TEXT,
  path VARCHAR(255),
  visibility_id INT,
  version VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME,
  deleted_at DATETIME,
  CONSTRAINT fk_recipes_storage FOREIGN KEY (storage_id) REFERENCES storage(id),
  CONSTRAINT fk_recipes_created_by FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT fk_recipes_visibility FOREIGN KEY (visibility_id) REFERENCES visibilities(id)
);

CREATE TABLE drawings (
  id INT PRIMARY KEY AUTO_INCREMENT,
  storage_id INT,
  created_by INT,
  title VARCHAR(255),
  description TEXT,
  path VARCHAR(255),
  visibility_id INT,
  version VARCHAR(255),
  created_at DATETIME,
  updated_at DATETIME,
  deleted_at DATETIME,
  CONSTRAINT fk_drawings_storage FOREIGN KEY (storage_id) REFERENCES storage(id),
  CONSTRAINT fk_drawings_created_by FOREIGN KEY (created_by) REFERENCES users(id),
  CONSTRAINT fk_drawings_visibility FOREIGN KEY (visibility_id) REFERENCES visibilities(id)
);

CREATE TABLE content_items (
  id INT PRIMARY KEY AUTO_INCREMENT,
  resource_type_id INT,  -- FK to resource_types
  resource_id INT,
  created_by INT,
  created_at DATETIME,
  CONSTRAINT fk_ci_resource_type FOREIGN KEY (resource_type_id) REFERENCES resource_types(id),
  CONSTRAINT fk_ci_created_by FOREIGN KEY (created_by) REFERENCES users(id)
);

CREATE TABLE comments (
  id INT PRIMARY KEY AUTO_INCREMENT,
  content_item_id INT,
  user_id INT,
  text TEXT,
  created_at DATETIME,
  deleted_at DATETIME,
  CONSTRAINT fk_comments_content_item FOREIGN KEY (content_item_id) REFERENCES content_items(id) ON DELETE CASCADE,
  CONSTRAINT fk_comments_user FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE likes (
  id INT PRIMARY KEY AUTO_INCREMENT,
  content_item_id INT,
  user_id INT,
  created_at DATETIME,
  CONSTRAINT fk_likes_content_item FOREIGN KEY (content_item_id) REFERENCES content_items(id) ON DELETE CASCADE,
  CONSTRAINT fk_likes_user FOREIGN KEY (user_id) REFERENCES users(id),
  UNIQUE KEY ux_likes_content_user (content_item_id, user_id)
);

CREATE TABLE audit_logs (
  id INT PRIMARY KEY AUTO_INCREMENT,
  entity_type VARCHAR(255),
  entity_id INT,
  action VARCHAR(255),
  performed_by INT,
  changes JSON,
  created_at DATETIME,
  CONSTRAINT fk_audit_performed_by FOREIGN KEY (performed_by) REFERENCES users(id)
);

-- Optional: seed common lookup values
INSERT INTO visibilities (code, name) VALUES ('public','Public'), ('private','Private');
INSERT INTO statuses (code, name) VALUES ('draft','Draft'), ('published','Published');
INSERT INTO collaborator_roles (code, name) VALUES ('editor','Editor'), ('viewer','Viewer');
INSERT INTO resource_types (code, name) VALUES ('project','Project'), ('recipe','Recipe'), ('drawing','Drawing');