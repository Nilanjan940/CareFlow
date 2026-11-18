
import { Bell, Search, UserCircle } from 'lucide-react'

function Topbar() {
  return (
    <header className="mb-8 flex flex-col justify-between gap-4 border-b border-slate-200 pb-6 sm:flex-row sm:items-center">
      <div>
        <p className="text-sm text-slate-500">
          Clinic Workspace
        </p>
        <p className="font-semibold text-slate-800">
          Administration
        </p>
      </div>

      <div className="flex items-center gap-4">
        <div className="flex items-center gap-2 rounded-xl border border-slate-200 bg-white px-3 py-2">
          <Search size={17} className="text-slate-400" />
          <input
            type="text"
            placeholder="Search..."
            className="w-32 bg-transparent text-sm outline-none placeholder:text-slate-400 sm:w-48"
          />
        </div>

        <button
          type="button"
          aria-label="Notifications"
          className="rounded-xl border border-slate-200 bg-white p-2.5 text-slate-500 hover:bg-slate-50"
        >
          <Bell size={19} />
        </button>

        <div className="flex items-center gap-2">
          <UserCircle size={34} className="text-slate-400" />
          <div className="hidden sm:block">
            <p className="text-sm font-semibold text-slate-800">
              Clinic Admin
            </p>
            <p className="text-xs text-slate-400">
              Administrator
            </p>
          </div>
        </div>
      </div>
    </header>
  )
}

export default Topbar
