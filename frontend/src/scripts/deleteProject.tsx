import { slugify } from "@/scripts/slugify";

export async function deleteProject(form: { typeSlug?: string; name?: string; slug?: string; projectSlug?: string }) {
    const rawSlug = form.projectSlug ?? form.slug ?? form.name ?? "";
    const slug = slugify(rawSlug);

    if (!slug) {
        throw new Error("Project slug is required");
    }

    const res = await fetch(`/api/projects/${encodeURIComponent(slug)}`, {
        method: "DELETE",
        credentials: "include",
    });

    const data = await res.json().catch(() => ({}));

    if (!res.ok) {
        throw new Error(data?.message ?? "Error deleting project");
    }

    return data;
}