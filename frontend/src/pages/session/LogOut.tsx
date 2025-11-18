import { useEffect } from "react";
import { useNavigate } from "react-router-dom";

export default function LogOut() {
  const navigate = useNavigate();

  logout();

  useEffect(() => {
    sessionStorage.removeItem("user");
    navigate("/");
  }, []);

  return null;
}

async function logout(){
    try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/auth/logout`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
    });
  } catch (err) {
    console.error(err);
    return null;
  }
}