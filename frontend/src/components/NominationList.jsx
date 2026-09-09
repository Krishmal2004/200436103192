export default function NominationList({ nominations, loading }) {
  if (loading) {
    return <p className="empty-state">Loading nominations…</p>
  }

  if (nominations.length === 0) {
    return <p className="empty-state">No nominations yet for this programme.</p>
  }

  return (
    <table>
      <thead>
        <tr>
          <th>Officer</th>
          <th>Employee No.</th>
          <th>Nominated By</th>
          <th>Status</th>
          <th>Submitted At</th>
        </tr>
      </thead>
      <tbody>
        {nominations.map((n) => (
          <tr key={n.id}>
            <td>{n.officerName}</td>
            <td>{n.employeeNo}</td>
            <td>{n.departmentName}</td>
            <td>
              <span className={`badge ${n.status === 'CONFIRMED' ? 'confirmed' : 'waitlisted'}`}>
                {n.status}
              </span>
            </td>
            <td>{new Date(n.createdAt).toLocaleString()}</td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
