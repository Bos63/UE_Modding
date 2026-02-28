import { MenuKey } from '../types';

const items: { key: MenuKey; label: string }[] = [
  { key: 'dashboard', label: 'Dashboard' },
  { key: 'create', label: 'Key Oluştur' },
  { key: 'list', label: 'Key Listesi' },
  { key: 'admins', label: 'Yöneticiler' },
  { key: 'users', label: 'Kullanıcılar' },
  { key: 'risales', label: 'Risales Panel' },
  { key: 'settings', label: 'Ayarlar' },
  { key: 'remote', label: 'Sistem / Güncelleme' }
];

export function AppShell({
  active,
  onSelect,
  children,
  onLogout
}: {
  active: MenuKey;
  onSelect: (k: MenuKey) => void;
  children: React.ReactNode;
  onLogout: () => void;
}) {
  return (
    <div className="min-h-screen md:grid md:grid-cols-[250px_1fr]">
      <aside className="glass m-3 p-3">
        <div className="text-xl font-semibold mb-4 text-neon">UAssetGUI Key Panel</div>
        <nav className="space-y-2">
          {items.map((item) => (
            <button
              key={item.key}
              onClick={() => onSelect(item.key)}
              className={`w-full text-left px-3 py-2 rounded-xl ${active === item.key ? 'bg-neon/20 text-neon' : 'bg-white/5'}`}
            >
              {item.label}
            </button>
          ))}
        </nav>
      </aside>
      <main className="p-3">
        <div className="glass mb-3 p-3 flex justify-between items-center">
          <a className="text-sm text-neon underline" href="https://urazpanel/uassetvip.com/" target="_blank">https://urazpanel/uassetvip.com/</a>
          <button className="px-3 py-2 rounded-xl bg-red-500/30" onClick={onLogout}>Çıkış</button>
        </div>
        {children}
      </main>
    </div>
  );
}
