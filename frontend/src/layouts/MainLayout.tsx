import Topbar from '../components/Topbar'
import {
  Activity,
  LayoutDashboard,
  Users,
  CalendarDays,
  Stethoscope,
  Settings,
  LogOut,
} from 'lucide-react'
import { NavLink, Outlet } from 'react-router-dom'

const navigation = [
  { name: 'Dashboard', path: '/', icon: LayoutDashboard },
  { name: 'Patients', path: '/patients', icon: Users },
  { name: 'Appointments', path: '/appointments', icon: CalendarDays },
  { name: 'Doctors', path: '/doctors', icon: Stethoscope },
]

function MainLayout() {
  return (
    <div className="min-h-screen bg-slate-50 md:flex">
      <aside className="flex w-full flex-col border-r border-slate-200 bg-white md:fixed md:inset-y-0 md:w-64">
        <div className="flex h-20 items-center gap-3 border-b border-slate-100 px-6">
          <div className="flex h-10 w-10 items-center justify-center rounded-xl bg-blue-600 text-white">
            <Activity size={23} />
          </div>
          <div>
            <h1 className="text-xl font-bold text-slate-900">CareFlow</h1>
            <p className="text-xs text-slate-400">Clinic Management</p>
          </div>
        </div>

        <nav className="flex-1 space-y-2 p-4">
          <p className="mb-4 px-3 text-xs font-semibold uppercase tracking-wider text-slate-400">
            Workspace
          </p>

          {navigation.map((item) => {
            const Icon = item.icon

            return (
              <NavLink
                key={item.path}
                to={item.path}
                end={item.path === '/'}
                className={({ isActive }) =>
                  `flex items-center gap-3 rounded-xl px-3 py-3 text-sm font-medium transition ${
                    isActive
                      ? 'bg-blue-50 text-blue-700'
                      : 'text-slate-500 hover:bg-slate-50 hover:text-slate-900'
                  }`
                }
              >
                <Icon size={19} />
                {item.name}
              </NavLink>
            )
          })}
        </nav>

        <div className="space-y-2 border-t border-slate-100 p-4">
          <button className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm text-slate-500 hover:bg-slate-50">
            <Settings size={19} />
            Settings
          </button>
          <button className="flex w-full items-center gap-3 rounded-xl px-3 py-3 text-sm text-slate-500 hover:bg-slate-50">
            <LogOut size={19} />
            Sign out
          </button>
        </div>
      </aside>

      <main className="min-w-0 flex-1 p-6 md:ml-64 md:p-10">
        <Topbar />
        <Outlet />
      </main>
    </div>
  )
}

export default MainLayout
