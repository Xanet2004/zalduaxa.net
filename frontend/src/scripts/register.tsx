export async function register(form: any, setRegisterError: (arg0: string) => void, setRegisterSuccess: (arg0: string) => void, setLoading: (arg0: boolean) => void) {
    try {
        setLoading(true);

        const res = await fetch(`${import.meta.env.VITE_API_URL}/auth/register`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(form),
        });

        let data;
        try { data = await res.json(); }
        catch { data = {}; }

        if (!res.ok) {
            throw new Error(data.message || "Error creating user");
        }

        setRegisterSuccess("Account created successfully! You can now log in.");
    } catch (err) {
        setRegisterError(err instanceof Error ? err.message : "Unknown error");
    } finally {
        setLoading(false);
    }
}
