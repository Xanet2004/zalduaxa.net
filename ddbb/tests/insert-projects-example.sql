INSERT INTO zalduaxanet.project_type (name, description, storage_path)
SELECT
  'web',
  'Web projects',
  '/storage/projects/web'
WHERE NOT EXISTS (
  SELECT 1 FROM zalduaxanet.project_type WHERE name = 'web'
);

INSERT INTO zalduaxanet.project (
  storage_id,
  owner_id,
  name,
  slug,
  type_id,
  description,
  icon_path,
  path,
  visibility_id,
  status_id,
  version,
  metadata,
  created_at,
  updated_at
)
SELECT
  (SELECT id FROM zalduaxanet.storage WHERE name = 'local-storage' ORDER BY id DESC LIMIT 1),
  (SELECT id FROM zalduaxanet.user WHERE username = 'admin' ORDER BY id DESC LIMIT 1),
  'Example Project',
  'example-project',
  (SELECT id FROM zalduaxanet.project_type WHERE name = 'web' ORDER BY id DESC LIMIT 1),
  'Example project inserted with seed script.',
  '/storage/projects/web/example-project/icon.png',
  '/storage/projects/web/example-project',
  (SELECT id FROM zalduaxanet.visibilitie WHERE code = 'public' ORDER BY id DESC LIMIT 1),
  (SELECT id FROM zalduaxanet.status WHERE code = 'draft' ORDER BY id DESC LIMIT 1),
  '0.1.0',
  '{"tags":["demo"],"visibility":"public"}'::json,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1
  FROM zalduaxanet.projects p
  WHERE p.storage_id = (SELECT id FROM zalduaxanet.storage WHERE name = 'local-storage' ORDER BY id DESC LIMIT 1)
    AND p.slug = 'example-project'
);
