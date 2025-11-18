import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from '@/scripts/login';
import { useSession } from "@/context/SessionContext";

export default function LogIn() {
    const navigate = useNavigate();
    const [form, setForm] = useState({
        username: "",
        password: "",
    });
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const { refreshUser } = useSession(); 

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        try {
            setLoading(true);
            const loginError = await login(form);
            if (loginError) {
                setError(loginError as string);
            }
            await refreshUser();
            setLoading(false);
        } catch (err) {
            console.error(err);
        }
    }
    return (
        <div>
            <h1>Login</h1>

            <form onSubmit={handleSubmit}>
                <label>Username</label>
                <input name="username" value={form.username} onChange={handleChange} />

                <label>Password</label>
                <input type="password" name="password" value={form.password} onChange={handleChange} />

                <button type="submit" disabled={loading}>
                    {loading ? "Creating..." : "Create account"}
                </button>
            </form>

            {error && <p style={{ color: "red" }}>{error}</p>}
        </div>
    );
}