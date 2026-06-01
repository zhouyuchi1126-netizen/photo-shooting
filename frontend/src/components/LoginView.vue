<template>
  <section class="auth-view">
    <div class="auth-card">
      <h1>{{ t('login') }}</h1>
      <div class="login-form">
        <label>
          {{ t('username') }}
          <input ref="userInput" v-model="form.username" type="text" autocomplete="username" @keydown.enter="focusPassword" />
        </label>
        <label>
          {{ t('password') }}
          <div class="pw-wrap">
            <input
              ref="pwInput"
              v-model="form.password"
              :type="showPw ? 'text' : 'password'"
              required autocomplete="current-password"
              @keydown.enter="submitLogin"
            />
            <button class="pw-toggle" type="button" @click="showPw = !showPw" :title="showPw ? t('hidePassword') : t('showPassword')">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
                <path v-if="showPw" d="M17.94 17.94A10.07 10.07 0 0112 20c-7 0-11-8-11-8a18.45 18.45 0 015.06-5.94M9.9 4.24A9.12 9.12 0 0112 4c7 0 11 8 11 8a18.5 18.5 0 01-2.16 3.19m-6.72-1.07a3 3 0 11-4.24-4.24"/>
                <line v-if="showPw" x1="1" y1="1" x2="23" y2="23"/>
                <path v-if="!showPw" d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"/>
                <circle v-if="!showPw" cx="12" cy="12" r="3"/>
              </svg>
            </button>
          </div>
        </label>
        <button class="submit-btn" @click="submitLogin">{{ t('login') }}</button>
        <p class="message" v-if="errorMessage">{{ errorMessage }}</p>
      </div>

      <p class="help-text">{{ t('noAccount') }} <router-link to="/register" class="highlight-link">{{ t('goToRegister') }}</router-link></p>
    </div>
  </section>
</template>

<script setup>
import { nextTick, onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '../api/auth';
import { useI18n } from '../i18n';

const router = useRouter();
const { t } = useI18n();
const errorMessage = ref('');
const showPw = ref(false);
const form = reactive({ username: '', password: '' });
const pwInput = ref(null);

// 上次登录的用户名（退出登录后保留）
form.username = localStorage.getItem('lastUsername') || '';

let submitting = false;

function focusPassword() {
  pwInput.value?.focus();
}

async function submitLogin() {
  if (!form.username || !form.password) return;
  if (submitting) return;
  submitting = true;
  errorMessage.value = '';
  try {
    const response = await login(form);
    if (response.success) {
      localStorage.setItem('lastUsername', form.username);
      localStorage.setItem('user', JSON.stringify(response));
      await router.push('/home');
    } else {
      errorMessage.value = response.message || '登录失败';
      nextTick(() => { pwInput.value?.focus(); submitting = false; });
      return;
    }
  } catch (error) {
    errorMessage.value = '服务器连接失败';
  }
  submitting = false;
}

onMounted(() => {
  nextTick(() => pwInput.value?.focus());
});
</script>

<style scoped>
.auth-view {
  display: grid; place-items: center; min-height: 70vh;
}
.auth-card {
  width: min(420px, 100%); box-sizing: border-box;
  border: 1px solid #e6e6e6; padding: 2rem; border-radius: 8px;
  box-shadow: 0 0 24px rgba(0,0,0,0.04);
}
.auth-card h1 { margin: 0 0 1.5rem; font-size: 2rem; text-align: center; }
label { display: block; margin-bottom: 1rem; font-size: 0.95rem; }
input {
  width: 100%; padding: 0.85rem 1rem;
  border: 1px solid #d9d9d9; border-radius: 6px;
  font-size: 1rem; box-sizing: border-box; margin-top: 0.4rem;
}
.pw-wrap { position: relative; margin-top: 0.4rem; }
.pw-wrap input { padding-right: 3rem; margin-top: 0; }
.pw-toggle {
  position: absolute; right: 4px; top: 50%; transform: translateY(-50%);
  width: 36px; height: 36px; padding: 0; display: grid; place-items: center;
  background: transparent; border: none; color: #999; cursor: pointer;
}
.pw-toggle:hover { color: #333; }
.submit-btn {
  width: 100%; padding: 0.95rem 1rem; margin-top: 1.5rem;
  border: none; border-radius: 6px;
  background: #111; color: white; font-size: 1rem; cursor: pointer;
}
.submit-btn:hover { opacity: 0.85; }

.help-text { margin-top: 1rem; text-align: center; }

.highlight-link {
  color: #111;
  font-weight: 600;
  border-bottom: 1px solid transparent;
  transition: border-color 0.15s;
}
.highlight-link:hover {
  border-bottom-color: #111;
}
.message { margin-top: 0; color: #d9534f; text-align: center; font-size: 0.9rem; }
</style>
