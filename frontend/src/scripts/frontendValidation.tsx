export function frontendValidation(form: {
    username: string;
    fullName: string;
    email: string;
    password: string;
    repeated_password: string;
}): string {
    if (!form.username || !form.fullName || !form.email) {
        return "All fields are required.";
    }
    if (form.password.length < 6) {
        return "Password must be at least 6 characters.";
    }
    if (form.password !== form.repeated_password) {
        return "Passwords do not match.";
    }
    return "success";
}