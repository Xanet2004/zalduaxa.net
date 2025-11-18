export async function signup(form: any, setSignupError: (arg0: string) => void, setSignupSuccess: (arg0: string) => void, setLoading: (arg0: boolean) => void) {
    try {
        setLoading(true);

        const res = await fetch(`${import.meta.env.VITE_API_URL}/auth/signup`, {
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

        setSignupSuccess("Account created successfully! You can now log in.");
    } catch (err) {
        setSignupError(err instanceof Error ? err.message : "Unknown error");
    } finally {
        setLoading(false);
    }
}
