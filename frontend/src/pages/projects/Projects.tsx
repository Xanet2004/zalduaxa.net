import ProjectTypeCard from '@/components/ProjectTypeCard/ProjectTypeCard';
import { useSession } from "@/context/SessionContext";
import { addProjectType } from '@/scripts/addProjectType';
import { deleteProjectType } from '@/scripts/deleteProjectType';
import { getProjectTypes } from '@/scripts/getProjectTypes';
import type { ProjectType } from '@/types/projectType';
import type { RequestProjectType } from '@/types/requestProjectType';
import { useEffect, useState } from 'react';

export default function Projects() {
    const { user } = useSession();
    const isAdmin = user?.role?.name == 'admin';
    const [isAddingProjectType, setIsAddingProjectType] = useState<boolean>();
    const [isDeletingProjectType, setIsDeletingProjectType] = useState<boolean>();
    const [projectTypes, setProjectTypes] = useState<ProjectType[] | null>(null);

    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const { refreshUser } = useSession();

    function handleChange(e: React.ChangeEvent<HTMLInputElement>) {
        const { name, files, value } = e.target;

        if (isAddingProjectType) {
            if (name === "image" && files) {
                setAddProjectTypeForm(prev => ({ ...prev, image: files[0] }));
            } else {
                setAddProjectTypeForm(prev => ({ ...prev, [name]: value }));
            }
        } else if (isDeletingProjectType) {
            setDeleteProjectTypeForm(prev => ({ ...prev, [name]: value }));
        }
    }

    const [addProjectTypeForm, setAddProjectTypeForm] = useState<RequestProjectType>({
        name: "",
        description: "",
        image: null,
    });

    const [deleteProjectTypeForm, setDeleteProjectTypeForm] = useState({
        name: ""
    });

    useEffect(() => {
        const fetchProjectTypes = async () => {
            try {
                setLoading(true);
                const data = await getProjectTypes();
                setProjectTypes(data as ProjectType[]);
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
        const data = await getProjectTypes();
        setProjectTypes(data as ProjectType[]);
        setLoading(false);
    }

    async function handleDeleteProjectType(e: React.FormEvent) {
        e.preventDefault();
        setLoading(true);
        await deleteProjectType(deleteProjectTypeForm);
        const data = await getProjectTypes();
        setProjectTypes(data as ProjectType[]);
        setLoading(false);
    }

    return (
        <div>
            <h1>Projects</h1>
            <div>
                  {Array.isArray(projectTypes) && projectTypes.map(pt => (
                    <ProjectTypeCard key={pt.id} {...pt} />
                ))}
            </div>
            {
                isAdmin && 
                (
                    <button onClick={() => {setIsAddingProjectType(true); setIsDeletingProjectType(false);}}>Add Project</button>
                )
            }
            {
                isAdmin && 
                (
                    <button onClick={() => {setIsAddingProjectType(false); setIsDeletingProjectType(true);}}>Delete Project</button>
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
                        <input name="image" type='file' onChange={handleChange}/>

                        <button type="submit" disabled={loading}>
                            {loading ? "Adding..." : "Add project type"}
                        </button>
                    </form>
                )
            }
            {
                isDeletingProjectType &&
                (
                    <form onSubmit={handleDeleteProjectType}>
                        <p>name</p>
                        <input name="name" value={deleteProjectTypeForm.name} onChange={handleChange}/>

                        <button type="submit" disabled={loading}>
                            {loading ? "Deleting..." : "Delete project type"}
                        </button>
                    </form>
                )
            }
        </div>
    );
}