SELECT id,
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
       updated_at,
       deleted_at
FROM zalduaxanet.projects
LIMIT 1000;