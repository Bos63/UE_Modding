import crypto from 'crypto';

export function generateRandomKey(prefix = 'UASSETVIP') {
  return `${prefix}-${crypto.randomBytes(8).toString('hex').toUpperCase()}`;
}

export function computeExpiry(type, base = new Date()) {
  const d = new Date(base);
  if (type === 'hourly') d.setHours(d.getHours() + 1);
  else if (type === 'daily') d.setDate(d.getDate() + 1);
  else if (type === 'weekly') d.setDate(d.getDate() + 7);
  else if (type === 'monthly') d.setDate(d.getDate() + 30);
  else return null;
  return d;
}
