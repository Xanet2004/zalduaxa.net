export interface RequestProjectType{
    id?: integer,
    name: string,
    storage_path: string,
    description: string,
    image: File | null
}