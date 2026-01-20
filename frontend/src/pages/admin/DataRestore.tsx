import { Link } from "react-router-dom"

export default function AdminDataRestore() {
  return (
    <div className="content-wrapper">
      <h1>Phuc hoi du lieu</h1>
      <div className="form-box">
        <p>Select a .sql file to restore the database.</p>
        <form method="post" action="/admin/data/restore" encType="multipart/form-data" className="form-inline">
          <input type="file" name="file" accept=".sql" required />
          <div style={{ marginTop: 8 }}>
            <button type="submit" className="btn btn-primary">
              Restore
            </button>
            <Link className="btn btn-cancel" style={{ marginLeft: 8 }} to="/admin/dashboard">
              Cancel
            </Link>
          </div>
        </form>
      </div>
    </div>
  )
}
