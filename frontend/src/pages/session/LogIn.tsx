import { useState } from "react";
import { useNavigate } from "react-router-dom";
import { login } from '@/scripts/login';
import { getSession } from "@/scripts/getSession";

export default function LogIn() {
    const [form, setForm] = useState({
        username: "",
        password: "",
    });

    const navigate = useNavigate();

    const [loginError, setLoginError] = useState("");
    const [loginSuccess, setLoginSuccess] = useState("");
    const [loading, setLoading] = useState(false);

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setLoginError("");
        setLoginSuccess("");
        setLoading(true);
        try {
            await login(form, setLoginError, setLoginSuccess, setLoading);
            await getSession();
            navigate("/");
        } catch (err) {
            console.error(err);
        } finally {
            setLoading(false);
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

            <p>{JSON.stringify(sessionStorage.getItem('user'), null, 2)}</p>

            {loginError && <p style={{ color: "red" }}>{loginError}</p>}
            {loginSuccess && <p style={{ color: "green" }}>{loginSuccess}</p>}
        </div>
    );
}