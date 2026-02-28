import app from './app.js';
import { env } from './config/env.js';
import { connectDb } from './config/db.js';
import { ensureBootstrapAdmin, ensureRemoteConfig } from './utils/seed.js';

async function bootstrap() {
  await connectDb();
  await ensureBootstrapAdmin(env.adminBootstrapToken);
  await ensureRemoteConfig();

  app.listen(env.port, () => {
    console.log(`UAssetGUI Key Panel API running on :${env.port}`);
  });
}

bootstrap().catch((err) => {
  console.error('Boot failed', err);
  process.exit(1);
});
