<template>
  <section class="auth-view">
    <div class="auth-card">
      <h1>{{ t('login') }}</h1>

      <el-form :model="form" @submit.prevent="submitLogin" label-position="top">
        <el-form-item :label="t('username')">
          <el-input
            ref="userInputRef"
            v-model="form.username"
            autocomplete="username"
            @keydown.enter="focusPassword"
          />
        </el-form-item>
        <el-form-item :label="t('password')">
          <el-input
            ref="pwInputRef"
            v-model="form.password"
            type="password"
            show-password
            autocomplete="current-password"
            @keydown.enter="submitLogin"
          />
        </el-form-item>
        <el-form-item>
          <button type="submit" class="submit-btn" :disabled="submitting">
            <span v-if="submitting" class="btn-loading">···</span>
            {{ t('login') }}
          </button>
        </el-form-item>
      </el-form>

      <p class="help-text">{{ t('noAccount') }} <router-link to="/register" class="highlight-link">{{ t('goToRegister') }}</router-link></p>
    </div>
  </section>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { login } from '../api/auth';
import { useI18n } from '../i18n';
import { ElMessage } from 'element-plus';

const router = useRouter();
const { t } = useI18n();

const submitting = ref(false);
const form = reactive({ username: localStorage.getItem('lastUsername') || '', password: '' });
const pwInputRef = ref(null);
const userInputRef = ref(null);

function focusPassword() { pwInputRef.value?.focus(); }

async function submitLogin() {
  if (!form.username || !form.password || submitting.value) return;
  submitting.value = true;
  try {
    const res = await login(form);
    if (res.success) {
      // 登录成功后重置页面缩放（解决 iOS 输入放大后不回缩的问题）
      const vp = document.querySelector('meta[name="viewport"]');
      if (vp) vp.content = 'width=device-width, initial-scale=1.0, maximum-scale=5.0';
      setTimeout(() => { if (vp) vp.content = 'width=device-width, initial-scale=1.0, maximum-scale=1.0'; }, 100);
      localStorage.setItem('lastUsername', form.username);
      localStorage.setItem('user', JSON.stringify(res));
      await router.push('/home');
    } else {
      ElMessage.error(res.message || '登录失败');
    }
  } catch { ElMessage.error('服务器连接失败'); }
  submitting.value = false;
}

onMounted(() => { userInputRef.value?.focus(); });
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
.auth-card h1 { margin: 0 0 1rem; font-size: 2rem; text-align: center; }
.submit-btn {
  width: 100%; padding: 0.95rem 1rem; border: none; border-radius: 6px;
  background: #111; color: white; font-size: 1rem; cursor: pointer;
}
.submit-btn:hover { opacity: 0.85; }
.submit-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-loading { margin-right: 4px; }
.help-text { margin-top: 1rem; text-align: center; }
.highlight-link { color: #111; font-weight: 600; border-bottom: 1px solid transparent; transition: border-color 0.15s; }
.highlight-link:hover { border-bottom-color: #111; }
</style>
