import express from 'express';
import cors from 'cors';

const app = express();
const port = process.env.PORT || 8080;
const ADMIN_TOKEN = process.env.ADMIN_TOKEN || 'change-me-admin-token';

app.use(cors());
app.use(express.json());
app.use(express.static('public'));

const keys = new Map();

app.post('/api/mobile/validate-key', (req, res) => {
  const key = (req.body?.key || '').trim();
  const record = keys.get(key);
  if (!record) {
    return res.json({ valid: false });
  }
  if (record.expiresAt && new Date(record.expiresAt) < new Date()) {
    return res.json({ valid: false });
  }
  return res.json({ valid: true, userName: record.userName, expiresAt: record.expiresAt, tier: record.tier });
});

app.use('/api/admin', (req, res, next) => {
  if (req.headers['x-admin-token'] !== ADMIN_TOKEN) {
    return res.status(401).json({ error: 'unauthorized' });
  }
  next();
});

app.get('/api/admin/keys', (_req, res) => {
  res.json(Array.from(keys.entries()).map(([key, value]) => ({ key, ...value })));
});

app.post('/api/admin/keys', (req, res) => {
  const { key, userName, tier, expiresAt } = req.body || {};
  if (!key) return res.status(400).json({ error: 'key required' });
  keys.set(key.trim(), { userName: userName || 'Premium User', tier: tier || 'premium', expiresAt: expiresAt || null });
  res.json({ ok: true });
});

app.delete('/api/admin/keys/:key', (req, res) => {
  keys.delete(req.params.key);
  res.json({ ok: true });
});

app.listen(port, () => console.log(`Key panel running on :${port}`));
