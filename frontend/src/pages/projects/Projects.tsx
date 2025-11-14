import { useState } from 'react';
import ProjectTypeCard from '@/components/ProjectTypeCard/ProjectTypeCard';

const projects = [
  {
    title: "Minecraft mods",
    url: "minecraft-mods",
    languages: ["Java"],
    tools: ["VSCode", "Aseprite"]
  },
  {
    title: "Procedural animation",
    url: "procedural-animation",
    languages: ["js"],
    tools: []
  },
  {
    title: "Calculator",
    url: "calculator",
    languages: [],
    tools: []
  }
];


export default function Projects() {
    const [isAdmin] = useState(true);
    const [isAdding, setIsAdding] = useState(false);

    return (
        <div>
            <h1>Projects</h1>
            <div>
                {projects.map(p => (
                    <ProjectTypeCard key={p.url} {...p} />
                ))}
            </div>
            {
                isAdmin && 
                (
                    <button onClick={() => setIsAdding(true)}>AddProject</button>
                )
            }
            {
                isAdding &&
                (
                    <div>
                        <p>name</p><input type="text"/>
                        <p>type</p><select name="asd" id="asd"></select>
                        <p>newType</p><input type="text"/>
                        <p>tools</p><select name="asd" id="asd"></select>
                        <p>newTool</p><input type="text"/>
                        <p>project</p><input type="file" name="" id="" />
                        <p></p>
                        <button onClick={() => setIsAdding(false)}>Done</button>
                    </div>
                )
            }
        </div>
    );
}