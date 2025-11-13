import { Link } from "react-router-dom";

export default function Header() {
    return (
        <div>
            <Link to="/">Home</Link>
            <p>HEADER</p>
        </div>
    );
}