import type { RequestProject } from "@/types/requestProject";

export async function addProject(req: RequestProject) {
  const form = new FormData();
  form.append("typeSlug", req.typeSlug);
  form.append("name", req.name ?? "");
  form.append("slug", req.slug ?? "");
  form.append("description", req.description ?? "");
  if (req.image) form.append("image", req.image);

  const res = await fetch(`/api/project/addProject`, {
    method: "POST",
    body: form,
    credentials: "include",
  });

  const data = await res.json().catch(() => ({}));
  if (!res.ok) throw new Error(data?.message ?? "Error");
  return data;
}