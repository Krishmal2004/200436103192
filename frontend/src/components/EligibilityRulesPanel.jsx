import { useCallback, useEffect, useState } from 'react'
import { getEligibilityRules, createEligibilityRule, deleteEligibilityRule } from '../api'

const RULE_TYPE_LABELS = {
  DEPARTMENT: 'Department',
  GRADE: 'Grade',
  MIN_YEARS_OF_SERVICE: 'Min. years of service',
  COOLDOWN_MONTHS: 'Cooldown (months)',
}

// Lets a coordinator configure a programme's eligibility as data — see
// docs/task03_workflow.md. Rules shown here are OR'd within a type (any
// listed department qualifies) and AND'd across types.
export default function EligibilityRulesPanel({ programmeId, departments }) {
  const [rules, setRules] = useState([])
  const [loading, setLoading] = useState(false)
  const [showAddForm, setShowAddForm] = useState(false)
  const [ruleType, setRuleType] = useState('DEPARTMENT')
  const [ruleValue, setRuleValue] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [deletingId, setDeletingId] = useState(null)
  const [error, setError] = useState(null)

  const refresh = useCallback(() => {
    if (!programmeId) {
      setRules([])
      return
    }
    setLoading(true)
    getEligibilityRules(programmeId)
      .then(setRules)
      .catch(() => setRules([]))
      .finally(() => setLoading(false))
  }, [programmeId])

  useEffect(() => {
    refresh()
    setShowAddForm(false)
  }, [refresh])

  const handleRuleTypeChange = (e) => {
    setRuleType(e.target.value)
    setRuleValue('')
  }

  const handleAddRule = async (e) => {
    e.preventDefault()
    if (!ruleValue) return
    setSubmitting(true)
    setError(null)
    try {
      await createEligibilityRule(programmeId, { ruleType, ruleValue: String(ruleValue) })
      setRuleValue('')
      setShowAddForm(false)
      refresh()
    } catch (err) {
      setError(err?.response?.data?.message || 'Something went wrong adding the rule.')
    } finally {
      setSubmitting(false)
    }
  }

  const handleDelete = async (ruleId) => {
    setDeletingId(ruleId)
    setError(null)
    try {
      await deleteEligibilityRule(programmeId, ruleId)
      refresh()
    } catch (err) {
      setError(err?.response?.data?.message || 'Something went wrong removing the rule.')
    } finally {
      setDeletingId(null)
    }
  }

  if (!programmeId) {
    return null
  }

  return (
    <div className="eligibility-panel">
      <div className="card-header">
        <h3>Eligibility Rules</h3>
        {!showAddForm && (
          <button type="button" className="secondary" onClick={() => setShowAddForm(true)}>
            + Add Rule
          </button>
        )}
      </div>

      {error && <div className="banner error">{error}</div>}

      {loading ? (
        <p className="empty-state">Loading rules…</p>
      ) : rules.length === 0 && !showAddForm ? (
        <p className="empty-state">Open to all officers — no eligibility rules set.</p>
      ) : (
        <ul className="rule-list">
          {rules.map((r) => (
            <li key={r.id} className="rule-list-item">
              <span className="rule-tag">
                {RULE_TYPE_LABELS[r.ruleType] || r.ruleType}: <strong>{r.ruleValueLabel}</strong>
              </span>
              <button
                type="button"
                className="link-danger"
                onClick={() => handleDelete(r.id)}
                disabled={deletingId === r.id}
              >
                {deletingId === r.id ? 'Removing…' : 'Remove'}
              </button>
            </li>
          ))}
        </ul>
      )}

      {showAddForm && (
        <form onSubmit={handleAddRule} className="rule-form">
          <div className="field">
            <label>Rule Type</label>
            <select value={ruleType} onChange={handleRuleTypeChange}>
              {Object.entries(RULE_TYPE_LABELS).map(([value, label]) => (
                <option key={value} value={value}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <div className="field">
            <label>{ruleType === 'DEPARTMENT' ? 'Eligible Department' : 'Value'}</label>
            {ruleType === 'DEPARTMENT' ? (
              <select value={ruleValue} onChange={(e) => setRuleValue(e.target.value)} required>
                <option value="">Select department…</option>
                {departments.map((d) => (
                  <option key={d.id} value={d.id}>
                    {d.name}
                  </option>
                ))}
              </select>
            ) : ruleType === 'GRADE' ? (
              <input
                type="text"
                placeholder="e.g. Senior Officer"
                value={ruleValue}
                onChange={(e) => setRuleValue(e.target.value)}
                required
              />
            ) : (
              <input
                type="number"
                min="0"
                placeholder={ruleType === 'COOLDOWN_MONTHS' ? 'e.g. 12' : 'e.g. 5'}
                value={ruleValue}
                onChange={(e) => setRuleValue(e.target.value)}
                required
              />
            )}
          </div>

          <div className="form-actions">
            <button type="submit" className="primary" disabled={submitting}>
              {submitting ? 'Saving…' : 'Add Rule'}
            </button>
            <button
              type="button"
              className="secondary"
              onClick={() => {
                setShowAddForm(false)
                setRuleValue('')
              }}
              disabled={submitting}
            >
              Cancel
            </button>
          </div>
        </form>
      )}
    </div>
  )
}
