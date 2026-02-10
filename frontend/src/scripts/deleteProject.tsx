import type { RequestProject } from "@/types/requestProject";

export async function deleteProject(req: Pick<RequestProject, "typeSlug" | "slug">) {
  const res = await fetch(`${import.meta.env.VITE_API_URL}/project/deleteProject`, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(req),
    credentials: "include",
  });

  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data?.message ?? "Error");
  return data;
}