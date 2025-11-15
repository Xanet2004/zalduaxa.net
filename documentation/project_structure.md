# Project Structure
`Not updated`
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
│   │   ├─ assets/
│   │   │   ├─ fonts/
│   │   │   ├─ icons/
│   │   │   ├─ images/
│   │   │   └─ videos/
│   │   ├─ components/
│   │   ├─ pages/
│   │   │   ├─ Home/            # Portfolio + presentation
│   │   │   ├─ projects/        # Dinamically add projects / drawings / recipes pages using id or maybe names
│   │   │   ├─ drawings/        # Dinamically generating pages. (Reads the information and builds the page using a template if that page doesnt exist)
│   │   │   ├─ recipes/
│   │   │   └─ agenda/
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
│   ├─ drawings/
│   │   └─ <drawing-id>/
│   └─ recipes/
│       └─ <recipes-id>/    # Dinamically generate recipe pages
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