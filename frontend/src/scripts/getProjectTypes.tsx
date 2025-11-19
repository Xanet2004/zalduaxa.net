export async function getProjectTypes() {
    try {
        const res = await fetch(`${import.meta.env.VITE_API_URL}/project/projectTypes`, {
            method: "GET",
            headers: { "Content-Type": "application/json" },
        });

        let data;
        try { data = await res.json(); }
        catch { data = {}; }

        if (!res.ok) {
            throw new Error(data.message || "Error loading project types");
        }

        if (!Array.isArray(data)) return [];
        return data;

    } catch (err) {
        return err;
    }
}