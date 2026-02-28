import { useState } from 'react';
import './index.css';
import { LoginPage } from './pages/LoginPage';
import { AppShell } from './layout/AppShell';
import { MenuKey } from './types';
import { renderPage } from './pages/PanelPages';
import { Toast } from './components/Toast';

export default function App() {
  const [active, setActive] = useState<MenuKey>('dashboard');
  const [msg, setMsg] = useState('');
  const [loggedIn, setLoggedIn] = useState(Boolean(localStorage.getItem('panel_jwt')));

  const notify = (s: string) => {
    setMsg(s);
    setTimeout(() => setMsg(''), 2200);
  };

  if (!loggedIn) return <LoginPage onSuccess={() => setLoggedIn(true)} />;

  return (
    <>
      <Toast message={msg} />
      <AppShell
        active={active}
        onSelect={setActive}
        onLogout={() => {
          localStorage.removeItem('panel_jwt');
          setLoggedIn(false);
        }}
      >
        {renderPage(active, notify)}
      </AppShell>
    </>
  );
}
