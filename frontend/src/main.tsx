import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
// import './index.css'
import Footer from '@/components/Footer/Footer'
import Header from '@/components/Header/Header'
import Home from '@/pages/Home'
import NotFound from '@/pages/error/NotFound'
import SignUp from '@/pages/session/SignUp'
import LogIn from '@/pages/session/LogIn'
import Profile from '@/pages/Profile'
import Projects from '@/pages/projects/Projects'
import '@/styles/global.css'
import '@/styles/tokens.css'

const router = createBrowserRouter([
  {
    path: '/',
    element: (
      <>
        <Header/>
          <main className="page-container">
            <Home />
          </main>
        <Footer/>
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
        <Header/>
        <main className="page-container">
          <Profile />
        </main>
        <Footer/>
      </>
    )
  },
  {
    path: '/projects',
    element: (
      <>
        <Header/>
        <main className="page-container">
          <Projects />
        </main>
        <Footer/>
      </>
    )
  },
  {
    path: '/signup',
    element: (
      <>
        <Header/>
        <main className="page-container">
          <SignUp />
        </main>
        <Footer/>
      </>
    )
  },
  {
    path: '/login',
    element: (
      <>
        <Header/>
        <main className="page-container">
          <LogIn />
        </main>
        <Footer/>
      </>
    )
  }
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router}/>
  </StrictMode>,
)
