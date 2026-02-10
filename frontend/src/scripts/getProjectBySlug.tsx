export async function getProjectBySlug(projectSlug: string) {
  try {
        const res = await fetch(`${import.meta.env.VITE_API_URL}/project/getProject/${encodeURIComponent(projectSlug)}`, {
            method: "GET",
            credentials: "include",
        });

        const data = await res.json().catch(() => ({}));

        if (!res.ok) {
            throw new Error(data?.message ?? "Failed to load projects");
        }

        return data;
    } catch (err) {
        return err;
    }
}