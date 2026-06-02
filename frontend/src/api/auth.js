import axios from 'axios';

const api = axios.create({
  baseURL: '/api/auth'
});

export async function login(credentials) {
  const response = await api.post('/login', credentials);
  return response.data;
}

export async function register(payload) {
  const response = await api.post('/register', payload);
  return response.data;
}

const wechatApi = axios.create({ baseURL: '/api/auth/wechat' });

export async function getWechatQrcode() {
  const response = await wechatApi.post('/qrcode');
  return response.data;
}

export async function getWechatStatus(ticket) {
  const response = await wechatApi.get(`/status/${ticket}`);
  return response.data;
}

export async function simulateWechatScan(ticket, data) {
  const response = await wechatApi.post(`/simulate/${ticket}`, data || {});
  return response.data;
}

const phoneApi = axios.create({ baseURL: '/api/auth/phone' });

export async function sendSmsCode(phone) {
  const response = await phoneApi.post('/send-code', { phone });
  return response.data;
}

export async function phoneLogin(phone, code) {
  const response = await phoneApi.post('/login', { phone, code });
  return response.data;
}
