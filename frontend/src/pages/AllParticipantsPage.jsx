import { useCallback, useEffect, useMemo, useState } from 'react'
import { getAllNominations, cancelNomination } from '../api'
import NominationList from '../components/NominationList'
import NominationFilters from '../components/NominationFilters'

// Office-wide participant register: every nomination across every
// programme, on its own page (see NavBar) rather than folded into the
// per-programme nomination flow.
export default function AllParticipantsPage() {
  const [allNominations, setAllNominations] = useState([])
  const [loading, setLoading] = useState(false)
  const [cancellingId, setCancellingId] = useState(null)
  const [banner, setBanner] = useState(null)

  const [status, setStatus] = useState('ALL')
  const [programmeId, setProgrammeId] = useState('ALL')
  const [departmentId, setDepartmentId] = useState('ALL')
  const [search, setSearch] = useState('')

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

  // Filter dropdown options are derived from the data itself, so they only
  // ever list programmes/departments that actually have a nomination here.
  const programmeOptions = useMemo(() => {
    const byId = new Map()
    allNominations.forEach((n) => byId.set(n.programmeId, { id: n.programmeId, title: n.programmeTitle }))
    return [...byId.values()].sort((a, b) => a.title.localeCompare(b.title))
  }, [allNominations])

  const departmentOptions = useMemo(() => {
    const byId = new Map()
    allNominations.forEach((n) => byId.set(n.departmentId, { id: n.departmentId, name: n.departmentName }))
    return [...byId.values()].sort((a, b) => a.name.localeCompare(b.name))
  }, [allNominations])

  const filteredNominations = useMemo(() => {
    const term = search.trim().toLowerCase()
    return allNominations.filter((n) => {
      if (status !== 'ALL' && n.status !== status) return false
      if (programmeId !== 'ALL' && String(n.programmeId) !== programmeId) return false
      if (departmentId !== 'ALL' && String(n.departmentId) !== departmentId) return false
      if (term && !n.officerName.toLowerCase().includes(term) && !n.employeeNo.toLowerCase().includes(term)) {
        return false
      }
      return true
    })
  }, [allNominations, status, programmeId, departmentId, search])

  const hasActiveFilters =
    status !== 'ALL' || programmeId !== 'ALL' || departmentId !== 'ALL' || search.trim() !== ''

  const clearFilters = () => {
    setStatus('ALL')
    setProgrammeId('ALL')
    setDepartmentId('ALL')
    setSearch('')
  }

  return (
    <div className="card">
      <div className="card-header">
        <h2>All Participants</h2>
        <span className="participant-count">
          {hasActiveFilters
            ? `${filteredNominations.length} of ${allNominations.length} nominations`
            : `${allNominations.length} nomination${allNominations.length === 1 ? '' : 's'} across all programmes`}
        </span>
      </div>

      {banner && <div className={`banner ${banner.type}`}>{banner.text}</div>}

      {allNominations.length > 0 && (
        <NominationFilters
          status={status}
          onStatusChange={setStatus}
          programmeId={programmeId}
          onProgrammeChange={setProgrammeId}
          departmentId={departmentId}
          onDepartmentChange={setDepartmentId}
          search={search}
          onSearchChange={setSearch}
          programmeOptions={programmeOptions}
          departmentOptions={departmentOptions}
          onClear={clearFilters}
          hasActiveFilters={hasActiveFilters}
        />
      )}

      <NominationList
        nominations={filteredNominations}
        loading={loading}
        onCancel={handleCancel}
        cancellingId={cancellingId}
        showProgramme
        emptyMessage={
          hasActiveFilters ? 'No nominations match these filters.' : 'No nominations have been submitted yet.'
        }
      />
    </div>
  )
}
