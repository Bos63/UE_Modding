import express from 'express';
import cors from 'cors';

const app = express();
const port = process.env.PORT || 8080;
const ADMIN_TOKEN = process.env.ADMIN_TOKEN || 'change-me-admin-token';

app.use(cors());
app.use(express.json());
app.use(express.static('public'));

const keys = new Map();

function createRandomKey(prefix = 'UAVIP') {
  const pool = 'ABCDEFGHJKLMNPQRSTUVWXYZ23456789';
  let rand = '';
  for (let i = 0; i < 16; i++) {
    rand += pool[Math.floor(Math.random() * pool.length)];
  }
  return `${prefix}-${rand}`;
}

function calculateExpiry(durationType, customDays) {
  const now = new Date();
  const expiresAt = new Date(now);

  switch (durationType) {
    case 'hourly':
      expiresAt.setHours(expiresAt.getHours() + 1);
      break;
    case 'daily':
      expiresAt.setDate(expiresAt.getDate() + 1);
      break;
    case 'weekly':
      expiresAt.setDate(expiresAt.getDate() + 7);
      break;
    case 'monthly':
      expiresAt.setMonth(expiresAt.getMonth() + 1);
      break;
    case 'custom_days': {
      const days = Number(customDays || 0);
      if (!Number.isFinite(days) || days <= 0) return null;
      expiresAt.setDate(expiresAt.getDate() + days);
      break;
    }
    default:
      return null;
  }

  return expiresAt.toISOString();
}

app.post('/api/mobile/validate-key', (req, res) => {
  const key = (req.body?.key || '').trim();
  const record = keys.get(key);

  if (!record) return res.json({ valid: false });
  if (record.expiresAt && new Date(record.expiresAt) < new Date()) {
    return res.json({ valid: false, expiresAt: record.expiresAt });
  }

  return res.json({ valid: true, expiresAt: record.expiresAt, durationType: record.durationType });
});

app.use('/api/admin', (req, res, next) => {
  if (req.headers['x-admin-token'] !== ADMIN_TOKEN) {
    return res.status(401).json({ error: 'unauthorized' });
  }
  next();
});

app.get('/api/admin/keys', (_req, res) => {
  res.json(
    Array.from(keys.entries()).map(([key, value]) => ({
      key,
      expiresAt: value.expiresAt,
      durationType: value.durationType,
      createdAt: value.createdAt
    }))
  );
});

app.post('/api/admin/keys', (req, res) => {
  const { customKey, randomKey, durationType, customDays } = req.body || {};

  const generatedKey = randomKey
    ? createRandomKey('UAVIP')
    : (customKey || '').trim();

  if (!generatedKey) {
    return res.status(400).json({ error: 'key required' });
  }

  const expiresAt = calculateExpiry(durationType, customDays);
  if (!expiresAt) {
    return res.status(400).json({ error: 'invalid durationType (hourly, daily, weekly, monthly, custom_days)' });
  }

  const record = {
    createdAt: new Date().toISOString(),
    durationType,
    expiresAt
  };

  keys.set(generatedKey, record);
  return res.json({ ok: true, key: generatedKey, ...record });
});

app.delete('/api/admin/keys/:key', (req, res) => {
  keys.delete(req.params.key);
  res.json({ ok: true });
});

app.listen(port, () => console.log(`Key panel running on :${port}`));
