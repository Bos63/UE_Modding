import { Router } from 'express';
import { RemoteConfigModel } from '../models/RemoteConfig.js';
import { requireAuth } from '../middleware/auth.js';

const router = Router();

router.get('/remote-config', async (_req, res) => {
  const doc = await RemoteConfigModel.findOne();
  res.json(doc);
});

router.post('/remote-config', requireAuth, async (req, res) => {
  const payload = {
    maintenance_mode: Boolean(req.body?.maintenance_mode),
    min_app_version: String(req.body?.min_app_version || '1.0.0'),
    force_update: Boolean(req.body?.force_update),
    message_banner: String(req.body?.message_banner || '')
  };

  const updated = await RemoteConfigModel.findOneAndUpdate({}, payload, { new: true, upsert: true });
  res.json(updated);
});

export default router;
