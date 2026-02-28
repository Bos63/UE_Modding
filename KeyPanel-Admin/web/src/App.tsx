import { useMemo, useState } from 'react';
import './index.css';
import api from './api/client';
import { Toast } from './components/Toast';

type KeyType = 'hourly' | 'daily' | 'weekly' | 'monthly';
type KeyRow = { key: string; date: string; type?: KeyType };

async function loginWithAdminToken(adminToken: string) {
  const { data } = await api.post('/auth/login', { token: adminToken });
  localStorage.setItem('panel_jwt', data.accessToken);
}

export default function App() {
  const [adminToken, setAdminToken] = useState('');
  const [type, setType] = useState<KeyType>('daily');
  const [mode, setMode] = useState<'random' | 'custom'>('random');
  const [customKey, setCustomKey] = useState('');
  const [items, setItems] = useState<KeyRow[]>([]);
  const [toast, setToast] = useState('');
  const [loading, setLoading] = useState(false);
  const [query, setQuery] = useState('');
  const [filterType, setFilterType] = useState<'all' | KeyType>('all');

  const notify = (m: string) => {
    setToast(m);
    setTimeout(() => setToast(''), 2200);
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
        notify(`Key oluşturuldu ve kopyalandı: ${data.key}`);
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
      setItems((data?.items || []).map((x: any) => ({ key: x.key, date: x.date, type: x.type })));
      notify('Liste güncellendi');
    } catch {
      notify('Liste alınamadı.');
    } finally {
      setLoading(false);
    }
  };

  const deleteKey = async (key: string) => {
    try {
      setLoading(true);
      await requireLogin();
      await api.delete(`/keys/${encodeURIComponent(key)}`);
      setItems((prev) => prev.filter((x) => x.key !== key));
      notify(`Key silindi: ${key}`);
    } catch {
      notify('Key silinemedi.');
    } finally {
      setLoading(false);
    }
  };

  const copyAll = async () => {
    const text = filteredItems.map((it) => `${it.key} | ${it.date}`).join('\n');
    if (!text) return notify('Kopyalanacak key yok.');
    await navigator.clipboard?.writeText(text);
    notify('Liste panoya kopyalandı');
  };

  const filteredItems = useMemo(() => {
    return items.filter((it) => {
      const queryOk = query.trim() ? it.key.toLowerCase().includes(query.trim().toLowerCase()) : true;
      const typeOk = filterType === 'all' ? true : it.type === filterType;
      return queryOk && typeOk;
    });
  }, [items, query, filterType]);

  const stats = useMemo(() => {
    const total = items.length;
    const daily = items.filter((x) => x.type === 'daily').length;
    const weekly = items.filter((x) => x.type === 'weekly').length;
    return { total, daily, weekly };
  }, [items]);

  return (
    <div className="min-h-screen bg-[#050b2a] text-white p-5">
      <Toast message={toast} />

      <h1 className="text-4xl font-semibold mb-5">UAssetGUI Key Panel</h1>

      <div className="grid md:grid-cols-3 gap-3 mb-4">
        <div className="panel-mini">Toplam Key: <b>{stats.total}</b></div>
        <div className="panel-mini">Günlük: <b>{stats.daily}</b></div>
        <div className="panel-mini">Haftalık: <b>{stats.weekly}</b></div>
      </div>

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

        <select className="panel-input mt-3 md:w-1/2" value={type} onChange={(e) => setType(e.target.value as KeyType)}>
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
        <div className="grid md:grid-cols-[1fr_220px_220px_auto_auto] gap-2 items-center">
          <input className="panel-input" placeholder="Key ara" value={query} onChange={(e) => setQuery(e.target.value)} />
          <select className="panel-input" value={filterType} onChange={(e) => setFilterType(e.target.value as any)}>
            <option value="all">Tüm Türler</option>
            <option value="hourly">Saatlik</option>
            <option value="daily">Günlük</option>
            <option value="weekly">Haftalık</option>
            <option value="monthly">Aylık</option>
          </select>
          <button className="panel-button" onClick={refreshList} disabled={loading}>Listeyi Yenile</button>
          <button className="panel-button-outline" onClick={copyAll}>Hepsini Kopyala</button>
        </div>

        <div className="mt-4 space-y-2">
          {filteredItems.map((it) => (
            <div key={it.key} className="bg-white/5 rounded-xl p-2 flex items-center justify-between text-sm gap-2">
              <div className="min-w-0">
                <div className="truncate font-medium">{it.key}</div>
                <div className="text-slate-300">{it.date}</div>
              </div>
              <button className="panel-delete" onClick={() => deleteKey(it.key)} disabled={loading}>Sil</button>
            </div>
          ))}
          {filteredItems.length === 0 && <div className="text-slate-400 text-sm">Filtreye uygun key yok.</div>}
        </div>
      </div>
    </div>
  );
}
