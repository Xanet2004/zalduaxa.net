import { useSession } from "@/context/SessionContext";
import type { Role } from "@/types/user";

function isRole(value: unknown): value is Role {
    return (
        typeof value === "object" &&
        value !== null &&
        "id" in value &&
        "name" in value
    );
}

export default function UserProfile() {
    const { user } = useSession();

    return (
        <div>
            <h1>Profile</h1>

            {user ? (
                <div>
                    {Object.entries(user).map(([key, value]) => {
                        if (value === null || value === undefined || value === "") {
                            return null;
                        }

                        if (
                            typeof value === "string" &&
                            (key.toLowerCase().includes("picture") ||
                                key.toLowerCase().includes("avatar"))
                        ) {
                            return (
                                <div key={key}>
                                    <p>
                                        <strong>{key}:</strong>
                                    </p>
                                    <img
                                        src={value}
                                        alt={`${user.username} ${key}`}
                                        style={{
                                            width: 100,
                                            height: 100,
                                            borderRadius: "50%",
                                        }}
                                    />
                                </div>
                            );
                        }

                        if (key === "role" && isRole(value)) {
                            return (
                                <div key={key}>
                                    <p>
                                        <strong>Role:</strong>
                                    </p>
                                    <ul>
                                        <li>
                                            <strong>ID:</strong> {value.id}
                                        </li>
                                        <li>
                                            <strong>Name:</strong> {value.name}
                                        </li>
                                        {value.description && (
                                            <li>
                                                <strong>Description:</strong>{" "}
                                                {value.description}
                                            </li>
                                        )}
                                    </ul>
                                </div>
                            );
                        }

                        const displayValue =
                            typeof value === "boolean" ? (value ? "Yes" : "No") : String(value);

                        return (
                            <p key={key}>
                                <strong>{key}:</strong> {displayValue}
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