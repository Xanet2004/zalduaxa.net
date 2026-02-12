import type { Project } from "@/types/project";
import { Link } from "react-router-dom";

type ProjectCardProps = {
  projectTypeSlug: string;
  project: Project;
};

export default function ProjectCard({ projectTypeSlug, project }: ProjectCardProps) {
    return (
      <Link to={`/projects/${projectTypeSlug}/${project.slug}`}>
        <h2>{project.name}</h2>

        {project.description && <p>Description: {project.description}</p>}

        <img
          src={`${import.meta.env.VITE_API_URL}/storage/projects/${projectTypeSlug}/${project.slug}/icon.png`}
          alt={project.name}
          style={{ width: "64px", height: "64px" }}
        />
      </Link>
    );
}