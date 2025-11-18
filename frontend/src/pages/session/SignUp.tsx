import { useState } from "react";
import { signup } from '@/scripts/signup';
import { frontendValidation } from '@/scripts/frontendValidation';

export default function SignUp() {
    const [form, setForm] = useState({
        username: "",
        fullName: "",
        email: "",
        password: "",
        repeated_password: "",
    });

    const [signupError, setSignupError] = useState("");
    const [signupSuccess, setSignupSuccess] = useState("");
    const [loading, setLoading] = useState(false);

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        setForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    }

    async function handleSubmit(e: React.FormEvent) {
        e.preventDefault();
        setSignupError("");
        setSignupSuccess("");
        
        let msg = frontendValidation(form);
        if(msg == "success"){
            setLoading(true);
            signup(form, setSignupError, setSignupSuccess, setLoading);
        } else {
            setSignupError(msg);
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

            {signupError && <p style={{ color: "red" }}>{signupError}</p>}
            {signupSuccess && <p style={{ color: "green" }}>{signupSuccess}</p>}
        </div>
    );
}