const badgeClassFor = (status) => {
  if (status === 'CONFIRMED') return 'confirmed'
  if (status === 'WAITLISTED') return 'waitlisted'
  return 'cancelled'
}

export default function NominationList({
  nominations,
  loading,
  onCancel,
  cancellingId,
  showProgramme = false,
  emptyMessage = 'No nominations yet for this programme.',
}) {
  if (loading) {
    return <p className="empty-state">Loading nominations…</p>
  }

  if (nominations.length === 0) {
    return <p className="empty-state">{emptyMessage}</p>
  }

  return (
    <table>
      <thead>
        <tr>
          {showProgramme && <th>Programme</th>}
          <th>Officer</th>
          <th>Employee No.</th>
          <th>Nominated By</th>
          <th>Status</th>
          <th>Submitted At</th>
          <th></th>
        </tr>
      </thead>
      <tbody>
        {nominations.map((n) => (
          <tr key={n.id}>
            {showProgramme && <td>{n.programmeTitle}</td>}
            <td>{n.officerName}</td>
            <td>{n.employeeNo}</td>
            <td>{n.departmentName}</td>
            <td>
              <span className={`badge ${badgeClassFor(n.status)}`}>{n.status}</span>
            </td>
            <td>{new Date(n.createdAt).toLocaleString()}</td>
            <td>
              {n.status !== 'CANCELLED' && onCancel && (
                <button
                  type="button"
                  className="link-danger"
                  onClick={() => onCancel(n.id)}
                  disabled={cancellingId === n.id}
                >
                  {cancellingId === n.id ? 'Cancelling…' : 'Cancel'}
                </button>
              )}
            </td>
          </tr>
        ))}
      </tbody>
    </table>
  )
}
