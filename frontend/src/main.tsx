import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './index.css'
import './styles/admin.css'
import './styles/responsive.css'
import './styles/app.css'
import App from './App.tsx'

document.body.classList.add('app-body')

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <App />
  </StrictMode>,
)
