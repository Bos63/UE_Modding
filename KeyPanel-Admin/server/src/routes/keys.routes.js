import { Router } from 'express';
import { KeyModel } from '../models/Key.js';
import { generateRandomKey, computeExpiry } from '../utils/keyUtils.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

router.post('/mobile/validate-key', async (req, res) => {
  const key = (req.body?.key || '').trim();
  if (!key) return res.json({ valid: false });

  const doc = await KeyModel.findOne({ key });
  if (!doc) return res.json({ valid: false });
  if (doc.expiresAt < new Date()) return res.json({ valid: false });

  return res.json({ valid: true, expiresAt: doc.expiresAt.toISOString(), type: doc.type });
});

router.use(requireAuth);

router.post('/keys', async (req, res) => {
  const { type, mode, customName } = req.body || {};
  if (!['hourly', 'daily', 'weekly', 'monthly'].includes(type)) {
    return res.status(400).json({ error: 'Invalid type' });
  }
  if (!['random', 'custom'].includes(mode)) {
    return res.status(400).json({ error: 'Invalid mode' });
  }

  const key = mode === 'random' ? generateRandomKey('URAZ') : String(customName || '').trim();
  if (!key) return res.status(400).json({ error: 'customName required' });

  const exists = await KeyModel.exists({ key });
  if (exists) return res.status(409).json({ error: 'Key already exists' });

  const expiresAt = computeExpiry(type);
  const created = await KeyModel.create({ key, type, expiresAt, createdAt: new Date() });
  return res.status(201).json({ key: created.key, type: created.type, expiresAt: created.expiresAt });
});

router.get('/keys', async (req, res) => {
  const { type = '', q = '', page = '1' } = req.query;
  const pageNum = Math.max(1, Number(page) || 1);
  const limit = 10;
  const filter = {};

  if (type) filter.type = type;
  if (q) filter.key = { $regex: q, $options: 'i' };

  const [items, total] = await Promise.all([
    KeyModel.find(filter)
      .sort({ createdAt: -1 })
      .skip((pageNum - 1) * limit)
      .limit(limit)
      .lean(),
    KeyModel.countDocuments(filter)
  ]);

  const rows = items.map((x) => ({ key: x.key, type: x.type, date: new Date(x.expiresAt).toISOString().slice(0, 10) }));
  res.json({ items: rows, page: pageNum, totalPages: Math.max(1, Math.ceil(total / limit)) });
});

router.delete('/keys/:key', async (req, res) => {
  await KeyModel.deleteOne({ key: req.params.key });
  res.json({ ok: true });
});

export default router;
