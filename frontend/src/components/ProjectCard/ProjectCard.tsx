import type { ProjectType } from "@/types/projectType";
import type { Project } from "@/types/project";
import { Link } from "react-router-dom";

type ProjectCardProps = {
  projectType: ProjectType;
  project: Project;
};

export default function ProjectCard({ projectType, project }: ProjectCardProps) {
    return (
    <Link to={`/projects/${projectType.slug}/${project.slug}`}>
      <h2>{project.name}</h2>

      {project.description && <p>Description: {project.description}</p>}

      <img
        src={`${import.meta.env.VITE_API_URL}/storage/projects/${projectType.slug}/${project.slug}/icon.png`}
        alt={project.name}
        style={{ width: "64px", height: "64px" }}
      />
    </Link>
    );
}