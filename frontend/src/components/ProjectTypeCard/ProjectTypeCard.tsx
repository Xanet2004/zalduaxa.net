import type { ProjectType } from "@/types/projectType";
import { Link } from "react-router-dom";

export default function ProjectTypeCard(props: ProjectType) {
    return (
        <Link to={`/projects/${props.name}`}>
            <h2>{props.name}</h2>
            {props.description && <p>Languages: {props.description}</p>}
            {props.imagePath && <p>Tools: {props.imagePath}</p>}
            {/* {props.languages && <p>Languages: {props.languages}</p>}
            {props.tools && <p>Tools: {props.tools}</p>} */}
        </Link>
    );
}