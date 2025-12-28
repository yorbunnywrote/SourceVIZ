import React from 'react';
import PropTypes from 'prop-types';

const MainLayout = ({ leftSidebar, rightSidebar, children }) => {
  return (
    <div className="flex h-screen w-screen bg-slate-900 text-slate-100 overflow-hidden">
      {/* Left Sidebar */}
      <aside className="w-72 flex-shrink-0 border-r border-slate-700 flex flex-col">
        {leftSidebar}
      </aside>

      {/* Center Stage */}
      <main className="flex-1 min-w-0 flex flex-col h-full bg-slate-900">
        {children}
      </main>

      {/* Right Sidebar */}
      <aside className="w-80 flex-shrink-0 border-l border-slate-700 flex flex-col">
        {rightSidebar}
      </aside>
    </div>
  );
};

MainLayout.propTypes = {
  leftSidebar: PropTypes.node,
  rightSidebar: PropTypes.node,
  children: PropTypes.node,
};

export default MainLayout;
