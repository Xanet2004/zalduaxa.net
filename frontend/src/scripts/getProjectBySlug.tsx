export async function getProjectBySlug(projectSlug: string) {
  try {
        const res = await fetch(`${import.meta.env.VITE_API_URL}/project/get-project/${encodeURIComponent(projectSlug)}`, {
            method: "GET",
            credentials: "include",
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            throw new Error(data?.message ?? "Failed to load projects");
        }

        console.log("API response for project:", data);

        return data;
    } catch (err) {
        return err;
    }
}