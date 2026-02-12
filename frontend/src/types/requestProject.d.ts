export interface RequestProject {
  typeSlug: string;
  slug: string;
  name?: string;
  description?: string;
  image?: File | null;
}