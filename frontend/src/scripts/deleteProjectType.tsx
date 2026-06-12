import { slugify } from "@/scripts/slugify";

type DeleteProjectTypeForm = {
    name?: string;
    slug?: string;
    projectTypeSlug?: string;
};

export async function deleteProjectType(form: DeleteProjectTypeForm) {
    try {
        const rawSlug = form.projectTypeSlug ?? form.slug ?? form.name ?? "";
        const slug = slugify(rawSlug);

        if (!slug) {
            throw new Error("Project type slug is required");
        }

        const res = await fetch(`/api/project-types/${encodeURIComponent(slug)}`, {
            method: "DELETE",
            credentials: "include",
        });

        let data: { message?: string };
        try {
            data = await res.json();
        } catch {
            data = {};
        }

        console.log(data);

        if (!res.ok) {
            throw new Error(data.message || "Error deleting project type");
        }

        return data;

    } catch (err) {
        return err;
    }
}