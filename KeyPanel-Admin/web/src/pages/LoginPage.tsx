import { useState } from 'react';
import api from '../api/client';

export function LoginPage({ onSuccess }: { onSuccess: () => void }) {
  const [token, setToken] = useState('');
  const [error, setError] = useState('');

  const login = async () => {
    setError('');
    try {
      const { data } = await api.post('/auth/login', { token });
      localStorage.setItem('panel_jwt', data.accessToken);
      onSuccess();
    } catch {
      setError('Token geçersiz.');
    }
  };

  return (
    <div className="min-h-screen flex items-center justify-center p-4">
      <div className="glass w-full max-w-md p-6">
        <h1 className="text-2xl font-bold mb-4 text-neon">Admin Giriş</h1>
        <input
          className="w-full bg-white/10 rounded-xl p-3"
          placeholder="Admin token"
          value={token}
          onChange={(e) => setToken(e.target.value)}
        />
        {error && <p className="text-red-400 mt-2">{error}</p>}
        <button className="mt-4 w-full rounded-xl p-3 bg-neon/30" onClick={login}>Giriş Yap</button>
      </div>
    </div>
  );
}
