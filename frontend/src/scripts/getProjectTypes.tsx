export async function getProjectTypes() {
    try {
        console.log(import.meta.env.VITE_API_URL)
        console.log(`${import.meta.env.VITE_API_URL}/project/projectTypes`)

        const res = await fetch(`${import.meta.env.VITE_API_URL}/project/projectTypes`, {
            method: "GET",
            headers: { "Content-Type": "application/json" },
        });

        let data;
        try { data = await res.json(); }
        catch { data = {}; }

        console.log(data)

        if (!res.ok) {
            throw new Error(data.message || "Error loading projects");
        }

        return data;

    } catch (err) {
        return err;
    }
}