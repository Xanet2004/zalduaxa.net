export async function addProjectType(form: any) {
    try {
        const formData = new FormData();
        formData.append("name", form.name ?? "");
        formData.append("slug", form.slug ?? "");
        formData.append("description", form.description ?? "");

        if (form.image instanceof File) {
            formData.append("image", form.image);
        }

        const res = await fetch(`${import.meta.env.VITE_API_URL}/project/addProjectType`, {
            method: "POST",
            credentials: "include",
            body: formData
        });

        let data;
        try { data = await res.json(); }
        catch { data = {}; }

        console.log(data)

        if (!res.ok) {
            throw new Error(data.message || "Error adding project type");
        }

        return data;

    } catch (err) {
        return err;
    }
}