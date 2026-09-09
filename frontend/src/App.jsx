import { HashRouter, Routes, Route } from 'react-router-dom'
import NavBar from './components/NavBar'
import NominatePage from './pages/NominatePage'
import AllParticipantsPage from './pages/AllParticipantsPage'

// HashRouter (URLs like /#/participants) needs no server-side rewrite rules
// to work — important since the built frontend can be served as plain
// static files (see docs/SCALE.md) with no routing configured on the host.
export default function App() {
  return (
    <HashRouter>
      <div className="app">
        <header className="page-header">
          <h1>Government Training Management System</h1>
          <p>Nominate officers for training programmes and track participation across the office.</p>
        </header>

        <NavBar />

        <Routes>
          <Route path="/" element={<NominatePage />} />
          <Route path="/participants" element={<AllParticipantsPage />} />
        </Routes>
      </div>
    </HashRouter>
  )
}
