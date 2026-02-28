import mongoose from 'mongoose';

const risaleSchema = new mongoose.Schema(
  {
    category: { type: String, required: true },
    title: { type: String, required: true },
    content: { type: String, required: true }
  },
  { timestamps: true, versionKey: false }
);

export const RisaleModel = mongoose.model('Risale', risaleSchema);
