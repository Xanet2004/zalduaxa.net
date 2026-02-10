export interface Project {
  id?: number;
  storageId?: number;
  ownerId?: number;
  typeId?: number;
  visibilityId?: number;
  statusId?: number;
  name: string;
  slug: string;
  description?: string;
  version?: string;
  metadata?: Record<string, unknown>;
  createdAt?: string;
  updatedAt?: string;
  deletedAt?: string | null;
}