import React, { useEffect, useState } from "react"
import Footer from "./Footer"
import Header from "./Header"

type AppLayoutProps = {
  sidebar: React.ReactNode
  children: React.ReactNode
}

export default function AppLayout({ sidebar, children }: AppLayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false)

  useEffect(() => {
    if (sidebarOpen) {
      document.body.classList.add("sidebar-open")
    } else {
      document.body.classList.remove("sidebar-open")
    }
    return () => document.body.classList.remove("sidebar-open")
  }, [sidebarOpen])

  return (
    <>
      <header className="app-header">
        <Header />
      </header>
      <main className="app-main">
        <button
          className="sidebar-toggle no-print"
          type="button"
          aria-controls="app-sidebar"
          aria-expanded={sidebarOpen}
          onClick={() => setSidebarOpen((prev) => !prev)}
        >
          Menu
        </button>
        <div className="app-layout">
          <aside className="sidebar" id="app-sidebar">
            {sidebar}
          </aside>
          <section className="content-area">{children}</section>
        </div>
      </main>
      <div
        id="sidebar-overlay"
        className="sidebar-overlay"
        aria-hidden={!sidebarOpen}
        onClick={() => setSidebarOpen(false)}
      />
      <footer className="app-footer">
        <Footer />
      </footer>
    </>
  )
}
