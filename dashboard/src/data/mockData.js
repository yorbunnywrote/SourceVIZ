// Mock data for the dashboard

export const MOCK_USERS = [
  { id: 1, name: "Alice Johnson", avatar: "https://i.pravatar.cc/150?u=1", role: "Admin", msgs: 12500, words: 85000, percentageMsgs: 15.2, percentageWords: 18.5 },
  { id: 2, name: "Bob Smith", avatar: "https://i.pravatar.cc/150?u=2", role: "User", msgs: 8200, words: 54000, percentageMsgs: 10.1, percentageWords: 11.8 },
  { id: 3, name: "Charlie Brown", avatar: "https://i.pravatar.cc/150?u=3", role: "Moderator", msgs: 6000, words: 42000, percentageMsgs: 7.3, percentageWords: 9.1 },
  { id: 4, name: "Diana Prince", avatar: "https://i.pravatar.cc/150?u=4", role: "User", msgs: 4500, words: 28000, percentageMsgs: 5.5, percentageWords: 6.1 },
  { id: 5, name: "Evan Wright", avatar: "https://i.pravatar.cc/150?u=5", role: "User", msgs: 8800, words: 61000, percentageMsgs: 10.8, percentageWords: 13.2 },
  { id: 6, name: "Fiona Gallagher", avatar: "https://i.pravatar.cc/150?u=6", role: "User", msgs: 3000, words: 15000, percentageMsgs: 3.6, percentageWords: 3.3 },
  { id: 7, name: "George Martin", avatar: "https://i.pravatar.cc/150?u=7", role: "User", msgs: 7500, words: 52000, percentageMsgs: 9.1, percentageWords: 11.3 },
  { id: 8, name: "Hannah Lee", avatar: "https://i.pravatar.cc/150?u=8", role: "User", msgs: 5500, words: 38000, percentageMsgs: 6.7, percentageWords: 8.2 },
];

export const MOCK_CHART_DATA = [
  { name: 'Jan', messages: 4000, active: 2400 },
  { name: 'Feb', messages: 3000, active: 1398 },
  { name: 'Mar', messages: 2000, active: 9800 },
  { name: 'Apr', messages: 2780, active: 3908 },
  { name: 'May', messages: 1890, active: 4800 },
  { name: 'Jun', messages: 2390, active: 3800 },
  { name: 'Jul', messages: 3490, active: 4300 },
];

export const MOCK_PIE_DATA = [
  { name: 'Images', value: 400 },
  { name: 'Videos', value: 300 },
  { name: 'Docs', value: 300 },
  { name: 'Links', value: 200 },
];

export const MOCK_SESSIONS = [
  { id: 1, date: "2023-10-27 10:00", device: "Desktop (Windows)", ip: "192.168.1.1" },
  { id: 2, date: "2023-10-26 14:30", device: "Mobile (iOS)", ip: "192.168.1.5" },
  { id: 3, date: "2023-10-25 09:15", device: "Desktop (Mac)", ip: "192.168.1.2" },
  { id: 4, date: "2023-10-24 18:20", device: "Mobile (Android)", ip: "10.0.0.4" },
];

export const MOCK_KPI_DATA = [
  { label: "Total Messages", value: "1.2M", change: "+12%" },
  { label: "Active Users", value: "3.4K", change: "+5%" },
  { label: "Media Shared", value: "450GB", change: "+8%" },
  { label: "Avg Response", value: "2m 30s", change: "-10%" },
];

export const MOCK_SEARCH_RESULTS = [
  { id: 1, text: "crypto", count: 145 },
  { id: 2, text: "bitcoin", count: 98 },
  { id: 3, text: "meeting", count: 76 },
  { id: 4, text: "project", count: 65 },
  { id: 5, text: "deadline", count: 54 },
  { id: 6, text: "update", count: 43 },
  { id: 7, text: "bug", count: 32 },
  { id: 8, text: "feature", count: 21 },
];

export const MOCK_NGRAM_RESULTS = [
  { id: 1, text: "good morning", count: 120 },
  { id: 2, text: "how are you", count: 110 },
  { id: 3, text: "please check", count: 95 },
  { id: 4, text: "let me know", count: 85 },
  { id: 5, text: "thanks a lot", count: 75 },
  { id: 6, text: "sounds good", count: 65 },
  { id: 7, text: "will do", count: 55 },
];
