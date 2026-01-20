import React from "react"
import { Navigate } from "react-router-dom"
import { useAuth } from "../auth"

type RequireRoleProps = {
  role: string
  children: React.ReactNode
}

export default function RequireRole({ role, children }: RequireRoleProps) {
  const { user } = useAuth()
  if (!user || !user.roles.includes(role)) {
    return <Navigate to="/" replace />
  }
  return <>{children}</>
}
