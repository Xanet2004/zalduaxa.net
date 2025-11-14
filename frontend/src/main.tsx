import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { createBrowserRouter, RouterProvider } from 'react-router-dom'
// import './index.css'
import '@/styles/tokens.css'
import '@/styles/global.css'
import Home from '@/pages/Home.tsx'
import Profile from '@/pages/Profile.tsx'
import NotFound from '@/pages/NotFound.tsx'
import Header from '@/components/Header/Header'
import Footer from '@/components/Footer/Footer'

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
  }
]);

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <RouterProvider router={router}/>
  </StrictMode>,
)
