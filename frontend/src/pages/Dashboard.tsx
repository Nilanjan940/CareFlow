
import { CalendarDays, Users, Stethoscope, Clock } from 'lucide-react'

const stats = [
  {
    title: 'Total Patients',
    value: '1,248',
    description: 'Registered patients',
    icon: Users,
  },
  {
    title: "Today's Appointments",
    value: '32',
    description: 'Scheduled for today',
    icon: CalendarDays,
  },
  {
    title: 'Available Doctors',
    value: '08',
    description: 'Currently registered',
    icon: Stethoscope,
  },
  {
    title: 'Waiting Patients',
    value: '06',
    description: 'Currently in queue',
    icon: Clock,
  },
]

function Dashboard() {
  return (
    <div className="space-y-8">
      <div>
        <p className="text-sm font-medium text-blue-600">Overview</p>
        <h1 className="mt-1 text-3xl font-bold text-slate-900">
          Dashboard
        </h1>
        <p className="mt-2 text-slate-500">
          Welcome to your clinic management workspace.
        </p>
      </div>

      <div className="grid gap-5 sm:grid-cols-2 xl:grid-cols-4">
        {stats.map((stat) => {
          const Icon = stat.icon

          return (
            <div
              key={stat.title}
              className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm"
            >
              <div className="flex items-center justify-between">
                <p className="text-sm font-medium text-slate-500">
                  {stat.title}
                </p>
                <div className="rounded-xl bg-blue-50 p-3 text-blue-600">
                  <Icon size={20} />
                </div>
              </div>

              <p className="mt-5 text-3xl font-bold text-slate-900">
                {stat.value}
              </p>
              <p className="mt-1 text-sm text-slate-400">
                {stat.description}
              </p>
            </div>
          )
        })}
      </div>

      <div className="rounded-2xl border border-slate-200 bg-white p-6 shadow-sm">
        <h2 className="text-lg font-semibold text-slate-900">
          Getting started
        </h2>
        <p className="mt-2 text-sm text-slate-500">
          Your clinic overview will appear here. Appointment management,
          patient records and doctor schedules are coming next.
        </p>
      </div>
    </div>
  )
}

export default Dashboard
