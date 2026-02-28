import crypto from 'crypto';

export function hashToken(token) {
  return crypto.createHash('sha256').update(String(token)).digest('hex');
}

export function tokenMatches(input, storedHash) {
  return hashToken(input) === storedHash;
}
