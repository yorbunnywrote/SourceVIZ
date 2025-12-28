import React, { useState } from 'react';
import { UploadCloud } from 'lucide-react';
import MainLayout from './layout/MainLayout';
import LeftSidebar from './components/LeftSidebar';
import RightSidebar from './components/RightSidebar';
import CenterStage from './components/CenterStage';
import { MOCK_USERS } from './data/mockData';

function App() {
  const [hasData, setHasData] = useState(true); // Toggle this to test "Upload Mode"
  const [selectedUser, setSelectedUser] = useState(MOCK_USERS[0]);

  const handleUserSelect = (user) => {
    setSelectedUser(user);
  };

  const handleUpload = () => {
    // Simulate upload
    setTimeout(() => {
      setHasData(true);
    }, 1000);
  };

  if (!hasData) {
    return (
      <div className="h-screen w-screen bg-slate-900 text-slate-100 flex items-center justify-center">
        <div className="bg-slate-800 p-8 rounded-xl border border-slate-700 shadow-2xl text-center max-w-md w-full">
          <div className="bg-slate-900/50 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
            <UploadCloud className="w-10 h-10 text-blue-400" />
          </div>
          <h1 className="text-2xl font-bold mb-2">Upload Telegram Data</h1>
          <p className="text-slate-400 mb-8">
            Select your exported chat JSON file to generate the dashboard analytics.
          </p>
          <button
            onClick={handleUpload}
            className="w-full py-3 bg-blue-600 hover:bg-blue-500 text-white rounded-lg font-medium transition-colors shadow-lg shadow-blue-900/20"
          >
            Select File
          </button>
          <p className="mt-4 text-xs text-slate-500">Supported formats: .json</p>
        </div>
      </div>
    );
  }

  return (
    <MainLayout
      leftSidebar={<LeftSidebar />}
      rightSidebar={<RightSidebar selectedUser={selectedUser} />}
    >
      <CenterStage
        users={MOCK_USERS}
        selectedUserId={selectedUser?.id}
        onSelectUser={handleUserSelect}
      />
    </MainLayout>
  );
}

export default App;
