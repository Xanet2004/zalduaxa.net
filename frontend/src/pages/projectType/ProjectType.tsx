import { useEffect, useState } from "react";
import { useParams } from "react-router-dom";
import { getProjectsByType } from "@/scripts/getProjectsByType";
import ProjectCard from "@/components/ProjectCard/ProjectCard";
import type { Project } from "@/types/project";
import { useSession } from "@/context/SessionContext";
import { addProject } from "@/scripts/addProject";
import { deleteProject } from "@/scripts/deleteProject";
import type { RequestProject } from "@/types/requestProject";

export default function ProjectType() {
  const { typeSlug } = useParams<{ typeSlug: string }>();
  const { user } = useSession();
  const isAdmin = user?.role?.name === "admin";

  const [projects, setProjects] = useState<Project[] | null>(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const [isAddingProject, setIsAddingProject] = useState<boolean>();
  const [isDeletingProject, setIsDeletingProject] = useState<boolean>();

  const [addProjectForm, setAddProjectForm] = useState<RequestProject>({
    typeSlug: typeSlug ?? "",
    name: "",
    slug: "",
    description: "",
    image: null,
  });

  const [deleteProjectForm, setDeleteProjectForm] = useState({
    name: ""
  });

  function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
    const { name, files, value } = e.target;

    if (isAddingProject) {
      if (name === "image" && files) {
        setAddProjectForm((prev) => ({ ...prev, image: files[0] }));
      } else {
        setAddProjectForm((prev) => ({ ...prev, [name]: value }));
      }
    } else if (isDeletingProject) {
      setDeleteProjectForm((prev) => ({ ...prev, [name]: value }));
    }
  }

  useEffect(() => {
    if (!typeSlug) return;

    setAddProjectForm((prev) => ({ ...prev, typeSlug }));
    setDeleteProjectForm((prev) => ({ ...prev, typeSlug }));

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

  async function refreshProjects() {
    if (!typeSlug) return;
    const list = await getProjectsByType(typeSlug);
    setProjects(list);
  }

  async function handleAddProject(e: React.FormEvent) {
    e.preventDefault();
    if (!typeSlug) return;

    try {
      setLoading(true);
      setError(null);
      await addProject({ ...addProjectForm, typeSlug });
      await refreshProjects();
    } catch (e: any) {
      setError(e?.message ?? "Error");
    } finally {
      setLoading(false);
    }
  }

  async function handleDeleteProject(e: React.FormEvent) {
    e.preventDefault();
    if (!typeSlug) return;

    try {
      setLoading(true);
      setError(null);
      await deleteProject({ name: deleteProjectForm.name, typeSlug });
      await refreshProjects();
    } catch (e: any) {
      setError(e?.message ?? "Error");
    } finally {
      setLoading(false);
    }
  }

  if (!typeSlug) return <p>Missing project type</p>;
  if (loading) return <p>Loading...</p>;

  return (
    <div>
      <h1>Projects of type: {typeSlug}</h1>

      {error && <p style={{ color: "red" }}>{error}</p>}

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

      {isAdmin && (
        <button
          onClick={() => {
            setIsAddingProject(true);
            setIsDeletingProject(false);
          }}
        >
          Add Project
        </button>
      )}

      {isAdmin && (
        <button
          onClick={() => {
            setIsAddingProject(false);
            setIsDeletingProject(true);
          }}
        >
          Delete Project
        </button>
      )}

      {isAddingProject && (
        <form onSubmit={handleAddProject}>
          <p>name</p>
          <input name="name" value={addProjectForm.name ?? ""} onChange={handleChange} required/>

          <p>slug</p>
          <input name="slug" value={addProjectForm.slug ?? ""} onChange={handleChange} />

          <p>description</p>
          <input name="description" value={addProjectForm.description ?? ""} onChange={handleChange} />

          <p>image</p>
          <input name="image" type="file" onChange={handleChange} />

          <button type="submit" disabled={loading}>
            {loading ? "Adding..." : "Add project"}
          </button>
        </form>
      )}

      {isDeletingProject && (
        <form onSubmit={handleDeleteProject}>
          <p>project name</p>
          <input name="name" value={deleteProjectForm.name ?? ""} onChange={handleChange} />

          <button type="submit" disabled={loading}>
            {loading ? "Deleting..." : "Delete project"}
          </button>
        </form>
      )}
    </div>
  );
}