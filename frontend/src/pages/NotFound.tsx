import { Link } from "react-router-dom";

export default function NotFound() {
    return (
        <div>
            <h1>Oh que penaa</h1>
            <p>Error 404</p>
            <Link to="/">Home</Link>
        </div>
    );
}