import express from 'express';
import cors from 'cors';
import morgan from 'morgan';

import { env } from './config/env.js';
import authRoutes from './routes/auth.routes.js';
import keysRoutes from './routes/keys.routes.js';
import adminsRoutes from './routes/admins.routes.js';
import usersRoutes from './routes/users.routes.js';
import risalesRoutes from './routes/risales.routes.js';
import remoteConfigRoutes from './routes/remote-config.routes.js';
import settingsRoutes from './routes/settings.routes.js';

const app = express();

app.use(cors());
app.use(express.json());
app.use(morgan('dev'));

app.get('/health', (_req, res) => res.json({ ok: true, panelLink: env.panelPublicLink }));

app.use('/auth', authRoutes);
app.use('/', keysRoutes);
app.use('/', adminsRoutes);
app.use('/', usersRoutes);
app.use('/', risalesRoutes);
app.use('/', remoteConfigRoutes);
app.use('/', settingsRoutes);

app.use((err, _req, res, _next) => {
  console.error(err);
  res.status(500).json({ error: 'Internal server error' });
});

export default app;
