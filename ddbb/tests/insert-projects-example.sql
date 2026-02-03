INSERT INTO zalduaxanet.project_type (name, description, slug)
SELECT
  'Minecraft Mods',
  'Minecraft mods created by myself!',
  'minecraft-mods'
WHERE NOT EXISTS (
  SELECT 1 FROM zalduaxanet.project_type WHERE name = 'minecraft-mods'
);

INSERT INTO zalduaxanet.project (
  storage_id,
  owner_id,
  name,
  slug,
  type_id,
  description,
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
  (SELECT id FROM zalduaxanet.project_type WHERE slug = 'minecraft-mods' ORDER BY id DESC LIMIT 1),
  'Example project inserted with seed script.',
  (SELECT id FROM zalduaxanet.visibility WHERE code = 'public' ORDER BY id DESC LIMIT 1),
  (SELECT id FROM zalduaxanet.status WHERE code = 'draft' ORDER BY id DESC LIMIT 1),
  '0.1.0',
  '{"tags":["demo"],"visibility":"public"}'::json,
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP
WHERE NOT EXISTS (
  SELECT 1
  FROM zalduaxanet.project p
  WHERE p.storage_id = (SELECT id FROM zalduaxanet.storage WHERE name = 'local-storage' ORDER BY id DESC LIMIT 1)
    AND p.slug = 'example-project'
);
