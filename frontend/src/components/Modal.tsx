import React from "react"

type ModalProps = {
  open: boolean
  onClose: () => void
  children: React.ReactNode
}

export default function Modal({ open, onClose, children }: ModalProps) {
  if (!open) return null

  return (
    <div id="modal" role="dialog" aria-modal="true" onClick={onClose}>
      <div id="modal-body" onClick={(event) => event.stopPropagation()}>
        {children}
      </div>
    </div>
  )
}
