import { Router } from 'express';
import { AdminModel } from '../models/Admin.js';
import { requireAuth, requireRole } from '../middleware/auth.js';
import { hashToken } from '../utils/tokenHash.js';

const router = Router();
router.use(requireAuth, requireRole('SuperAdmin', 'Admin'));

router.get('/admins', async (_req, res) => {
  const items = await AdminModel.find().select('-passwordHash').sort({ createdAt: -1 });
  res.json(items);
});

router.post('/admins', async (req, res) => {
  const { username, token, role = 'Admin' } = req.body || {};
  if (!username || !token) return res.status(400).json({ error: 'username and token required' });
  const passwordHash = hashToken(token);
  const created = await AdminModel.create({ username, passwordHash, role });
  res.status(201).json({ _id: created._id, username: created.username, role: created.role, isActive: created.isActive });
});

router.delete('/admins/:id', async (req, res) => {
  await AdminModel.findByIdAndDelete(req.params.id);
  res.json({ ok: true });
});

export default router;
