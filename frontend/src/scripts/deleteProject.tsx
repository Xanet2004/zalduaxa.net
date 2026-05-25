// import type { RequestProject } from "@/types/requestProject";

export async function deleteProject(form: { typeSlug: string; name: string }) {
  const res = await fetch(`/api/project/deleteProject`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(form),
    credentials: "include",
  });

  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data?.message ?? "Error");
  return data;
}