import { Link } from "react-router-dom";

interface ProjectTypeCardProps {
  readonly url: string;
  readonly title: string;
  readonly languages: readonly string[];
  readonly tools: readonly string[];
}

export default function ProjectTypeCard(props: ProjectTypeCardProps) {
    return (
        <Link to={`/projects/${props.url}`}>
            <h2>{props.title}</h2>
            {props.languages && <p>Languages: {props.languages}</p>}
            {props.tools && <p>Tools: {props.tools}</p>}
        </Link>
    );
}