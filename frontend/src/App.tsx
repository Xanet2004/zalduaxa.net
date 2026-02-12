import { StrictMode, useEffect } from 'react'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'

import { SessionProvider } from '@/context/SessionContext'
import { getSession } from '@/scripts/getSession'

import Footer from '@/components/Footer/Footer'
import Header from '@/components/Header/Header'
import Home from '@/pages/Home'
import UserProfile from '@/pages/UserProfile'
import NotFound from '@/pages/error/NotFound'
import ProjectTypes from '@/pages/projectTypes/ProjectTypes'
import LogIn from '@/pages/session/LogIn'
import SignUp from '@/pages/session/SignUp'
import '@/styles/global.css'
import '@/styles/tokens.css'
import LogOut from './pages/session/LogOut'
import ProjectType from './pages/projectType/ProjectType'
import Project from './pages/project/Project'

export default function App() {
    const router = createBrowserRouter([
        {
            path: '/',
            element: (
                <>
                    <Header />
                    <main className="page-container">
                        <Home />
                    </main>
                    <Footer />
                </>
            ),
            errorElement: (
                <>
                    <Header />
                    <main className="page-container">
                        <NotFound />
                    </main>
                    <Footer />
                </>
            )
        },
        {
            path: '/user-profile',
            element: (
                <>
                    <Header />
                    <main className="page-container">
                        <UserProfile />
                    </main>
                    <Footer />
                </>
            )
        },
        {
            path: '/signup',
            element: (
                <>
                    <Header />
                    <main className="page-container">
                        <SignUp />
                    </main>
                    <Footer />
                </>
            )
        },
        {
            path: '/login',
            element: (
                <>
                    <Header />
                    <main className="page-container">
                        <LogIn />
                    </main>
                    <Footer />
                </>
            )
        },
        {
            path: '/logout',
            element: (
                <>
                    <Header />
                    <main className="page-container">
                        <LogOut />
                    </main>
                    <Footer />
                </>
            )
        },
        {
            path: '/projects',
            element: (
                <>
                    <Header />
                    <main className="page-container">
                        <ProjectTypes />
                    </main>
                    <Footer />
                </>
            )
        },
        {
            path: "/projects/:typeSlug",
            element: (
                <>
                    <Header />
                    <main className="page-container">
                        <ProjectType />
                    </main>
                    <Footer />
                </>
            )
        },
        {
            path: "/projects/:typeSlug/:projectSlug",
            element: (
                <>
                    <Header />
                    <main className="page-container">
                        <Project />
                    </main>
                    <Footer />
                </>
            )
        }
    ]);

    useEffect(() => {
        getSession();
    }, [])

    return (
        <StrictMode>
            <SessionProvider>
                <RouterProvider router={router} />
            </SessionProvider>
        </StrictMode>
    );
}