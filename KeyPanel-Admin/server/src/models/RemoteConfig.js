import mongoose from 'mongoose';

const remoteConfigSchema = new mongoose.Schema(
  {
    maintenance_mode: { type: Boolean, default: false },
    min_app_version: { type: String, default: '1.0.0' },
    force_update: { type: Boolean, default: false },
    message_banner: { type: String, default: '' }
  },
  { timestamps: true, versionKey: false }
);

export const RemoteConfigModel = mongoose.model('RemoteConfig', remoteConfigSchema);
