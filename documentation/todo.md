# TODO List (API Features)
[Back to menu](/README.md)

## Users
- [o] Create user (POST /users) — admin or normal registration
- [ ] Read/list users (GET /users)
- [ ] Update user (PUT /users/:id)
- [ ] Delete/deactivate user (DELETE /users/:id)
- [o] Login / authentication (token or session)
- [ ] Login error output
- [o] Role & permission system
- [ ] List roles (GET /roles) — for assigning roles when creating users

## Storage
- [ ] List storage (GET /storage) — usually only one, but useful for selecting base path

## Projects
- [ ] Create project (POST /projects) — admin or user
- [ ] Read/list projects (GET /projects or /projects/:id)
- [ ] Update project (PUT /projects/:id)
- [ ] Delete project (DELETE /projects/:id)
- [ ] Optional: assign collaborators (POST /project_collaborators)

## Project Types
- [ ] Create project type (POST /project_types)
- [ ] List project types (GET /project_types)

## Recipes / Drawings
- [ ] CRUD: create (POST), read (GET), update (PUT), delete (DELETE)
- [ ] File upload (linked to `storage.base_path`)

## Content Interactions
### Comments
- [ ] Add comment (POST /comments)
- [ ] List comments (GET /comments?content_item_id=X)
- [ ] Delete comment (DELETE /comments/:id)

### Likes
- [ ] Like (POST /likes)
- [ ] Remove like (DELETE /likes/:id)
- [ ] Count likes (GET /likes?content_item_id=X)

## Audit Logs (optional)
- [ ] Auto-create logs in backend on CRUD actions
- [ ] View logs (GET /audit_logs?entity_type=project&entity_id=X)