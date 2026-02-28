import mongoose from 'mongoose';

const userSchema = new mongoose.Schema(
  {
    name: { type: String, required: true },
    email: { type: String, default: '' },
    isActive: { type: Boolean, default: true }
  },
  { timestamps: true, versionKey: false }
);

export const UserModel = mongoose.model('User', userSchema);
