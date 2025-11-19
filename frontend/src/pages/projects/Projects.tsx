import { useEffect, useState } from 'react';
import { useSession } from "@/context/SessionContext";
import { getProjectTypes } from '@/scripts/getProjectTypes';
import ProjectTypeCard from '@/components/ProjectTypeCard/ProjectTypeCard';

interface ProjectTypes{

}

export default function Projects() {
    const { user } = useSession();
    const isAdmin = user?.role?.name == 'admin';
    const [isAdding, setIsAdding] = useState<boolean>();
    const [projectTypes, setProjectTypes] = useState<ProjectTypes>();

    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const { refreshUser } = useSession(); 

    useEffect(() => {
        try {
            setLoading(true);
            const projectTypes = getProjectTypes();
            console.log(projectTypes)
            setLoading(false);
        } catch (err) {
            console.error(err);
        }
    }, [])

    // async function handleSubmit(e: React.FormEvent) {
    //     e.preventDefault();
    //     try {
    //         setLoading(true);
    //         const loginError = await getProjectTypes();
    //         if (loginError) {
    //             setError(loginError as string);
    //         }
    //         await refreshUser();
    //         setLoading(false);
    //     } catch (err) {
    //         console.error(err);
    //     }
    // }

    return (
        <div>
            <h1>Projects</h1>
            <div>
                {/* {projects.map(p => (
                    <ProjectTypeCard key={p.url} {...p} />
                ))} */}
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
                    <form>
                        <p>name</p><input type="text"/>
                        <p>type</p><select name="asd" id="asd"></select>
                        <p>newType</p><input type="text"/>
                        <p>tools</p><select name="asd" id="asd"></select>
                        <p>newTool</p><input type="text"/>
                        <p>project</p><input type="file" name="" id="" />
                        <p></p>
                        <button type="submit" disabled={loading}>
                            {loading ? "Adding..." : "Add project"}
                        </button>
                    </form>
                )
            }
        </div>
    );
}