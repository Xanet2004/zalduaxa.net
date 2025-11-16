import { useState } from 'react';
import { Link } from "react-router-dom";
import './header.css'

export default function Header() {
    const [isSigned] = useState(false);
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
                    !isSigned &&
                    (
                        <>
                            <Link to="/login">Log in</Link>
                            <Link to="/signup">Sign up</Link> 
                        </>
                    )
                }
                {
                    isSigned &&
                    (
                        <>
                            <Link to="/signup">Sign out</Link> 
                            <Link to="/signup">Log out</Link> 
                        </>
                    )
                    /* could show a more accessible list */
                }
            </li>
        </nav>
    );
}