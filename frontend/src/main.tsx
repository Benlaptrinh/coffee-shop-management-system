import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import './styles/admin.css'
import './styles/responsive.css'
import './styles/app.css'
import './index.css'
import App from './App.tsx'
// @ts-ignore
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'

document.body.classList.add('app-body')

const queryClient = new QueryClient()

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <QueryClientProvider client={queryClient}>
      <App />
    </QueryClientProvider>
  </StrictMode>,
)
