import { useState } from 'react'

export default function NominationForm({ departments, officers, selectedProgrammeId, onSubmit, submitting }) {
  const [departmentId, setDepartmentId] = useState('')
  const [officerId, setOfficerId] = useState('')
  const [submittedBy, setSubmittedBy] = useState('')

  // Only offer officers who belong to the selected nominating department.
  const officersInDepartment = departmentId
    ? officers.filter((o) => String(o.departmentId) === String(departmentId))
    : officers

  const handleDepartmentChange = (e) => {
    setDepartmentId(e.target.value)
    // The previously selected officer may not belong to the new department.
    setOfficerId('')
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!selectedProgrammeId || !departmentId || !officerId) return

    const ok = await onSubmit({
      programmeId: Number(selectedProgrammeId),
      departmentId: Number(departmentId),
      officerId: Number(officerId),
      submittedBy: submittedBy || null,
    })

    // Only clear the officer/submitter fields on success, so a rejected
    // duplicate stays on screen for the user to see what they picked.
    if (ok) {
      setOfficerId('')
      setSubmittedBy('')
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="field">
        <label>Nominating Department</label>
        <select value={departmentId} onChange={handleDepartmentChange} required>
          <option value="">Select department…</option>
          {departments.map((d) => (
            <option key={d.id} value={d.id}>
              {d.name}
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label>Officer</label>
        <select
          value={officerId}
          onChange={(e) => setOfficerId(e.target.value)}
          required
          disabled={!departmentId}
        >
          <option value="">
            {departmentId ? 'Select officer…' : 'Select a department first…'}
          </option>
          {officersInDepartment.map((o) => (
            <option key={o.id} value={o.id}>
              {o.name} ({o.employeeNo})
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label>Submitted by (optional)</label>
        <input
          type="text"
          placeholder="e.g. your name"
          value={submittedBy}
          onChange={(e) => setSubmittedBy(e.target.value)}
        />
      </div>

      <button type="submit" className="primary" disabled={!selectedProgrammeId || submitting}>
        {submitting ? 'Submitting…' : 'Submit Nomination'}
      </button>
    </form>
  )
}
