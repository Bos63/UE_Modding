import { Router } from 'express';
import { RisaleModel } from '../models/Risale.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();
router.use(requireAuth);

router.get('/risales', async (_req, res) => {
  const items = await RisaleModel.find().sort({ updatedAt: -1 });
  res.json(items);
});

router.post('/risales', async (req, res) => {
  const { category, title, content } = req.body || {};
  if (!category || !title || !content) return res.status(400).json({ error: 'category/title/content required' });
  const created = await RisaleModel.create({ category, title, content });
  res.status(201).json(created);
});

router.put('/risales/:id', async (req, res) => {
  const updated = await RisaleModel.findByIdAndUpdate(req.params.id, req.body || {}, { new: true });
  res.json(updated);
});

router.delete('/risales/:id', async (req, res) => {
  await RisaleModel.findByIdAndDelete(req.params.id);
  res.json({ ok: true });
});

export default router;
