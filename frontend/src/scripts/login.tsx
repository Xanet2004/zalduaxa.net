export async function login(form: { username: string; password: string; }) {
    try {
        const res = await fetch(`${import.meta.env.VITE_API_URL}/auth/login`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
            body: JSON.stringify(form),
        });

        let data;
        try { data = await res.json(); }
        catch { data = {}; }

        if (!res.ok) {
            throw new Error(data.message || "Error creating user");
        }

    } catch (err) {
        return err;
    }
}
