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
    phoneLogin: '手机号登录',
    passwordLogin: '密码登录',
    phoneNumber: '手机号',
    smsCode: '验证码',
    sendCode: '获取验证码',
    resendAfter: '重新发送',
    phoneHint: '请输入11位手机号',
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
    phoneLogin: '手機號登入',
    passwordLogin: '密碼登入',
    phoneNumber: '手機號',
    smsCode: '驗證碼',
    sendCode: '獲取驗證碼',
    resendAfter: '重新發送',
    phoneHint: '請輸入11位手機號',
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
    phoneLogin: 'Phone Login',
    passwordLogin: 'Password Login',
    phoneNumber: 'Phone Number',
    smsCode: 'SMS Code',
    sendCode: 'Send Code',
    resendAfter: 'Resend',
    phoneHint: 'Enter 11-digit phone number',
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
