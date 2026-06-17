import { StrictMode, useEffect } from 'react'
import { createBrowserRouter, Outlet, RouterProvider } from 'react-router-dom'

import { SessionProvider } from '@/context/SessionContext'
import { getSession } from '@/scripts/getSession'

import MatomoPageTracker from '@/components/matomo/MatomoPageTracker'

import Footer from '@/components/Footer/Footer'
import Header from '@/components/Header/Header'
import Home from '@/pages/Home'
import UserProfile from '@/pages/UserProfile'
import NotFound from '@/pages/error/NotFound'
import ProjectTypes from '@/pages/projectTypes/ProjectTypes'
import LogIn from '@/pages/session/LogIn'
import SignUp from '@/pages/session/SignUp'
import LogOut from '@/pages/session/LogOut'
import ProjectType from '@/pages/projectType/ProjectType'
import Project from '@/pages/project/Project'

import '@/styles/global.css'
import '@/styles/tokens.css'

function RootLayout() {
    return (
        <>
            <MatomoPageTracker />
            <Header />
            <main className="page-container">
                <Outlet />
            </main>
            <Footer />
        </>
    )
}

const router = createBrowserRouter([
    {
        path: '/',
        element: <RootLayout />,
        errorElement: (
            <>
                <Header />
                <main className="page-container">
                    <NotFound />
                </main>
                <Footer />
            </>
        ),
        children: [
            {
                index: true,
                element: <Home />
            },
            {
                path: 'user-profile',
                element: <UserProfile />
            },
            {
                path: 'signup',
                element: <SignUp />
            },
            {
                path: 'login',
                element: <LogIn />
            },
            {
                path: 'logout',
                element: <LogOut />
            },
            {
                path: 'projects',
                element: <ProjectTypes />
            },
            {
                path: 'projects/:typeSlug',
                element: <ProjectType />
            },
            {
                path: 'projects/:typeSlug/:projectSlug',
                element: <Project />
            }
        ]
    }
])

export default function App() {
    useEffect(() => {
        getSession()
    }, [])

    return (
        <StrictMode>
            <SessionProvider>
                <RouterProvider router={router} />
            </SessionProvider>
        </StrictMode>
    )
}