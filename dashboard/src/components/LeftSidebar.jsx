import React, { useState } from 'react';
import { Search, Hash, ListFilter } from 'lucide-react';
import { MOCK_SEARCH_RESULTS, MOCK_NGRAM_RESULTS } from '../data/mockData';

const LeftSidebar = () => {
  const [mode, setMode] = useState('search'); // 'search' or 'ngram'
  const [searchText, setSearchText] = useState('');
  const [nCount, setNCount] = useState(2);
  const [topK, setTopK] = useState(10);

  const results = mode === 'search' ? MOCK_SEARCH_RESULTS : MOCK_NGRAM_RESULTS;

  return (
    <div className="flex flex-col h-full bg-slate-900 border-r border-slate-700">
      {/* Header / Toggle */}
      <div className="p-4 border-b border-slate-700">
        <h2 className="text-sm font-semibold text-slate-400 mb-4 uppercase tracking-wider">Analysis Tools</h2>
        <div className="flex bg-slate-800 p-1 rounded-lg">
          <button
            onClick={() => setMode('search')}
            className={`flex-1 flex items-center justify-center py-2 text-sm font-medium rounded-md transition-all ${
              mode === 'search'
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Search className="w-4 h-4 mr-2" />
            Search
          </button>
          <button
            onClick={() => setMode('ngram')}
            className={`flex-1 flex items-center justify-center py-2 text-sm font-medium rounded-md transition-all ${
              mode === 'ngram'
                ? 'bg-blue-600 text-white shadow-md'
                : 'text-slate-400 hover:text-slate-200'
            }`}
          >
            <Hash className="w-4 h-4 mr-2" />
            N-grams
          </button>
        </div>
      </div>

      {/* Controls */}
      <div className="p-4 border-b border-slate-700 space-y-4">
        {mode === 'search' ? (
          <div>
            <label className="block text-xs font-medium text-slate-400 mb-1.5">Find Word/Phrase</label>
            <div className="relative">
              <input
                type="text"
                value={searchText}
                onChange={(e) => setSearchText(e.target.value)}
                placeholder="Enter keyword..."
                className="w-full bg-slate-800 text-sm text-slate-200 px-3 py-2 rounded-md border border-slate-700 focus:outline-none focus:ring-1 focus:ring-blue-500 placeholder-slate-500"
              />
              <Search className="absolute right-3 top-1/2 transform -translate-y-1/2 text-slate-500 w-4 h-4" />
            </div>
          </div>
        ) : (
          <div className="space-y-4">
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5">N-Count</label>
              <input
                type="number"
                value={nCount}
                onChange={(e) => setNCount(parseInt(e.target.value) || 2)}
                min="1"
                max="5"
                className="w-full bg-slate-800 text-sm text-slate-200 px-3 py-2 rounded-md border border-slate-700 focus:outline-none focus:ring-1 focus:ring-blue-500 placeholder-slate-500"
              />
            </div>
            <div>
              <label className="block text-xs font-medium text-slate-400 mb-1.5">Top K ({topK})</label>
              <input
                type="range"
                min="5"
                max="50"
                value={topK}
                onChange={(e) => setTopK(parseInt(e.target.value))}
                className="w-full h-2 bg-slate-700 rounded-lg appearance-none cursor-pointer accent-blue-500"
              />
            </div>
          </div>
        )}
      </div>

      {/* Results Area */}
      <div className="flex-1 overflow-y-auto custom-scrollbar p-0">
        <div className="p-4 pb-2 sticky top-0 bg-slate-900/95 backdrop-blur-sm z-10 border-b border-slate-800">
           <h3 className="text-xs font-semibold text-slate-500 uppercase flex items-center">
             <ListFilter className="w-3 h-3 mr-1.5" />
             Results ({results.length})
           </h3>
        </div>
        <div className="p-2 space-y-1">
          {results.map((item) => (
            <div key={item.id} className="group flex items-center justify-between p-2 hover:bg-slate-800 rounded-md cursor-pointer transition-colors border border-transparent hover:border-slate-700">
              <span className="text-sm text-slate-300 group-hover:text-blue-400 font-medium truncate pr-2">
                {item.text}
              </span>
              <span className="text-xs px-2 py-0.5 bg-slate-800 group-hover:bg-slate-700 text-slate-400 rounded-full border border-slate-700">
                {item.count}
              </span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default LeftSidebar;
