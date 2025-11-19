export async function addProjectType(form: any) {
    try {
        const res = await fetch(`${import.meta.env.VITE_API_URL}/project/addProjectType`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(form)
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