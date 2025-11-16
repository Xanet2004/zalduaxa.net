import { useState } from "react";

export default function SignUp() {
    const [form, setForm] = useState({
        username: "",
        fullName: "",
        email: "",
        password: "",
        repeated_password: "",
    });

    const [error, setError] = useState("");
    const [success, setSuccess] = useState("");
    const [loading, setLoading] = useState(false);

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setError("");
        setSuccess("");
        
        // Frontend validation
        if (!form.username || !form.fullName || !form.email) {
            return setError("All fields are required.");
        }

        if (form.password.length < 6) {
            return setError("Password must be at least 6 characters.");
        }

        if (form.password !== form.repeated_password) {
            return setError("Passwords do not match.");
        }

        setLoading(true);

        try {
            const res = await fetch(
                `${import.meta.env.VITE_API_URL}/auth/register`,
                {
                    method: "POST",
                    headers: { "Content-Type": "application/json" },
                    body: JSON.stringify(form),
                }
            );

            let data;
            try { data = await res.json(); }
            catch { data = {}; }

            if (!res.ok) {
                throw new Error(data.message || "Error creating user");
            }

            setSuccess("Account created successfully! You can now log in.");
        } catch (err) {
            setError(err instanceof Error ? err.message : "Unknown error");
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <h1>SignUp</h1>

            <form onSubmit={handleSubmit}>
                <label>Username</label>
                <input name="username" value={form.username} onChange={handleChange} />

                <label>Full Name</label>
                <input name="fullName" value={form.fullName} onChange={handleChange} />

                <label>Email</label>
                <input type="email" name="email" value={form.email} onChange={handleChange} />

                <label>Password</label>
                <input type="password" name="password" value={form.password} onChange={handleChange} />

                <label>Repeat Password</label>
                <input type="password" name="repeated_password" value={form.repeated_password} onChange={handleChange} />

                <button type="submit" disabled={loading}>
                    {loading ? "Creating..." : "Create account"}
                </button>
            </form>

            {error && <p style={{ color: "red" }}>{error}</p>}
            {success && <p style={{ color: "green" }}>{success}</p>}
        </div>
    );
}
