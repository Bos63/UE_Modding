export type MenuKey =
  | 'dashboard'
  | 'create'
  | 'list'
  | 'admins'
  | 'users'
  | 'risales'
  | 'settings'
  | 'remote';

export type KeyType = 'hourly' | 'daily' | 'weekly' | 'monthly';

export interface KeyRow {
  key: string;
  type: KeyType;
  date: string;
}
