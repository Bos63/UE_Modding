import mongoose from 'mongoose';

const settingSchema = new mongoose.Schema(
  {
    panelLink: { type: String, default: 'https://urazpanel/uassetvip.com/' },
    tokenRefreshMinutes: { type: Number, default: 60 },
    logLevel: { type: String, enum: ['error', 'warn', 'info', 'debug'], default: 'info' }
  },
  { timestamps: true, versionKey: false }
);

export const SettingModel = mongoose.model('Setting', settingSchema);
