import React, { useEffect, useState } from "react"
import { useLocation } from "react-router-dom"
import Footer from "./Footer"
import Header from "./Header"

type AppLayoutProps = {
  sidebar: React.ReactNode
  children: React.ReactNode
}

export default function AppLayout({ sidebar, children }: AppLayoutProps) {
  const [sidebarOpen, setSidebarOpen] = useState(false)
  const location = useLocation()

  useEffect(() => {
    document.body.style.overflow = sidebarOpen ? "hidden" : ""
    return () => {
      document.body.style.overflow = ""
    }
  }, [sidebarOpen])

  // Đóng sidebar sau khi chuyển route (mobile click vào menu xong tự ẩn)
  useEffect(() => {
    setSidebarOpen(false)
  }, [location.pathname])

  return (
    <>
      <header className="app-header">
        <Header />
      </header>
      <main className="app-main">
        <div className="mx-auto w-full max-w-6xl px-4 sm:px-6 lg:px-8">
          <button
            className="sidebar-toggle no-print inline-flex items-center gap-2 rounded-full border border-border bg-surface px-4 py-2 text-sm font-semibold text-text shadow-soft transition hover:bg-surface-muted min-[901px]:hidden"
            type="button"
            aria-controls="app-sidebar"
            aria-expanded={sidebarOpen}
            onClick={() => setSidebarOpen((prev) => !prev)}
          >
            Danh mục
          </button>
          <div className="app-layout flex flex-col gap-4 lg:flex-row lg:gap-6">
            <aside
              className={`sidebar fixed top-[100px] bottom-0 left-0 z-50 h-[calc(100vh-72px)] w-[82vw] max-w-[320px] overflow-y-auto bg-surface shadow-soft transition-all duration-200 rounded-r-2xl min-[901px]:sticky min-[901px]:top-[88px] min-[901px]:bottom-auto min-[901px]:h-fit min-[901px]:z-auto min-[901px]:w-[270px] min-[901px]:max-w-none min-[901px]:shadow-none min-[901px]:rounded-none ${
                sidebarOpen
                  ? "translate-x-0 opacity-100 pointer-events-auto"
                  : "-translate-x-full opacity-0 pointer-events-none"
              } min-[901px]:translate-x-0 min-[901px]:opacity-100 min-[901px]:pointer-events-auto`}
              id="app-sidebar"
            >
              {sidebar}
            </aside>
            <section className="content-area w-full min-w-0">{children}</section>
          </div>
        </div>
      </main>
      <div
        id="sidebar-overlay"
        className={`sidebar-overlay ${sidebarOpen ? "block" : "hidden"} fixed inset-0 z-40 bg-black/40 min-[901px]:hidden`}
        aria-hidden={!sidebarOpen}
        onClick={() => setSidebarOpen(false)}
      />
      <footer className="app-footer">
        <Footer />
      </footer>
    </>
  )
}
