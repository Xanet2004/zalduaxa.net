import { useSession } from "@/context/SessionContext";
import { Link } from "react-router-dom";
import './header.css';

export default function Header() {
    const { user } = useSession();

    return (
        <nav>
            <div><Link to="/">Home</Link></div>
            <li className="nav-items">
                <Link to="/user-profile">User Profile</Link>
                <Link to="/projects">Projects</Link> {/* projects could open a list of project types */}
                <Link to="/art">Art</Link>
                <Link to="/creations">Creations</Link>
                <Link to="/recipes">Recipes</Link>
                <Link to="/agenda">Agenda</Link>
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
                            <Link to="/signout">Sign out</Link> 
                        </>
                    )
                    /* could show a more accessible list */
                }
            </li>
        </nav>
    );
}