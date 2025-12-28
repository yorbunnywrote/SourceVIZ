import React from 'react';
import PropTypes from 'prop-types';
import { AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { MessageCircle, Users, Image, Clock, MoreHorizontal } from 'lucide-react';
import { MOCK_CHART_DATA, MOCK_KPI_DATA, MOCK_USERS } from '../data/mockData';

const CenterStage = ({ users, selectedUserId, onSelectUser }) => {
  return (
    <div className="flex flex-col h-full overflow-hidden">
      {/* Header with KPI Cards */}
      <div className="p-6 pb-2">
        <h1 className="text-2xl font-bold mb-6">Overview</h1>
        <div className="grid grid-cols-4 gap-4 mb-6">
          {MOCK_KPI_DATA.map((kpi, index) => (
            <div key={index} className="bg-slate-800 p-4 rounded-lg border border-slate-700 shadow-sm">
              <div className="flex justify-between items-start mb-2">
                <span className="text-slate-400 text-sm font-medium">{kpi.label}</span>
                <span className={`text-xs px-1.5 py-0.5 rounded ${kpi.change.startsWith('+') ? 'bg-emerald-900 text-emerald-300' : 'bg-rose-900 text-rose-300'}`}>
                  {kpi.change}
                </span>
              </div>
              <div className="text-2xl font-bold text-slate-100">{kpi.value}</div>
            </div>
          ))}
        </div>
      </div>

      {/* Main Chart */}
      <div className="flex-shrink-0 px-6 pb-6 h-64">
        <div className="bg-slate-800 p-4 rounded-lg border border-slate-700 h-full w-full">
          <h3 className="text-sm font-semibold text-slate-400 mb-4">Message Activity Volume</h3>
          <div className="h-[calc(100%-2rem)] w-full">
            <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={MOCK_CHART_DATA}>
                <defs>
                  <linearGradient id="colorMessages" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                  <linearGradient id="colorActive" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#10b981" stopOpacity={0.3}/>
                    <stop offset="95%" stopColor="#10b981" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" stroke="#334155" vertical={false} />
                <XAxis dataKey="name" stroke="#94a3b8" tick={{fontSize: 12}} tickLine={false} axisLine={false} />
                <YAxis stroke="#94a3b8" tick={{fontSize: 12}} tickLine={false} axisLine={false} />
                <Tooltip
                  contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', color: '#f1f5f9' }}
                  itemStyle={{ color: '#f1f5f9' }}
                />
                <Area type="monotone" dataKey="messages" stroke="#3b82f6" fillOpacity={1} fill="url(#colorMessages)" strokeWidth={2} />
                <Area type="monotone" dataKey="active" stroke="#10b981" fillOpacity={1} fill="url(#colorActive)" strokeWidth={2} />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>
      </div>

      {/* Bottom Split Section */}
      <div className="flex-1 px-6 pb-6 min-h-0 overflow-hidden">
        <div className="grid grid-cols-3 gap-6 h-full">

          {/* Users Table (Col Span 2) */}
          <div className="col-span-2 bg-slate-800 rounded-lg border border-slate-700 flex flex-col h-full overflow-hidden">
            <div className="p-4 border-b border-slate-700 flex justify-between items-center">
              <h3 className="font-semibold text-slate-200">Active Users</h3>
              <button className="text-slate-400 hover:text-slate-200"><MoreHorizontal className="w-5 h-5" /></button>
            </div>
            <div className="overflow-y-auto flex-1 p-0">
              <table className="w-full text-left border-collapse">
                <thead className="bg-slate-900/50 text-slate-400 text-xs uppercase sticky top-0 z-10">
                  <tr>
                    <th className="px-4 py-3 font-medium">User</th>
                    <th className="px-4 py-3 font-medium">Role</th>
                    <th className="px-4 py-3 font-medium">Activity Score</th>
                    <th className="px-4 py-3 font-medium">Sentiment</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-slate-700">
                  {users.map((user) => (
                    <tr
                      key={user.id}
                      onClick={() => onSelectUser(user)}
                      className={`cursor-pointer transition-colors ${selectedUserId === user.id ? 'bg-blue-900/20' : 'hover:bg-slate-700/30'}`}
                    >
                      <td className="px-4 py-3">
                        <div className="flex items-center">
                          <img src={user.avatar} alt="" className="w-8 h-8 rounded-full mr-3" />
                          <span className={`font-medium ${selectedUserId === user.id ? 'text-blue-400' : 'text-slate-200'}`}>
                            {user.name}
                          </span>
                        </div>
                      </td>
                      <td className="px-4 py-3 text-slate-400 text-sm">{user.role}</td>
                      <td className="px-4 py-3">
                        <div className="flex items-center">
                          <div className="w-24 h-1.5 bg-slate-700 rounded-full mr-2 overflow-hidden">
                            <div
                              className="h-full bg-blue-500 rounded-full"
                              style={{ width: `${user.activity}%` }}
                            ></div>
                          </div>
                          <span className="text-xs text-slate-400">{user.activity}%</span>
                        </div>
                      </td>
                      <td className="px-4 py-3">
                        <span className={`text-xs px-2 py-1 rounded-full ${
                          user.sentiment === 'Positive' ? 'bg-emerald-900/50 text-emerald-400' :
                          user.sentiment === 'Negative' ? 'bg-rose-900/50 text-rose-400' :
                          'bg-slate-700 text-slate-300'
                        }`}>
                          {user.sentiment}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>

          {/* Heatmap (Col Span 1) */}
          <div className="col-span-1 bg-slate-800 rounded-lg border border-slate-700 flex flex-col h-full overflow-hidden">
            <div className="p-4 border-b border-slate-700">
              <h3 className="font-semibold text-slate-200">Activity Heatmap</h3>
            </div>
            <div className="flex-1 p-4 flex items-center justify-center">
              {/* Simplified Heatmap Visual */}
              <div className="grid grid-cols-7 gap-1 w-full h-full content-center">
                {Array.from({ length: 42 }).map((_, i) => {
                   // Generate random intensity for demo
                   const intensity = Math.random();
                   let bgClass = 'bg-slate-700';
                   if (intensity > 0.8) bgClass = 'bg-emerald-400';
                   else if (intensity > 0.6) bgClass = 'bg-emerald-600';
                   else if (intensity > 0.4) bgClass = 'bg-emerald-800';
                   else if (intensity > 0.2) bgClass = 'bg-slate-600';

                   return (
                     <div key={i} className={`${bgClass} rounded-sm w-full h-8 opacity-80 hover:opacity-100 transition-opacity cursor-pointer`} title={`Day ${i+1}`}></div>
                   );
                })}
              </div>
            </div>
            <div className="p-3 bg-slate-900/30 text-xs text-slate-500 text-center">
              Last 6 Weeks Activity
            </div>
          </div>

        </div>
      </div>
    </div>
  );
};

CenterStage.propTypes = {
  users: PropTypes.array.isRequired,
  selectedUserId: PropTypes.number,
  onSelectUser: PropTypes.func.isRequired,
};

export default CenterStage;
