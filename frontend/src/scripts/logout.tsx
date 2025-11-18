export async function logout(setLogoutError: (arg0: string) => void, setLogoutSuccess: (arg0: string) => void, setLoading: (arg0: boolean) => void) {
    try {
        setLoading(true);

        const res = await fetch(`${import.meta.env.VITE_API_URL}/auth/logout`, {
            method: "POST",
            credentials: "include",
            headers: { "Content-Type": "application/json" }
        });

        let data;
        try { data = await res.json(); }
        catch { data = {}; }

        if (!res.ok) {
            throw new Error(data.message || "Error creating user");
        }

        setLogoutSuccess(data);
    } catch (err) {
        setLogoutError(err instanceof Error ? err.message : "Unknown error");
    } finally {
        setLoading(false);
    }
}