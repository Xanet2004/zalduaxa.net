import { useEffect, useState } from 'react';
import { Link } from "react-router-dom";
import './header.css';
import type { User } from '@/types/user';

export default function Header() {
    const [user, setUser] = useState<User | null>(null);
    
    useEffect(() => {
        const saved = sessionStorage.getItem('user');
        if (saved) {
            setUser(JSON.parse(saved));
            console.log(user == null)
        }
    }, [])

    return (
        <nav>
            <div><Link to="/">Home</Link></div>
            <li className="nav-items">
                <Link to="/profile">Profile</Link>
                <Link to="/projects">Projects</Link> {/* projects could open a list of project types */}
                <Link to="/profile">Art</Link>
                <Link to="/profile">Creations</Link>
                <Link to="/profile">Recipes</Link>
                <Link to="/profile">Agenda</Link>
            </li>
            <li>
                {
                    user == null &&
                    (
                        <>
                            <Link to="/login">Log in</Link>
                            <Link to="/signup">Sign up</Link> 
                        </>
                    )
                }
                {
                    user != null &&
                    (
                        <>
                            <Link to="/logout">Log out</Link> 
                            <Link to="/signup">Sign out</Link> 
                        </>
                    )
                    /* could show a more accessible list */
                }
            </li>
        </nav>
    );
}