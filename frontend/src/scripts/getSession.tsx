export async function getSession() {
  try {
    const res = await fetch(`${import.meta.env.VITE_API_URL}/auth/session`, {
      method: "GET",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
    });

    const data = await res.json().catch(() => ({}));

    if (res.ok && data.user) {
      sessionStorage.setItem("user", JSON.stringify(data.user));
      return data.user;
    }

    sessionStorage.removeItem("user");
    return data.user;
  } catch (err) {
    sessionStorage.removeItem("user");
    console.error(err);
    return null;
  }
}
