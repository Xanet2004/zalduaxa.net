import type { ProjectType } from "@/types/projectType";
import { Link } from "react-router-dom";

export default function ProjectTypeCard(props: ProjectType) {
    return (
        <Link to={`/projects/${props.slug}`}>
            <h2>{props.name}</h2>
            {props.description && <p>Description: {props.description}</p>}
            <img src={`/api/storage/projectTypes/${props.slug}/icon.png`} alt={props.name} style={{width: '64px', height:'64px'}}/>
            {/* {props.languages && <p>Languages: {props.languages}</p>}
            {props.tools && <p>Tools: {props.tools}</p>} */}
        </Link>
    );
}