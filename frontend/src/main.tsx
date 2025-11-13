import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
import './index.css'
import Home from '@/pages/Home.tsx'
import Profile from '@/pages/Profile.tsx'
import NotFound from '@/pages/NotFound.tsx'
import Header from '@/components/Header.tsx'
import Footer from '@/components/Footer.tsx'

const router = createBrowserRouter([
  {
    path: '/',
    element: (
      <>
        <Header/>
        <Home/>
        <Footer/>
      </>
    ),
    errorElement: (
      <>
        <Header />
        <NotFound />
        <Footer />
      </>
    )
  },
  {
    path: '/profile',
    element: (
      <>
        <Header/>
        <Profile/>
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
