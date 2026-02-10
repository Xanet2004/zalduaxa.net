import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getProjectsByType } from "@/scripts/getProjectsByType";
import ProjectCard from "@/components/ProjectCard/ProjectCard";
import type { Project } from "@/types/project";

export default function ProjectType() {
  const { typeSlug } = useParams<{ typeSlug: string }>();
  const [projects, setProjects] = useState<Project[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!typeSlug) return;

    (async () => {
      try {
        setLoading(true);
        setError(null);
        const list = await getProjectsByType(typeSlug);
        setProjects(list);
      } catch (e: any) {
        setError(e?.message ?? "Error");
        setProjects(null);
      } finally {
        setLoading(false);
      }
    })();
  }, [typeSlug]);

  if (!typeSlug) return <p>Missing project type</p>;
  if (loading) return <p>Loading...</p>;
  if (error) return <p style={{ color: "red" }}>{error}</p>;

  return (
    <div>
      <h1>Projects of type: {typeSlug}</h1>

      {projects?.length ? (
        <ul>
          {projects.map((p) => (
            <li key={p.id}>
              <ProjectCard projectTypeSlug={typeSlug} project={p} />
            </li>
          ))}
        </ul>
      ) : (
        <p>No projects found</p>
      )}
    </div>
  );
}