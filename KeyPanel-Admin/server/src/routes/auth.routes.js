import { Router } from 'express';
import jwt from 'jsonwebtoken';
import { AdminModel } from '../models/Admin.js';
import { env } from '../config/env.js';
import { tokenMatches } from '../utils/tokenHash.js';

const router = Router();

router.post('/login', async (req, res) => {
  const { token } = req.body || {};
  if (!token) return res.status(400).json({ error: 'token required' });

  const admin = await AdminModel.findOne({ isActive: true });
  if (!admin) return res.status(500).json({ error: 'No admin configured' });

  const ok = tokenMatches(token, admin.passwordHash);
  if (!ok) return res.status(401).json({ error: 'Invalid admin token' });

  const accessToken = jwt.sign({ sub: admin._id.toString(), role: admin.role, username: admin.username }, env.jwtSecret, {
    expiresIn: env.tokenTtl
  });

  res.json({ accessToken, admin: { username: admin.username, role: admin.role } });
});

export default router;
