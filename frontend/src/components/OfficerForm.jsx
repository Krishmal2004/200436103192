import { useState } from 'react'

// Local YYYY-MM-DD for "today" — joined date can't be in the future.
const todayIso = () => {
  const d = new Date()
  const offsetMs = d.getTimezoneOffset() * 60000
  return new Date(d.getTime() - offsetMs).toISOString().slice(0, 10)
}

export default function OfficerForm({ departments, onSubmit, onCancel, submitting }) {
  const [employeeNo, setEmployeeNo] = useState('')
  const [name, setName] = useState('')
  const [departmentId, setDepartmentId] = useState('')
  const [grade, setGrade] = useState('')
  const [joinedDate, setJoinedDate] = useState('')
  const maxDate = todayIso()

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!employeeNo || !name || !departmentId) return

    const ok = await onSubmit({
      employeeNo,
      name,
      departmentId: Number(departmentId),
      grade: grade || null,
      joinedDate: joinedDate || null,
    })

    if (ok) {
      setEmployeeNo('')
      setName('')
      setDepartmentId('')
      setGrade('')
      setJoinedDate('')
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="field">
        <label>Employee No.</label>
        <input type="text" value={employeeNo} onChange={(e) => setEmployeeNo(e.target.value)} required />
      </div>

      <div className="field">
        <label>Name</label>
        <input type="text" value={name} onChange={(e) => setName(e.target.value)} required />
      </div>

      <div className="field">
        <label>Department</label>
        <select value={departmentId} onChange={(e) => setDepartmentId(e.target.value)} required>
          <option value="">Select department…</option>
          {departments.map((d) => (
            <option key={d.id} value={d.id}>
              {d.name}
            </option>
          ))}
        </select>
      </div>

      <div className="field">
        <label>Grade / Designation (optional)</label>
        <input
          type="text"
          placeholder="e.g. Senior Officer"
          value={grade}
          onChange={(e) => setGrade(e.target.value)}
        />
        <p className="field-hint">Needed only for programmes with a grade-based eligibility rule.</p>
      </div>

      <div className="field">
        <label>Joined Date (optional)</label>
        <input type="date" value={joinedDate} max={maxDate} onChange={(e) => setJoinedDate(e.target.value)} />
        <p className="field-hint">Needed only for programmes with a minimum-years-of-service rule.</p>
      </div>

      <div className="form-actions">
        <button type="submit" className="primary" disabled={submitting}>
          {submitting ? 'Saving…' : 'Save Officer'}
        </button>
        <button type="button" className="secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
      </div>
    </form>
  )
}
