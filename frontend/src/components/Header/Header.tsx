import { Link } from "react-router-dom";
import './header.css'

export default function Header() {
    return (
        <nav>
            <div><Link to="/">Home</Link></div>
            <li className="nav-items">
                <Link to="/profile">Profile</Link>
                <Link to="/profile">Projects</Link> {/* projects could open a list of project types */}
                <Link to="/profile">Art</Link>
                <Link to="/profile">Creations</Link>
                <Link to="/profile">Recipes</Link>
                <Link to="/profile">Agenda</Link>
            </li>
            <li>
                <Link to="/">Home</Link>
            </li>
        </nav>
    );
}