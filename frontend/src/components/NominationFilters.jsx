export default function NominationFilters({
  status,
  onStatusChange,
  programmeId,
  onProgrammeChange,
  departmentId,
  onDepartmentChange,
  search,
  onSearchChange,
  programmeOptions,
  departmentOptions,
  onClear,
  hasActiveFilters,
}) {
  return (
    <div className="filters-row">
      <div className="field filters-field">
        <label>Status</label>
        <select value={status} onChange={(e) => onStatusChange(e.target.value)}>
          <option value="ALL">All statuses</option>
          <option value="CONFIRMED">Confirmed</option>
          <option value="WAITLISTED">Waitlisted</option>
          <option value="CANCELLED">Cancelled</option>
        </select>
      </div>

      <div className="field filters-field">
        <label>Programme</label>
        <select value={programmeId} onChange={(e) => onProgrammeChange(e.target.value)}>
          <option value="ALL">All programmes</option>
          {programmeOptions.map((p) => (
            <option key={p.id} value={p.id}>
              {p.title}
            </option>
          ))}
        </select>
      </div>

      <div className="field filters-field">
        <label>Nominated By</label>
        <select value={departmentId} onChange={(e) => onDepartmentChange(e.target.value)}>
          <option value="ALL">All departments</option>
          {departmentOptions.map((d) => (
            <option key={d.id} value={d.id}>
              {d.name}
            </option>
          ))}
        </select>
      </div>

      <div className="field filters-field filters-search">
        <label>Search</label>
        <input
          type="text"
          placeholder="Officer name or employee no."
          value={search}
          onChange={(e) => onSearchChange(e.target.value)}
        />
      </div>

      {hasActiveFilters && (
        <button type="button" className="secondary filters-clear" onClick={onClear}>
          Clear filters
        </button>
      )}
    </div>
  )
}
