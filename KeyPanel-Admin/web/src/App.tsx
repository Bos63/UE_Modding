import { useState } from 'react';
import './index.css';
import api from './api/client';
import { Toast } from './components/Toast';

type KeyRow = { key: string; date: string };

async function loginWithAdminToken(adminToken: string) {
  const { data } = await api.post('/auth/login', { token: adminToken });
  localStorage.setItem('panel_jwt', data.accessToken);
}

export default function App() {
  const [adminToken, setAdminToken] = useState('');
  const [type, setType] = useState<'hourly' | 'daily' | 'weekly' | 'monthly'>('daily');
  const [mode, setMode] = useState<'random' | 'custom'>('random');
  const [customKey, setCustomKey] = useState('');
  const [items, setItems] = useState<KeyRow[]>([]);
  const [toast, setToast] = useState('');
  const [loading, setLoading] = useState(false);

  const notify = (m: string) => {
    setToast(m);
    setTimeout(() => setToast(''), 2000);
  };

  const requireLogin = async () => {
    if (!adminToken.trim()) {
      notify('Admin token gerekli');
      throw new Error('missing token');
    }
    await loginWithAdminToken(adminToken.trim());
  };

  const createKey = async () => {
    try {
      setLoading(true);
      await requireLogin();
      const { data } = await api.post('/keys', {
        type,
        mode,
        customName: mode === 'custom' ? customKey : undefined
      });
      if (data?.key) {
        await navigator.clipboard?.writeText(data.key);
        notify(`Key oluşturuldu: ${data.key}`);
      } else {
        notify('Key oluşturuldu');
      }
      await refreshList();
    } catch {
      notify('İşlem başarısız (token veya bağlantı hatası).');
    } finally {
      setLoading(false);
    }
  };

  const refreshList = async () => {
    try {
      setLoading(true);
      await requireLogin();
      const { data } = await api.get('/keys', { params: { type: '', q: '', page: 1 } });
      setItems((data?.items || []).map((x: any) => ({ key: x.key, date: x.date })));
    } catch {
      notify('Liste alınamadı.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#050b2a] text-white p-5">
      <Toast message={toast} />

      <h1 className="text-4xl font-semibold mb-5">UAssetGUI Key Panel</h1>

      <div className="panel-card">
        <input
          className="panel-input"
          placeholder="Admin token"
          value={adminToken}
          onChange={(e) => setAdminToken(e.target.value)}
        />

        <div className="grid md:grid-cols-2 gap-4 mt-3">
          <select className="panel-input" value={mode} onChange={(e) => setMode(e.target.value as 'random' | 'custom')}>
            <option value="random">Random Key</option>
            <option value="custom">Özel Key</option>
          </select>
          <input
            className="panel-input"
            placeholder="Özel key (örn: URAZ-VIP-001)"
            value={customKey}
            onChange={(e) => setCustomKey(e.target.value)}
            disabled={mode !== 'custom'}
          />
        </div>

        <select className="panel-input mt-3 md:w-1/2" value={type} onChange={(e) => setType(e.target.value as any)}>
          <option value="hourly">Saatlik</option>
          <option value="daily">Günlük</option>
          <option value="weekly">Haftalık</option>
          <option value="monthly">Aylık</option>
        </select>

        <button className="panel-button mt-3" onClick={createKey} disabled={loading}>
          {loading ? 'İşleniyor...' : 'Key Oluştur'}
        </button>

        <p className="text-slate-300 text-sm mt-4">Panel link (mobil için): https://urazpanel/uassetvip.com/</p>
      </div>

      <div className="panel-card mt-4">
        <button className="panel-button" onClick={refreshList} disabled={loading}>
          Listeyi Yenile
        </button>

        <div className="mt-4 space-y-2">
          {items.map((it) => (
            <div key={it.key} className="bg-white/5 rounded-xl p-2 flex justify-between text-sm">
              <span>{it.key}</span>
              <span>{it.date}</span>
            </div>
          ))}
          {items.length === 0 && <div className="text-slate-400 text-sm">Henüz key yok.</div>}
        </div>
      </div>
    </div>
  );
}
