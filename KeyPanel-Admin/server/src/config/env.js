import dotenv from 'dotenv';

dotenv.config();

export const env = {
  port: Number(process.env.PORT || 8080),
  mongoUri: process.env.MONGO_URI || 'mongodb://127.0.0.1:27017/uassetgui_panel',
  jwtSecret: process.env.JWT_SECRET || 'change-me-jwt-secret',
  adminBootstrapToken: process.env.ADMIN_BOOTSTRAP_TOKEN || 'change-me-bootstrap',
  panelPublicLink: process.env.PANEL_PUBLIC_LINK || 'https://urazpanel/uassetvip.com/',
  tokenTtl: process.env.JWT_TTL || '8h'
};
