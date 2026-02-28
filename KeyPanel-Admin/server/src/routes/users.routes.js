import { Router } from 'express';
import { UserModel } from '../models/User.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();
router.use(requireAuth);

router.get('/users', async (_req, res) => {
  const items = await UserModel.find().sort({ createdAt: -1 });
  res.json(items);
});

router.post('/users', async (req, res) => {
  const { name, email = '' } = req.body || {};
  if (!name) return res.status(400).json({ error: 'name required' });
  const created = await UserModel.create({ name, email });
  res.status(201).json(created);
});

router.patch('/users/:id/toggle', async (req, res) => {
  const user = await UserModel.findById(req.params.id);
  if (!user) return res.status(404).json({ error: 'not found' });
  user.isActive = !user.isActive;
  await user.save();
  res.json(user);
});

export default router;
