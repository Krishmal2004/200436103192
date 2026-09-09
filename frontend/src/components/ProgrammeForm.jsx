import { useState } from 'react'

export default function ProgrammeForm({ onSubmit, onCancel, submitting }) {
  const [title, setTitle] = useState('')
  const [trainingDate, setTrainingDate] = useState('')
  const [venue, setVenue] = useState('')
  const [trainer, setTrainer] = useState('')
  const [maxParticipants, setMaxParticipants] = useState('')

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!title || !trainingDate || !maxParticipants) return

    const ok = await onSubmit({
      title,
      trainingDate,
      venue: venue || null,
      trainer: trainer || null,
      maxParticipants: Number(maxParticipants),
    })

    if (ok) {
      setTitle('')
      setTrainingDate('')
      setVenue('')
      setTrainer('')
      setMaxParticipants('')
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
        <input type="date" value={trainingDate} onChange={(e) => setTrainingDate(e.target.value)} required />
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
