import { StrictMode, useEffect, useState } from 'react'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'

import { getSession } from '@/scripts/getSession'

import Footer from '@/components/Footer/Footer'
import Header from '@/components/Header/Header'
import Home from '@/pages/Home'
import Profile from '@/pages/Profile'
import NotFound from '@/pages/error/NotFound'
import Projects from '@/pages/projects/Projects'
import LogIn from '@/pages/session/LogIn'
import SignUp from '@/pages/session/SignUp'
import '@/styles/global.css'
import '@/styles/tokens.css'
import type { User } from './types/user'
import LogOut from './pages/session/LogOut'

export default function App() {
    const [user, setUser] = useState<User | null>();

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
            path: '/profile',
            element: (
                <>
                    <Header />
                    <main className="page-container">
                        <Profile />
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
                        <Projects />
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
        }
    ]);

    useEffect(() => {
        getSession();
        const saved = sessionStorage.getItem('user');
        if (saved) {
            setUser(JSON.parse(saved));
        }
    }, [])

    return (
        <StrictMode>
            {/* <p>{sessionStorage.getItem('user')}</p> */}
            {user && <p>{user.username}</p>}
            <RouterProvider router={router} />
        </StrictMode>
    );
}