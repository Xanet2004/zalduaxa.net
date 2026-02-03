export async function getProjectsByType(typeSlug: string) {
  try {
        const res = await fetch(`${import.meta.env.VITE_API_URL}/project/${encodeURIComponent(typeSlug)}/projects`, {
            method: "GET",
            credentials: "include",
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            throw new Error(data?.message ?? "Failed to load projects");
        }

        return data.projects;
    } catch (err) {
        return err;
    }
}