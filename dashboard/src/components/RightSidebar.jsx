import React from 'react';
import PropTypes from 'prop-types';
import { PieChart, Pie, Cell, ResponsiveContainer, Legend, Tooltip } from 'recharts';
import { Smartphone, Monitor, Globe } from 'lucide-react';
import { MOCK_PIE_DATA, MOCK_SESSIONS } from '../data/mockData';

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444'];

const RightSidebar = ({ selectedUser }) => {
  if (!selectedUser) {
    return (
      <div className="flex flex-col items-center justify-center h-full text-slate-500 p-6 text-center">
        <p>Select a user to view details</p>
      </div>
    );
  }

  return (
    <div className="flex flex-col h-full bg-slate-900 overflow-y-auto custom-scrollbar">
      {/* User Header */}
      <div className="p-6 border-b border-slate-700 flex flex-col items-center">
        <img
          src={selectedUser.avatar}
          alt={selectedUser.name}
          className="w-24 h-24 rounded-full border-4 border-slate-800 shadow-lg mb-4"
        />
        <h2 className="text-xl font-bold text-slate-100">{selectedUser.name}</h2>
        <span className={`mt-1 px-3 py-1 text-xs rounded-full ${
          selectedUser.role === 'Admin' ? 'bg-purple-900 text-purple-200' :
          selectedUser.role === 'Moderator' ? 'bg-blue-900 text-blue-200' :
          'bg-slate-800 text-slate-300'
        }`}>
          {selectedUser.role}
        </span>
      </div>

      {/* Media Stats */}
      <div className="p-4 border-b border-slate-700">
        <h3 className="text-sm font-semibold text-slate-400 mb-4 uppercase tracking-wider">Media Types</h3>
        <div className="h-48 w-full">
          <ResponsiveContainer width="100%" height="100%">
            <PieChart>
              <Pie
                data={MOCK_PIE_DATA}
                cx="50%"
                cy="50%"
                innerRadius={40}
                outerRadius={60}
                paddingAngle={5}
                dataKey="value"
              >
                {MOCK_PIE_DATA.map((entry, index) => (
                  <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} stroke="none" />
                ))}
              </Pie>
              <Tooltip
                contentStyle={{ backgroundColor: '#1e293b', borderColor: '#334155', color: '#f1f5f9' }}
                itemStyle={{ color: '#f1f5f9' }}
              />
              <Legend iconSize={8} wrapperStyle={{ fontSize: '12px' }} />
            </PieChart>
          </ResponsiveContainer>
        </div>
      </div>

      {/* Sessions List */}
      <div className="flex-1 p-4">
        <h3 className="text-sm font-semibold text-slate-400 mb-4 uppercase tracking-wider">Recent Sessions</h3>
        <div className="space-y-3">
          {MOCK_SESSIONS.map((session) => (
            <div key={session.id} className="bg-slate-800 p-3 rounded-md border border-slate-700">
              <div className="flex items-center justify-between mb-1">
                <div className="flex items-center text-slate-200 font-medium text-sm">
                  {session.device.includes('Mobile') ? <Smartphone className="w-4 h-4 mr-2 text-slate-400" /> : <Monitor className="w-4 h-4 mr-2 text-slate-400" />}
                  {session.device}
                </div>
              </div>
              <div className="flex justify-between text-xs text-slate-500">
                <span>{session.ip}</span>
                <span>{session.date}</span>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

RightSidebar.propTypes = {
  selectedUser: PropTypes.object,
};

export default RightSidebar;
