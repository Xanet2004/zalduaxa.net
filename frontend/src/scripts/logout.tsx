import { useNavigate } from "react-router-dom";
import { useSession } from "@/context/SessionContext";

export async function logout() {
    const navigate = useNavigate();
    const { setUser } = useSession();
    try {
        await fetch(`${import.meta.env.VITE_API_URL}/auth/logout`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            credentials: "include",
        });
    } catch (err) {
        console.error(err);
    } finally {
        sessionStorage.removeItem("user");
        setUser(null);
        navigate("/");
    }
}