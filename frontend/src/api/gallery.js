import axios from 'axios';

const api = axios.create({ baseURL: '/api' });

// 从 localStorage 同步角色到 axios 默认头
function syncRole() {
  try {
    const raw = localStorage.getItem('user');
    if (raw) {
      const u = JSON.parse(raw);
      if (u.role) {
        api.defaults.headers.common['X-User-Role'] = u.role;
        return;
      }
    }
  } catch (_) {}
  delete api.defaults.headers.common['X-User-Role'];
}

// 立即同步 + 每次请求前同步
syncRole();
api.interceptors.request.use(cfg => { syncRole(); return cfg; });

export async function getGroups() {
  const res = await api.get('/gallery/groups');
  return res.data || [];
}

export async function getImages(groupId) {
  const res = await api.get(`/gallery/groups/${groupId}/images`);
  return res.data || [];
}

export async function createGroup(payload) {
  const res = await api.post('/admin/groups', payload);
  return res.data;
}

export async function uploadImage(groupId, file) {
  const fd = new FormData();
  fd.append('file', file);
  const res = await api.post(`/admin/groups/${groupId}/images`, fd);
  return res.data;
}

export async function updateGroup(groupId, payload) {
  const res = await api.put(`/admin/groups/${groupId}`, payload);
  return res.data;
}

export async function deleteGroup(groupId) {
  const res = await api.delete(`/admin/groups/${groupId}`);
  return res.data;
}

export async function deleteImage(groupId, imageName) {
  const res = await api.delete(`/admin/groups/${groupId}/images/${encodeURIComponent(imageName)}`);
  return res.data;
}

export async function setCover(groupId, imageName) {
  const res = await api.put(`/admin/groups/${groupId}/cover`, { imageName });
  return res.data;
}
