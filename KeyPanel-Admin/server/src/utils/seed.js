import { AdminModel } from '../models/Admin.js';
import { RemoteConfigModel } from '../models/RemoteConfig.js';
import { hashToken } from './tokenHash.js';

export async function ensureBootstrapAdmin(token) {
  const count = await AdminModel.countDocuments();
  if (count > 0) return;
  const hash = hashToken(token);
  await AdminModel.create({ username: 'bootstrap', passwordHash: hash, role: 'SuperAdmin' });
}

export async function ensureRemoteConfig() {
  const doc = await RemoteConfigModel.findOne();
  if (!doc) await RemoteConfigModel.create({});
}
