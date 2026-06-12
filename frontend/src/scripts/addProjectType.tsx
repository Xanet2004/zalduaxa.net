import type { RequestProjectType } from "@/types/requestProjectType";

export async function addProjectType(form: RequestProjectType) {
    try {
        const formData = new FormData();

        formData.append("name", form.name ?? "");
        formData.append("slug", form.slug ?? "");
        formData.append("description", form.description ?? "");

        if (form.image instanceof File) {
            formData.append("image", form.image);
        }

        const res = await fetch(`/api/project-types`, {
            method: "POST",
            credentials: "include",
            body: formData,
        });

        let data: { message?: string };
        try {
            data = await res.json();
        } catch {
            data = {};
        }

        console.log(data);

        if (!res.ok) {
            throw new Error(data.message || "Error adding project type");
        }

        return data;

    } catch (err) {
        return err;
    }
}