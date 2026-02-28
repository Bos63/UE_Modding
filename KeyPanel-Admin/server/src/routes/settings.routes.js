import { Router } from 'express';
import { requireAuth } from '../middleware/auth.js';
import { SettingModel } from '../models/Setting.js';

const router = Router();

router.get('/settings', requireAuth, async (_req, res) => {
  const settings = await SettingModel.findOne();
  res.json(settings || { panelLink: 'https://urazpanel/uassetvip.com/', tokenRefreshMinutes: 60, logLevel: 'info' });
});

router.post('/settings', requireAuth, async (req, res) => {
  const payload = {
    panelLink: String(req.body?.panelLink || 'https://urazpanel/uassetvip.com/'),
    tokenRefreshMinutes: Number(req.body?.tokenRefreshMinutes || 60),
    logLevel: String(req.body?.logLevel || 'info')
  };

  const doc = await SettingModel.findOneAndUpdate({}, payload, { upsert: true, new: true });
  res.json(doc);
});

export default router;
