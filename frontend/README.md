# frontend — zalduaxa.net

React + TypeScript + Vite UI for the Zalduaxa.net portfolio platform.

## Main directories

```
src/
├── components/   # Reusable UI components (Header, Footer, ProjectCard)
├── context/      # React context (SessionContext manages auth state)
├── pages/        # Route-level pages (Home, Project, Projects, session, etc.)
├── scripts/      # Backend API call functions (fetch with /api/ prefix)
├── styles/       # Global CSS and design tokens
├── types/        # TypeScript type definitions
├── App.tsx
└── main.tsx
```

## Scripts

```bash
npm install        # Install dependencies
npm run dev        # Start Vite dev server (port 5173)
npm run build      # Build for production
npm run lint       # Run ESLint
```

## API calls

All API requests use the `/api/` prefix (e.g. `/api/project-types`, `/api/auth/login`).

- **In Docker:** nginx proxies `/api/...` to the backend, stripping the `/api` prefix.
- **In local development:** The Vite dev server does not define a proxy. To test the full stack locally, run it in Docker.

## Pages

Available routes:

| Path | Description |
|------|-------------|
| `/` | Home / landing |
| `/projects` | Browse project types |
| `/projects/:typeSlug` | Projects of a specific type |
| `/project/:slug` | Project detail |
| `/signup` | User registration |
| `/login` | User login |
| `/logout` | Logout |
| `/profile` | User profile |
