import React, { createContext, useContext, useEffect, useMemo, useState, useCallback } from "react"
import api from "./api"

type AuthUser = {
  username: string
  roles: string[]
}

type AuthContextType = {
  token: string | null
  user: AuthUser | null
  login: (payload: { token: string; username: string; roles: string[] }) => void
  logout: () => void
}

const AuthContext = createContext<AuthContextType>({
  token: null,
  user: null,
  login: () => {},
  logout: () => {},
})

const USER_KEY = "auth_user"

function loadUser(): AuthUser | null {
  const raw = localStorage.getItem(USER_KEY)
  if (!raw) return null
  try {
    return JSON.parse(raw) as AuthUser
  } catch (err) {
    return null
  }
}

export const AuthProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const [token, setToken] = useState<string | null>(() => {
    // Only read from localStorage once on mount
    if (typeof window !== "undefined") {
      return localStorage.getItem("token")
    }
    return null
  })
  const [user, setUser] = useState<AuthUser | null>(() => loadUser())

  useEffect(() => {
    if (token) {
      localStorage.setItem("token", token)
      api.setToken(token)
    } else {
      localStorage.removeItem("token")
      api.setToken(null)
    }
  }, [token])

  // subscribe to api unauthorized events to handle logout+redirect in UI
  useEffect(() => {
    const unsub = api.onUnauthorized(() => {
      setToken(null)
      setUser(null)
      localStorage.removeItem(USER_KEY)
      if (window.location.pathname !== "/login") {
        window.location.replace("/login")
      }
    })
    return () => unsub && unsub()
  }, [])

  const login = useCallback((payload: { token: string; username: string; roles: string[] }) => {
    setToken(payload.token)
    const nextUser = { username: payload.username, roles: payload.roles }
    setUser(nextUser)
    localStorage.setItem(USER_KEY, JSON.stringify(nextUser))
  }, [])

  const logout = useCallback(() => {
    setToken(null)
    setUser(null)
    localStorage.removeItem(USER_KEY)
  }, [])

  const value = useMemo(() => ({ token, user, login, logout }), [token, user, login, logout])

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

export const useAuth = () => useContext(AuthContext)
