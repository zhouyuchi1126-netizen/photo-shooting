<template>
  <section class="auth-view">
    <div class="auth-card">
      <h1>{{ t('register') }}</h1>
      <form @submit.prevent="submitRegister">
        <label>
          {{ t('username') }}
          <input v-model="form.username" type="text" required autocomplete="username" />
        </label>
        <label>
          {{ t('displayName') }}
          <input v-model="form.displayName" type="text" required autocomplete="name" />
        </label>
        <label>
          {{ t('password') }}
          <div class="pw-wrap">
            <input v-model="form.password" :type="showPw ? 'text' : 'password'" required autocomplete="new-password" />
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
        <p class="password-hint">{{ t('passwordHint') }}</p>
        <button type="submit">{{ t('register') }}</button>
      </form>
      <p class="help-text">{{ t('hasAccount') }} <router-link to="/" class="highlight-link">{{ t('backToLogin') }}</router-link></p>
      <p class="message" v-if="message">{{ message }}</p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { register } from '../api/auth';
import { useI18n } from '../i18n';

const router = useRouter();
const { t } = useI18n();
const message = ref('');
const showPw = ref(false);
const form = reactive({ username: '', password: '', displayName: '' });

async function submitRegister() {
  message.value = '';
  try {
    const response = await register(form);
    if (response.success) {
      localStorage.setItem('user', JSON.stringify(response));
      await router.push('/home');
    } else {
      message.value = response.message || '注册失败';
    }
  } catch (error) {
    message.value = '服务器连接失败';
  }
}
</script>

<style scoped>
.auth-view {
  display: grid;
  place-items: center;
  min-height: 70vh;
}

.auth-card {
  width: min(420px, 100%);
  box-sizing: border-box;
  border: 1px solid #e6e6e6;
  padding: 2rem;
  border-radius: 8px;
  box-shadow: 0 0 24px rgba(0,0,0,0.04);
}

.auth-card h1 {
  margin: 0 0 1.5rem;
  font-size: 2rem;
  text-align: center;
}

label {
  display: block;
  margin-bottom: 1rem;
  font-size: 0.95rem;
}

input {
  width: 100%;
  padding: 0.85rem 1rem;
  border: 1px solid #d9d9d9;
  border-radius: 6px;
  font-size: 1rem;
  box-sizing: border-box;
  margin-top: 0.4rem;
}

button[type="submit"] {
  width: 100%;
  padding: 0.95rem 1rem;
  border: none;
  border-radius: 6px;
  background: #111;
  color: white;
  font-size: 1rem;
  cursor: pointer;
}

.pw-wrap {
  position: relative;
  margin-top: 0.4rem;
}
.pw-wrap input {
  padding-right: 3rem;
  margin-top: 0;
}
.pw-toggle {
  position: absolute;
  right: 4px; top: 50%;
  transform: translateY(-50%);
  width: 36px; height: 36px;
  padding: 0;
  display: grid; place-items: center;
  background: transparent; border: none;
  color: #999; cursor: pointer;
}
.pw-toggle:hover { color: #333; }

.password-hint {
  margin: -0.5rem 0 1rem;
  color: #666;
  font-size: 0.9rem;
}

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
.message { margin-top: 1rem; color: #d9534f; text-align: center; }
</style>
