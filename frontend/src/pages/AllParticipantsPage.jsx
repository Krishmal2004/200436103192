import { useCallback, useEffect, useState } from 'react'
import { getAllNominations, cancelNomination } from '../api'
import NominationList from '../components/NominationList'

// Office-wide participant register: every nomination across every
// programme, on its own page (see NavBar) rather than folded into the
// per-programme nomination flow.
export default function AllParticipantsPage() {
  const [allNominations, setAllNominations] = useState([])
  const [loading, setLoading] = useState(false)
  const [cancellingId, setCancellingId] = useState(null)
  const [banner, setBanner] = useState(null)

  const refresh = useCallback(() => {
    setLoading(true)
    getAllNominations()
      .then(setAllNominations)
      .catch(() => setAllNominations([]))
      .finally(() => setLoading(false))
  }, [])

  useEffect(() => {
    refresh()
  }, [refresh])

  const handleCancel = async (nominationId) => {
    setCancellingId(nominationId)
    setBanner(null)
    try {
      await cancelNomination(nominationId)
      setBanner({ type: 'success', text: 'Nomination cancelled. Anyone next on the waiting list has been promoted.' })
      refresh()
    } catch (err) {
      const data = err?.response?.data
      setBanner({ type: 'error', text: data?.message || 'Something went wrong cancelling the nomination.' })
    } finally {
      setCancellingId(null)
    }
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2>All Participants</h2>
        <span className="participant-count">
          {allNominations.length} nomination{allNominations.length === 1 ? '' : 's'} across all programmes
        </span>
      </div>
      {banner && <div className={`banner ${banner.type}`}>{banner.text}</div>}
      <NominationList
        nominations={allNominations}
        loading={loading}
        onCancel={handleCancel}
        cancellingId={cancellingId}
        showProgramme
        emptyMessage="No nominations have been submitted yet."
      />
    </div>
  )
}
