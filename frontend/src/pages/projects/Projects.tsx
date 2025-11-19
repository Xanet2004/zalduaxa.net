import { useEffect, useState } from 'react';
import { useSession } from "@/context/SessionContext";
import { getProjectTypes } from '@/scripts/getProjectTypes';
import ProjectTypeCard from '@/components/ProjectTypeCard/ProjectTypeCard';
import { addProjectType } from '@/scripts/addProjectType';
import type { ProjectType } from '@/types/projectType';

export default function Projects() {
    const { user } = useSession();
    const isAdmin = user?.role?.name == 'admin';
    const [isAddingProjectType, setIsAddingProjectType] = useState<boolean>();
    const [projectTypes, setProjectTypes] = useState<ProjectType[] | null>(null);

    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const { refreshUser } = useSession(); 

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        setAddProjectTypeForm(prev => ({ ...prev, [e.target.name]: e.target.value }));
    }

    const [addProjectTypeForm, setAddProjectTypeForm] = useState({
        name: "",
        description: "",
        imagePath: "",
    });

    useEffect(() => {
        const fetchProjectTypes = async () => {
            try {
                setLoading(true);
                const data: ProjectType[] = await getProjectTypes();
                setProjectTypes(data);
            } catch (err) {
                console.error(err);
            } finally {
                setLoading(false);
            }
        };

        fetchProjectTypes();
    }, []);
    
    async function handleAddProjectType(e: React.FormEvent) {
        e.preventDefault();
        setLoading(true);
        await addProjectType(addProjectTypeForm);
        setLoading(false);
    }

    return (
        <div>
            <h1>Projects</h1>
            <div>
                {projectTypes && projectTypes.map((pt: ProjectType) => (
                    <ProjectTypeCard key={pt.id} {...pt} />
                ))}
            </div>
            {
                isAdmin && 
                (
                    <button onClick={() => setIsAddingProjectType(true)}>AddProject</button>
                )
            }
            {
                isAddingProjectType &&
                (
                    <form onSubmit={handleAddProjectType}>
                        <p>name</p>
                        <input name="name" value={addProjectTypeForm.name} onChange={handleChange}/>
                        <p>description</p>
                        <input name="description" value={addProjectTypeForm.description} onChange={handleChange}/>
                        <p>image</p>
                        <input name="imagePath" value={addProjectTypeForm.imagePath} onChange={handleChange}/>

                        <button type="submit" disabled={loading}>
                            {loading ? "Adding..." : "Add project"}
                        </button>
                    </form>
                )
            }
        </div>
    );
}