import React, { createContext, useContext, useState, useEffect, type ReactNode } from "react";
import type { User } from "@/types/user";
import { getSession } from "@/scripts/getSession";

interface SessionContextType {
  user: User | null;
  setUser: (user: User | null) => void;
  refreshUser: () => Promise<void>;
}

const SessionContext = createContext<SessionContextType>({
  user: null,
  setUser: () => {},
  refreshUser: async () => {},
});

export function SessionProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null);

  useEffect(() => {
    const saved = sessionStorage.getItem("user");
    if (saved) setUser(JSON.parse(saved));
  }, []);

  async function refreshUser() {
    const sessionUser = await getSession();
    setUser(sessionUser);
  }

  return (
    <SessionContext.Provider value={{ user, setUser, refreshUser }}>
      {children}
    </SessionContext.Provider>
  );
}

export function useSession() {
  return useContext(SessionContext);
}