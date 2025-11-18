import { useSession } from "@/context/SessionContext";

export default function UserProfile() {
    const { user } = useSession();
    return (
        <div>
            <h1>Profile</h1>
            {user ? (
                <div>
                    {Object.entries(user).map(([key, value]) => {
                        if (value === null || value === undefined || value === "") return null;
                        if (typeof value === "boolean") value = value ? "Yes" : "No";
                        if (key.toLowerCase().includes("picture") || key.toLowerCase().includes("avatar")) {
                            return (
                                <div key={key}>
                                    <p><strong>{key}:</strong></p>
                                    <img
                                        src={value as string}
                                        alt={`${user.username} ${key}`}
                                        style={{ width: 100, height: 100, borderRadius: "50%" }}
                                    />
                                </div>
                            );
                        }

                        if (key === "role" && typeof value === "object") {
                            return (
                                <div key={key}>
                                <p><strong>Role:</strong></p>
                                <ul>
                                    <li><strong>ID:</strong> {(value as any).id}</li>
                                    <li><strong>Name:</strong> {(value as any).name}</li>
                                    <li><strong>Description:</strong> {(value as any).description}</li>
                                </ul>
                                </div>
                            );
                        }


                        return (
                            <p key={key}>
                                <strong>{key}:</strong> {value as string}
                            </p>
                        );
                    })}
                </div>
            ) : (
                <p>No user logged in</p>
            )}
        </div>
    );
}