import { useEffect, useState } from 'react';
import api from '../api/client';
import { Skeleton } from '../components/Skeleton';
import { KeyType, MenuKey } from '../types';

export function Dashboard() {
  return <div className="glass p-4">Premium dashboard hazır. Sol menüden yönetim ekranlarını kullanın.</div>;
}

export function KeyCreate({ notify }: { notify: (s: string) => void }) {
  const [type, setType] = useState<KeyType>('daily');
  const [mode, setMode] = useState<'random' | 'custom'>('random');
  const [customName, setCustomName] = useState('URAZ-VIP-001');

  const create = async () => {
    const { data } = await api.post('/keys', { type, mode, customName });
    navigator.clipboard?.writeText(data.key);
    notify(`Key oluşturuldu: ${data.key} (kopyalandı)`);
  };

  return (
    <div className="glass p-4 space-y-3">
      <h2 className="text-neon font-semibold">Key Oluştur</h2>
      <select className="w-full bg-white/10 rounded-xl p-3" value={type} onChange={(e) => setType(e.target.value as KeyType)}>
        <option value="hourly">Saatlik</option><option value="daily">Günlük</option><option value="weekly">Haftalık</option><option value="monthly">Aylık</option>
      </select>
      <select className="w-full bg-white/10 rounded-xl p-3" value={mode} onChange={(e) => setMode(e.target.value as any)}>
        <option value="random">Random Key</option><option value="custom">Özel İsimli Key</option>
      </select>
      {mode === 'custom' && <input className="w-full bg-white/10 rounded-xl p-3" value={customName} onChange={(e)=>setCustomName(e.target.value)} />}
      <button className="rounded-xl px-4 py-3 bg-neon/30" onClick={create}>Key Oluştur</button>
    </div>
  );
}

export function KeyList() {
  const [items, setItems] = useState<any[]>([]);
  const [q, setQ] = useState('');
  const [type, setType] = useState('');
  const [page, setPage] = useState(1);
  const [totalPages, setTotalPages] = useState(1);
  const [loading, setLoading] = useState(true);

  const load = async () => {
    setLoading(true);
    const { data } = await api.get('/keys', { params: { q, type, page } });
    setItems(data.items || []);
    setTotalPages(data.totalPages || 1);
    setLoading(false);
  };

  useEffect(() => { load(); }, [q, type, page]);

  return (
    <div className="glass p-4">
      <h2 className="text-neon font-semibold mb-3">Key Listesi</h2>
      <div className="grid md:grid-cols-3 gap-2 mb-3">
        <input className="bg-white/10 rounded-xl p-2" placeholder="Ara" value={q} onChange={(e)=>setQ(e.target.value)} />
        <select className="bg-white/10 rounded-xl p-2" value={type} onChange={(e)=>setType(e.target.value)}>
          <option value="">Tümü</option><option value="hourly">Saatlik</option><option value="daily">Günlük</option><option value="weekly">Haftalık</option><option value="monthly">Aylık</option>
        </select>
      </div>
      {loading ? <Skeleton /> : (
        <>
          {items.length === 0 ? <div className="text-gray-300">Kayıt yok.</div> : (
            <div className="overflow-auto rounded-xl border border-white/10">
              <table className="w-full text-sm">
                <thead><tr className="bg-white/10"><th className="text-left p-2">KEY</th><th className="text-left p-2">TARİH (GÜN)</th></tr></thead>
                <tbody>{items.map((x) => <tr key={x.key} className="border-t border-white/10"><td className="p-2">{x.key}</td><td className="p-2">{x.date}</td></tr>)}</tbody>
              </table>
            </div>
          )}
          <div className="flex gap-2 mt-3">
            <button className="px-3 py-2 bg-white/10 rounded-xl" onClick={()=>setPage((p)=>Math.max(1,p-1))}>Önceki</button>
            <div className="px-3 py-2">{page}/{totalPages}</div>
            <button className="px-3 py-2 bg-white/10 rounded-xl" onClick={()=>setPage((p)=>Math.min(totalPages,p+1))}>Sonraki</button>
          </div>
        </>
      )}
    </div>
  );
}

export function AdminsPage() {
  const [items, setItems] = useState<any[]>([]);
  const [username, setUsername] = useState('');
  const [token, setToken] = useState('');
  const [role, setRole] = useState('Admin');
  const load = async () => setItems((await api.get('/admins')).data);
  useEffect(() => { load(); }, []);
  return <div className="glass p-4 space-y-3"><h2 className="text-neon">Yöneticiler</h2>
    <div className="grid md:grid-cols-4 gap-2"><input className="bg-white/10 p-2 rounded-xl" placeholder="username" value={username} onChange={e=>setUsername(e.target.value)}/><input className="bg-white/10 p-2 rounded-xl" placeholder="token" value={token} onChange={e=>setToken(e.target.value)}/><select className="bg-white/10 p-2 rounded-xl" value={role} onChange={e=>setRole(e.target.value)}><option>SuperAdmin</option><option>Admin</option><option>Viewer</option></select><button className="bg-neon/30 rounded-xl" onClick={async()=>{await api.post('/admins',{username,token,role});setUsername('');setToken('');load();}}>Ekle</button></div>
    <ul className="space-y-2">{items.map(x=><li key={x._id} className="bg-white/5 rounded-xl p-2 flex justify-between">{x.username} - {x.role}<button className="text-red-300" onClick={async()=>{await api.delete(`/admins/${x._id}`);load();}}>Sil</button></li>)}</ul>
  </div>;
}

export function UsersPage() { const [items,setItems]=useState<any[]>([]); const [name,setName]=useState(''); const load=async()=>setItems((await api.get('/users')).data); useEffect(()=>{load();},[]);
return <div className="glass p-4 space-y-3"><h2 className="text-neon">Kullanıcılar</h2><div className="flex gap-2"><input className="bg-white/10 p-2 rounded-xl flex-1" placeholder="Ad" value={name} onChange={e=>setName(e.target.value)}/><button className="bg-neon/30 rounded-xl px-4" onClick={async()=>{await api.post('/users',{name});setName('');load();}}>Ekle</button></div><ul className="space-y-2">{items.map(u=><li key={u._id} className="bg-white/5 rounded-xl p-2 flex justify-between">{u.name} ({u.isActive?'Aktif':'Pasif'})<button onClick={async()=>{await api.patch(`/users/${u._id}/toggle`);load();}}>Durum Değiştir</button></li>)}</ul></div>; }

export function RisalesPage() { const [items,setItems]=useState<any[]>([]); const [form,setForm]=useState({category:'',title:'',content:''}); const load=async()=>setItems((await api.get('/risales')).data); useEffect(()=>{load();},[]);
return <div className="glass p-4 space-y-2"><h2 className="text-neon">Risales Panel</h2><input className="bg-white/10 rounded-xl p-2 w-full" placeholder="Kategori" value={form.category} onChange={e=>setForm({...form,category:e.target.value})}/><input className="bg-white/10 rounded-xl p-2 w-full" placeholder="Başlık" value={form.title} onChange={e=>setForm({...form,title:e.target.value})}/><textarea className="bg-white/10 rounded-xl p-2 w-full min-h-32" placeholder="İçerik" value={form.content} onChange={e=>setForm({...form,content:e.target.value})}/><button className="bg-neon/30 rounded-xl px-4 py-2" onClick={async()=>{await api.post('/risales',form);setForm({category:'',title:'',content:''});load();}}>Kaydet</button><ul className="space-y-2">{items.map(r=><li key={r._id} className="bg-white/5 p-2 rounded-xl">{r.category} / {r.title}</li>)}</ul></div>; }

export function SettingsPage() { const [form,setForm]=useState({panelLink:'',tokenRefreshMinutes:60,logLevel:'info'}); useEffect(()=>{api.get('/settings').then(r=>setForm(r.data));},[]);
return <div className="glass p-4 space-y-2"><h2 className="text-neon">Ayarlar</h2><input className="bg-white/10 rounded-xl p-2 w-full" value={form.panelLink||''} onChange={e=>setForm({...form,panelLink:e.target.value})}/><input className="bg-white/10 rounded-xl p-2 w-full" type="number" value={form.tokenRefreshMinutes||60} onChange={e=>setForm({...form,tokenRefreshMinutes:Number(e.target.value)})}/><select className="bg-white/10 rounded-xl p-2 w-full" value={form.logLevel||'info'} onChange={e=>setForm({...form,logLevel:e.target.value})}><option>error</option><option>warn</option><option>info</option><option>debug</option></select><button className="bg-neon/30 rounded-xl px-4 py-2" onClick={async()=>{await api.post('/settings',form);}}>Kaydet</button></div>; }

export function RemoteConfigPage() { const [form,setForm]=useState({maintenance_mode:false,min_app_version:'1.0.0',force_update:false,message_banner:''}); useEffect(()=>{api.get('/remote-config').then(r=>r.data&&setForm(r.data));},[]);
return <div className="glass p-4 space-y-3"><h2 className="text-neon">Sistem / Güncelleme (Remote Config)</h2><label className="flex gap-2"><input type="checkbox" checked={form.maintenance_mode} onChange={e=>setForm({...form,maintenance_mode:e.target.checked})}/>maintenance_mode</label><label className="flex gap-2"><input type="checkbox" checked={form.force_update} onChange={e=>setForm({...form,force_update:e.target.checked})}/>force_update</label><input className="bg-white/10 rounded-xl p-2 w-full" value={form.min_app_version} onChange={e=>setForm({...form,min_app_version:e.target.value})} placeholder="min_app_version"/><textarea className="bg-white/10 rounded-xl p-2 w-full" value={form.message_banner} onChange={e=>setForm({...form,message_banner:e.target.value})} placeholder="message_banner"/><button className="bg-neon/30 rounded-xl px-4 py-2" onClick={async()=>{await api.post('/remote-config',form);}}>Kaydet</button></div>; }

export function renderPage(active: MenuKey, notify: (s: string) => void) {
  if (active === 'dashboard') return <Dashboard />;
  if (active === 'create') return <KeyCreate notify={notify} />;
  if (active === 'list') return <KeyList />;
  if (active === 'admins') return <AdminsPage />;
  if (active === 'users') return <UsersPage />;
  if (active === 'risales') return <RisalesPage />;
  if (active === 'settings') return <SettingsPage />;
  return <RemoteConfigPage />;
}
