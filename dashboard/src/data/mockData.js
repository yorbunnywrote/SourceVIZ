// Mock data for the dashboard

export const MOCK_USERS = [
  { id: 1, name: "Alice Johnson", avatar: "https://i.pravatar.cc/150?u=1", role: "Admin", activity: 95, sentiment: "Positive" },
  { id: 2, name: "Bob Smith", avatar: "https://i.pravatar.cc/150?u=2", role: "User", activity: 82, sentiment: "Neutral" },
  { id: 3, name: "Charlie Brown", avatar: "https://i.pravatar.cc/150?u=3", role: "Moderator", activity: 60, sentiment: "Negative" },
  { id: 4, name: "Diana Prince", avatar: "https://i.pravatar.cc/150?u=4", role: "User", activity: 45, sentiment: "Positive" },
  { id: 5, name: "Evan Wright", avatar: "https://i.pravatar.cc/150?u=5", role: "User", activity: 88, sentiment: "Neutral" },
  { id: 6, name: "Fiona Gallagher", avatar: "https://i.pravatar.cc/150?u=6", role: "User", activity: 30, sentiment: "Negative" },
  { id: 7, name: "George Martin", avatar: "https://i.pravatar.cc/150?u=7", role: "User", activity: 75, sentiment: "Positive" },
  { id: 8, name: "Hannah Lee", avatar: "https://i.pravatar.cc/150?u=8", role: "User", activity: 55, sentiment: "Neutral" },
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
