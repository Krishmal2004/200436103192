import { NavLink } from 'react-router-dom'

export default function NavBar() {
  return (
    <nav className="navbar">
      <NavLink to="/" end className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
        Nominate
      </NavLink>
      <NavLink to="/participants" className={({ isActive }) => (isActive ? 'nav-link active' : 'nav-link')}>
        All Participants
      </NavLink>
    </nav>
  )
}
