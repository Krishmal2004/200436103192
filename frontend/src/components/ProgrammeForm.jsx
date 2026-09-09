import { useState } from 'react'

// Local YYYY-MM-DD for "today", so the date input's min matches the browser's
// own local date rather than being shifted by toISOString()'s UTC conversion.
const todayIso = () => {
  const d = new Date()
  const offsetMs = d.getTimezoneOffset() * 60000
  return new Date(d.getTime() - offsetMs).toISOString().slice(0, 10)
}

export default function ProgrammeForm({ onSubmit, onCancel, submitting }) {
  const [title, setTitle] = useState('')
  const [trainingDate, setTrainingDate] = useState('')
  const [venue, setVenue] = useState('')
  const [trainer, setTrainer] = useState('')
  const [maxParticipants, setMaxParticipants] = useState('')
  const [programmeCode, setProgrammeCode] = useState('')
  const minDate = todayIso()

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!title || !trainingDate || !maxParticipants) return
    if (trainingDate < minDate) return

    const ok = await onSubmit({
      title,
      trainingDate,
      venue: venue || null,
      trainer: trainer || null,
      maxParticipants: Number(maxParticipants),
      programmeCode: programmeCode || null,
    })

    if (ok) {
      setTitle('')
      setTrainingDate('')
      setVenue('')
      setTrainer('')
      setMaxParticipants('')
      setProgrammeCode('')
    }
  }

  return (
    <form onSubmit={handleSubmit}>
      <div className="field">
        <label>Title</label>
        <input type="text" value={title} onChange={(e) => setTitle(e.target.value)} required />
      </div>

      <div className="field">
        <label>Training Date</label>
        <input
          type="date"
          value={trainingDate}
          min={minDate}
          onChange={(e) => setTrainingDate(e.target.value)}
          required
        />
      </div>

      <div className="field">
        <label>Venue (optional)</label>
        <input type="text" value={venue} onChange={(e) => setVenue(e.target.value)} />
      </div>

      <div className="field">
        <label>Trainer (optional)</label>
        <input type="text" value={trainer} onChange={(e) => setTrainer(e.target.value)} />
      </div>

      <div className="field">
        <label>Max Participants</label>
        <input
          type="number"
          min="1"
          value={maxParticipants}
          onChange={(e) => setMaxParticipants(e.target.value)}
          required
        />
      </div>

      <div className="field">
        <label>Programme Code (optional)</label>
        <input
          type="text"
          placeholder="e.g. FIN-MGMT — defaults to the title if left blank"
          value={programmeCode}
          onChange={(e) => setProgrammeCode(e.target.value)}
        />
        <p className="field-hint">
          Give recurring programmes the same code across sessions/years so the
          12-month re-registration rule can recognise them as "the same" programme.
        </p>
      </div>

      <div className="form-actions">
        <button type="submit" className="primary" disabled={submitting}>
          {submitting ? 'Saving…' : 'Save Programme'}
        </button>
        <button type="button" className="secondary" onClick={onCancel} disabled={submitting}>
          Cancel
        </button>
      </div>
    </form>
  )
}
