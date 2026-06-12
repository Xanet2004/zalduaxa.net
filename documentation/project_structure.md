# Project Structure

[Back to menu](/README.md)

## Root structure

```
project-root/
├── backend/              # Spring Boot API
├── frontend/             # React + TypeScript + Vite UI
├── db/                   # Historical SQL, reference queries, test data
├── documentation/        # Project docs
├── storage/              # Local file storage (images, assets)
├── docker-compose.yml    # Full stack (postgres + backend + frontend)
├── docker-compose.dev.yml # PostgreSQL-only dev helper
└── README.md
```

---

## Backend (Spring Boot + Java 21)

```
backend/src/main/java/net/zalduaxa/backend/
├── BackendApplication.java
├── config/               # App-wide Spring configuration
│   ├── PasswordEncoderConfig.java
│   └── SecurityConfig.java
├── common/               # Shared across domains
│   ├── dto/
│   ├── exception/
│   └── util/
├── auth/                 # Authentication, sessions, users, roles
│   ├── controller/
│   ├── dto/request/
│   ├── dto/response/
│   ├── model/
│   ├── security/
│   └── service/
├── project/              # Project types, projects, visibility
│   ├── controller/
│   ├── dto/request/
│   ├── dto/response/
│   ├── model/
│   └── service/
└── storage/              # Static file serving and storage ops
    ├── config/
    └── service/
```

### Domain overview

| Package | Responsibility |
|---------|---------------|
| `auth` | Signup, login, JWT issuance, DB-backed sessions, user/role management |
| `project` | Project types, projects, visibility filters, project CRUD endpoints |
| `storage` | Static resource mapping via WebConfig, file/asset operations |
| `common` | Shared DTOs (`MessageResponse`), custom exceptions, utility classes (`SlugUtils`) |
| `config` | Spring Security filter chain, CORS, password encoder bean, method security |

---

## Frontend (React + TypeScript + Vite)

```
frontend/src/
├── components/       # Reusable UI components
│   ├── Footer/
│   ├── Header/
│   ├── ProjectCard/
│   └── ProjectTypeCard/
├── context/          # React context providers
│   └── SessionContext.tsx
├── pages/            # Route-level page components
│   ├── error/
│   ├── session/      # LogIn, LogOut, SignUp
│   ├── project/      # Project detail
│   ├── projects/     # Legacy/duplicate — to be cleaned
│   ├── projectType/  # Project type detail
│   ├── projectTypes/ # Active project listing page
│   ├── Home.tsx
│   └── UserProfile.tsx
├── scripts/          # API call functions (uses `/api/...`)
├── styles/           # Global CSS and design tokens
├── types/            # TypeScript type definitions
├── App.tsx
└── main.tsx
```

### Key frontend notes

- `scripts/` contains all backend API calls using `fetch()` with the `/api/` prefix.
- `context/SessionContext.tsx` manages user session state across the app.
- `pages/projects/Projects.tsx` is a duplicate/legacy page and should be cleaned in a future task.

---

## Storage (local filesystem)

```
storage/
├── projects/       # Project assets organised by project type slug
└── projectTypes/   # Project type icons
```

Storage paths are slug-based. The local `./storage` directory is mounted into the backend container at `/app/storage`. Files are served through the `/storage/**` endpoint.
