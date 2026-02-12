import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getProjectBySlug } from "@/scripts/getProjectBySlug";
import type { Project } from "@/types/project";

export default function Project() {
  const { typeSlug } = useParams<{ typeSlug: string }>();
  const { projectSlug } = useParams<{ projectSlug: string }>();
  const [project, setProject] = useState<Project | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (!typeSlug) return;
    if (!projectSlug) return;

    (async () => {
      try {
        setLoading(true);
        setError(null);
        const project = await getProjectBySlug(projectSlug);
        setProject(project);
      } catch (e: any) {
        setError(e?.message ?? "Error");
        setProject(null);
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

      {project ? (
        <div>
            <p>{project.name}</p>
            <p>{project.description}</p>
            <p>{project.slug}</p>
            <p>{project.version}</p>
            <p>{JSON.stringify(project.metadata)}</p>
        </div>
      ) : (
        <p>No project found</p>
      )}
    </div>
  );
}