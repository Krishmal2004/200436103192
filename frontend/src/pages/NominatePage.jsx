import { useEffect, useState, useCallback } from 'react'
import {
  getDepartments,
  getOfficers,
  getProgrammes,
  getNominations,
  createNomination,
  createProgramme,
} from '../api'
import NominationForm from '../components/NominationForm'
import NominationList from '../components/NominationList'
import ProgrammeForm from '../components/ProgrammeForm'

export default function NominatePage() {
  const [departments, setDepartments] = useState([])
  const [officers, setOfficers] = useState([])
  const [programmes, setProgrammes] = useState([])
  const [selectedProgrammeId, setSelectedProgrammeId] = useState('')

  const [nominations, setNominations] = useState([])
  const [loadingNominations, setLoadingNominations] = useState(false)

  const [submitting, setSubmitting] = useState(false)
  const [banner, setBanner] = useState(null) // { type: 'error' | 'success', text }

  const [showProgrammeForm, setShowProgrammeForm] = useState(false)
  const [creatingProgramme, setCreatingProgramme] = useState(false)

  // Load reference data (departments, officers, programmes) once on mount.
  useEffect(() => {
    getDepartments().then(setDepartments).catch(() => {})
    getOfficers().then(setOfficers).catch(() => {})
    getProgrammes().then((data) => {
      setProgrammes(data)
      if (data.length > 0) setSelectedProgrammeId(String(data[0].id))
    }).catch(() => {})
  }, [])

  // Re-fetches programmes so confirmedCount stays accurate after a nomination
  // is created (cancellation, which also affects confirmedCount, now lives
  // on the All Participants page).
  const refreshProgrammes = useCallback(() => {
    getProgrammes().then(setProgrammes).catch(() => {})
  }, [])

  const refreshNominations = useCallback((programmeId) => {
    if (!programmeId) {
      setNominations([])
      return
    }
    setLoadingNominations(true)
    getNominations(programmeId)
      .then(setNominations)
      .catch(() => setNominations([]))
      .finally(() => setLoadingNominations(false))
  }, [])

  useEffect(() => {
    refreshNominations(selectedProgrammeId)
  }, [selectedProgrammeId, refreshNominations])

  const handleSubmitNomination = async (payload) => {
    setSubmitting(true)
    setBanner(null)
    try {
      const created = await createNomination(payload)
      setBanner({
        type: 'success',
        text:
          created.status === 'CONFIRMED'
            ? `Nomination confirmed for ${created.officerName}.`
            : `Programme is full — ${created.officerName} has been waitlisted.`,
      })
      refreshNominations(selectedProgrammeId)
      refreshProgrammes()
      return true
    } catch (err) {
      const data = err?.response?.data
      if (err?.response?.status === 409) {
        // Duplicate nomination rejected by the backend (Task 1 behaviour).
        setBanner({ type: 'error', text: data?.message || 'This officer is already nominated for this programme.' })
      } else {
        setBanner({ type: 'error', text: data?.message || 'Something went wrong submitting the nomination.' })
      }
      return false
    } finally {
      setSubmitting(false)
    }
  }

  const handleCreateProgramme = async (payload) => {
    setCreatingProgramme(true)
    setBanner(null)
    try {
      const created = await createProgramme(payload)
      setProgrammes((prev) => [...prev, created])
      setSelectedProgrammeId(String(created.id))
      setShowProgrammeForm(false)
      setBanner({ type: 'success', text: `Programme "${created.title}" created.` })
      return true
    } catch (err) {
      const data = err?.response?.data
      setBanner({ type: 'error', text: data?.message || 'Something went wrong creating the programme.' })
      return false
    } finally {
      setCreatingProgramme(false)
    }
  }

  const selectedProgramme = programmes.find((p) => String(p.id) === String(selectedProgrammeId))

  // "Current Nominations" here is the confirmed roster; waitlisted/cancelled
  // history for the programme is still visible on the All Participants page.
  const confirmedNominations = nominations.filter((n) => n.status === 'CONFIRMED')

  return (
    <>
      <div className="card">
        <div className="card-header">
          <h2>Training Programme</h2>
          {!showProgrammeForm && (
            <button type="button" className="secondary" onClick={() => setShowProgrammeForm(true)}>
              + Add Programme
            </button>
          )}
        </div>

        {showProgrammeForm ? (
          <ProgrammeForm
            onSubmit={handleCreateProgramme}
            onCancel={() => setShowProgrammeForm(false)}
            submitting={creatingProgramme}
          />
        ) : (
          <>
            <div className="field">
              <label>Programme</label>
              <select value={selectedProgrammeId} onChange={(e) => setSelectedProgrammeId(e.target.value)}>
                {programmes.length === 0 && <option value="">No programmes yet</option>}
                {programmes.map((p) => (
                  <option key={p.id} value={p.id}>
                    {p.title} — {p.trainingDate}
                  </option>
                ))}
              </select>
            </div>
            {selectedProgramme && (
              <p style={{ color: '#6b7280', fontSize: '0.9rem', margin: 0 }}>
                Venue: {selectedProgramme.venue || '—'} · Trainer: {selectedProgramme.trainer || '—'} ·{' '}
                {selectedProgramme.confirmedCount}/{selectedProgramme.maxParticipants} confirmed
              </p>
            )}
          </>
        )}
      </div>

      <div className="card">
        <h2>Nominate an Officer</h2>
        {banner && <div className={`banner ${banner.type}`}>{banner.text}</div>}
        <NominationForm
          departments={departments}
          officers={officers}
          selectedProgrammeId={selectedProgrammeId}
          onSubmit={handleSubmitNomination}
          submitting={submitting}
        />
      </div>

      <div className="card">
        <h2>Current Nominations</h2>
        <NominationList
          nominations={confirmedNominations}
          loading={loadingNominations}
          emptyMessage="No confirmed nominations yet for this programme."
        />
      </div>
    </>
  )
}
