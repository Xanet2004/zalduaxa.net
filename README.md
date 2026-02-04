# zalduaxa.net

A full-stack portfolio platform that brings together **backend**, **frontend**, and a **PostgreSQL database** in a single repository.  
This project is built to showcase work in a structured way (projects, types, media/storage, visibility) while keeping a clean separation between **public content**, **authenticated content**, and **admin management**.

This repository currently includes a working end-to-end flow between the frontend, backend, and database.

## Features

- Public visitors can browse all **public** projects
- Authenticated users (guest) can browse **public + private** projects
- Admin users can create and manage projects

## Documentation

- Git structure and syntax: [/documentation/git_structure.md](/documentation/git_structure.md)
- Project structure: [/documentation/project_structure.md](/documentation/project_structure.md)
- Database diagram (dbdiagram.io): https://dbdiagram.io/d/zalduaxanet-69190b9f6735e11170fd8a62
- Database details: [/documentation/ddbb.md](/documentation/ddbb.md)
- Docker setup (dev + deploy): [/documentation/docker.md](/documentation/docker.md)
- Todo list: [/documentation/todo.md](/documentation/todo.md)

## Repository layout

The project is organized around three main parts:

- `backend/`  
  API service (Spring Boot) and business logic, including database access and storage integration.

- `frontend/`  
  Portfolio UI that consumes the backend API and renders projects by visibility.

- `database/` (or Docker-managed Postgres)  
  PostgreSQL schema used by the backend. The schema and relations are documented in the links above.

## Access model

The portfolio uses a simple visibility model:

- Public: accessible without authentication
- Private: accessible only to authenticated users
- Admin: can create and maintain content

## Storage

Project assets (images, icons, files) are served through a storage path exposed by the backend.  
Configuration is documented in the Docker setup and project structure docs.

## Getting started

All setup and run instructions are documented here:

- Docker setup (dev + deploy): [/documentation/docker.md](/documentation/docker.md)

## Roadmap

Planned improvements and pending work are tracked in:

- [/documentation/todo.md](/documentation/todo.md)