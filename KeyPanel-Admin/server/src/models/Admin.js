import mongoose from 'mongoose';

const adminSchema = new mongoose.Schema(
  {
    username: { type: String, unique: true, required: true },
    passwordHash: { type: String, required: true },
    role: { type: String, enum: ['SuperAdmin', 'Admin', 'Viewer'], default: 'Admin' },
    isActive: { type: Boolean, default: true }
  },
  { timestamps: true, versionKey: false }
);

export const AdminModel = mongoose.model('Admin', adminSchema);
