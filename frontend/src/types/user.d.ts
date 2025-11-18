export interface User {
    username: string;
    fullName: string;
    email: string;
    isActive: boolean;
    createdAt: string;
    deletedAt?: string;
    github?: string;
    linkedin?: string;
    phone?: string;
    profilePicture?: string;
    role?: string;
}
