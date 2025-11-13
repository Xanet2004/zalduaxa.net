# zalduaxa.net

This project contains the **backend**, **frontend**, and **database (DDBB)** for my portfolio.


## Git Branch Structure

- **main**: Production-ready state. Only stable and tested code should be merged here.  
- **develop**: Main development branch. All new features and changes should be merged here first.  
- **hotfix**: For urgent bug fixes that need to be applied immediately to production.


## Git Commit Syntax

To keep commits clean and understandable, use the following syntax:

```
<type>(<scope>): <short description>
```


### Commit Types

- **feat**: A new feature
- **fix**: A bug fix
- **docs**: Documentation changes
- **style**: Code style changes (formatting, missing semicolons, etc.)
- **refactor**: Code changes that neither fix a bug nor add a feature
- **test**: Adding or updating tests
- **chore**: Changes to the build process or auxiliary tools

### Examples

```bash
git commit -m "feat(frontend): add responsive navbar"
git commit -m "fix(backend): correct user authentication error"
git commit -m "docs: update README with project structure"
git commit -m "style: fix indentation in main.css"
```

## Project structure
### Local development
```
project-root/
├─ .git/
├─ backend/                  # Java Spring Boot
│   ├─ src/
│   ├─ resources/
│   ├─ application.yml
│   └─ ...
├─ frontend/                  # React + TypeScript + Vite
│   ├─ src/
│   │   ├─ portfolio/
│   │   │   ├─ projects/
│   │   │   ├─ drawings/
│   │   │   ├─ recipes/
│   │   │   └─ agenda/
│   │   ├─ assets/
│   │   │   ├─ fonts/
│   │   │   ├─ icons/
│   │   │   ├─ images/
│   │   │   └─ videos/
│   │   ├─ components/
│   │   ├─ pages/
│   │   ├─ hooks/
│   │   ├─ services/
│   │   └─ utils/
│   ├─ vite.config.ts
│   └─ package.json
├─ storage/                   # Projects, drawings, etc. (local)
│   ├─ projects/
│   │   ├─ minecraft-mods/
│   │   │   └─ <project-id>/
│   │   ├─ games/
│   │   │   └─ <project-id>/
│   │   └─ code/
│   │       └─ <project-id>/
│   └─ drawings/
│       └─ <drawing-id>/
├─ database/
│   └─ mysql/
├─ .gitignore
├─ backend.zip
├─ package-lock.json
└─ README.md
```

### Production

```
project-root/
├─ backend/                  # Java Spring Boot
│   ├─ src/
│   ├─ resources/
│   ├─ application.yml
│   └─ ...
├─ frontend/                  # React + TypeScript + Vite
│   ├─ src/
│   │   ├─ portfolio/
│   │   │   ├─ projects/
│   │   │   ├─ drawings/
│   │   │   ├─ recipes/
│   │   │   └─ agenda/
│   │   ├─ components/
│   │   ├─ pages/
│   │   ├─ hooks/
│   │   ├─ services/
│   │   └─ utils/
│   ├─ vite.config.ts
│   └─ package.json
├─ storage/ (host volume)     # Mounted only in backend container
│   ├─ projects/
│   │   ├─ minecraft-mods/
│   │   │   └─ <project-id>/
│   │   ├─ games/
│   │   │   └─ <project-id>/
│   │   └─ code/
│   │       └─ <project-id>/
│   └─ drawings/
│       └─ <drawing-id>/
├─ database/
│   └─ mysql/                 # Mounted in db container
├─ docker/
│   ├─ Dockerfile.backend
│   ├─ Dockerfile.frontend
│   ├─ docker-compose.yml
│   └─ scripts/
├─ .gitignore
├─ README.md
```