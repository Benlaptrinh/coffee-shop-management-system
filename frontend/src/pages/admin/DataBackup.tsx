import { Link } from "react-router-dom"

export default function AdminDataBackup() {
  return (
    <div className="content-wrapper">
      <h1>Sao luu du lieu</h1>
      <div className="form-box">
        <p>Click "Backup" to download a backup file.</p>
        <form method="post" action="/admin/data/backup" className="form-inline">
          <button type="submit" className="btn btn-primary">
            Backup
          </button>
          <Link className="btn btn-cancel" style={{ marginLeft: 8 }} to="/admin/dashboard">
            Cancel
          </Link>
        </form>
      </div>
    </div>
  )
}
