export async function login(form: any, setLoginError: (arg0: string) => void, setLoginSuccess: (arg0: string) => void, setLoading: (arg0: boolean) => void) {
    try {
        setLoading(true);

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

        setLoginSuccess("User logged in");
    } catch (err) {
        setLoginError(err instanceof Error ? err.message : "Unknown error");
    } finally {
        setLoading(false);
    }
}
