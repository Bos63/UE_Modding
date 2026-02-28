import mongoose from 'mongoose';

const keySchema = new mongoose.Schema(
  {
    key: { type: String, unique: true, required: true, trim: true },
    type: {
      type: String,
      enum: ['hourly', 'daily', 'weekly', 'monthly'],
      required: true
    },
    createdAt: { type: Date, default: Date.now },
    expiresAt: { type: Date, required: true }
  },
  { versionKey: false }
);

export const KeyModel = mongoose.model('Key', keySchema);
