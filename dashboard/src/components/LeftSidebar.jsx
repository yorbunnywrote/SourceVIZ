import React, { useState } from 'react';
import { Search, MessageSquare, Users, Settings, HelpCircle, BarChart2 } from 'lucide-react';

const LeftSidebar = () => {
  const [activeItem, setActiveItem] = useState('dashboard');

  const navItems = [
    { id: 'dashboard', icon: BarChart2, label: 'Dashboard' },
    { id: 'chats', icon: MessageSquare, label: 'Chats' },
    { id: 'users', icon: Users, label: 'Users' },
    { id: 'settings', icon: Settings, label: 'Settings' },
  ];

  return (
    <div className="flex flex-col h-full bg-slate-900">
      {/* Search Header */}
      <div className="p-4 border-b border-slate-700">
        <div className="relative">
          <Search className="absolute left-3 top-1/2 transform -translate-y-1/2 text-slate-400 w-4 h-4" />
          <input
            type="text"
            placeholder="Search..."
            className="w-full bg-slate-800 text-sm text-slate-200 pl-10 pr-4 py-2 rounded-md focus:outline-none focus:ring-1 focus:ring-blue-500 placeholder-slate-500 border border-slate-700"
          />
        </div>
      </div>

      {/* Navigation */}
      <nav className="flex-1 overflow-y-auto py-4">
        <ul className="space-y-1 px-2">
          {navItems.map((item) => (
            <li key={item.id}>
              <button
                onClick={() => setActiveItem(item.id)}
                className={`w-full flex items-center px-3 py-2 text-sm font-medium rounded-md transition-colors ${
                  activeItem === item.id
                    ? 'bg-slate-800 text-blue-400'
                    : 'text-slate-400 hover:bg-slate-800 hover:text-slate-200'
                }`}
              >
                <item.icon className="w-5 h-5 mr-3" />
                {item.label}
              </button>
            </li>
          ))}
        </ul>
      </nav>

      {/* Footer / Help */}
      <div className="p-4 border-t border-slate-700">
        <button className="flex items-center text-sm text-slate-400 hover:text-slate-200 transition-colors">
          <HelpCircle className="w-5 h-5 mr-3" />
          Help & Support
        </button>
      </div>
    </div>
  );
};

export default LeftSidebar;
