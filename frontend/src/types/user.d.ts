export interface User {
    username: string;
    fullName: string;
    email: string;
    profilePicture?: string;
    role?: Role;
}

interface Role {
    id: number;
    name: string;
    description?: string;
}