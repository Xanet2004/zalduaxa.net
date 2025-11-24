import type { ProjectType } from "@/types/projectType";
import { Link } from "react-router-dom";

export default function ProjectTypeCard(props: ProjectType) {
    // console.log(`${import.meta.env.VITE_API_URL}/storage/${props.imagePath}`)
    return (
        <Link to={`/projects/${props.storagePath}`}>
            <h2>{props.name}</h2>
            {props.description && <p>Description: {props.description}</p>}
            <img src={`${import.meta.env.VITE_API_URL}/storage/${props.imagePath}`} alt={props.name} style={{width: '64px', height:'64px'}}/>
            {/* {props.languages && <p>Languages: {props.languages}</p>}
            {props.tools && <p>Tools: {props.tools}</p>} */}
        </Link>
    );
}