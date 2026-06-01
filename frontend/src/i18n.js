import { computed, ref } from 'vue';

const messages = {
  'zh-CN': {
    login: '登录',
    register: '注册',
    username: '用户名',
    password: '密码',
    displayName: '显示名称',
    passwordHint: '密码至少 8 位，必须包含大小写字母和特殊字符',
    noAccount: '还没有账号？',
    hasAccount: '已有账号？',
    backToLogin: '返回登录',
    goToRegister: '立即注册',
    showPassword: '显示密码',
    hidePassword: '隐藏密码',
  },
  'zh-TW': {
    login: '登入',
    register: '註冊',
    username: '用戶名',
    password: '密碼',
    displayName: '顯示名稱（可選）',
    passwordHint: '密碼至少 8 位，必須包含大小寫字母和特殊字符',
    noAccount: '還沒有帳號？',
    hasAccount: '已有帳號？',
    backToLogin: '返回登入',
    goToRegister: '立即註冊',
    showPassword: '顯示密碼',
    hidePassword: '隱藏密碼',
  },
  'en': {
    login: 'Login',
    register: 'Register',
    username: 'Username',
    password: 'Password',
    displayName: 'Display Name (optional)',
    passwordHint: 'At least 8 characters, including uppercase, lowercase and special character',
    noAccount: 'No account yet?',
    hasAccount: 'Already have an account?',
    backToLogin: 'Back to Login',
    goToRegister: 'Register Now',
    showPassword: 'Show password',
    hidePassword: 'Hide password',
  },
};

const locale = ref(localStorage.getItem('locale') || 'zh-CN');

export function setLocale(l) {
  locale.value = l;
  localStorage.setItem('locale', l);
}

export function getLocale() {
  return locale;
}

export const locales = [
  { code: 'zh-CN', label: '简体中文' },
  { code: 'zh-TW', label: '繁體中文' },
  { code: 'en', label: 'English' },
];

export function useI18n() {
  const t = (key) => computed(() => messages[locale.value]?.[key] || messages['zh-CN'][key] || key);
  return { t, getLocale, setLocale, locales };
}
