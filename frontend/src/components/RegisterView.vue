<template>
  <section class="auth-view">
    <div class="auth-card">
      <h1>{{ t('register') }}</h1>

      <el-form :model="form" @submit.prevent="submitRegister" label-position="top" :rules="rules" ref="formRef">
        <el-form-item :label="t('username')" prop="username">
          <el-input v-model="form.username" autocomplete="username" />
        </el-form-item>
        <el-form-item :label="t('displayName')" prop="displayName">
          <el-input v-model="form.displayName" autocomplete="name" />
        </el-form-item>
        <el-form-item :label="t('password')" prop="password">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            autocomplete="new-password"
          />
        </el-form-item>
        <p class="password-hint">{{ t('passwordHint') }}</p>
        <el-form-item>
          <button type="submit" class="submit-btn" :disabled="submitting">
            <span v-if="submitting" class="btn-loading">···</span>
            {{ t('register') }}
          </button>
        </el-form-item>
      </el-form>

      <p class="help-text">{{ t('hasAccount') }} <router-link to="/" class="highlight-link">{{ t('backToLogin') }}</router-link></p>
    </div>
  </section>
</template>

<script setup>
import { reactive, ref } from 'vue';
import { useRouter } from 'vue-router';
import { register } from '../api/auth';
import { useI18n } from '../i18n';
import { ElMessage } from 'element-plus';

const router = useRouter();
const { t } = useI18n();
const submitting = ref(false);
const formRef = ref(null);
const form = reactive({ username: '', password: '', displayName: '' });

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  displayName: [{ required: true, message: '请输入显示名称', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码至少 6 位', trigger: 'blur' },
  ],
};

async function submitRegister() {
  if (!formRef.value) return;
  try {
    await formRef.value.validate();
  } catch {
    return;
  }
  submitting.value = true;
  try {
    const response = await register(form);
    if (response.success) {
      localStorage.setItem('user', JSON.stringify(response));
      await router.push('/home');
    } else {
      ElMessage.error(response.message || '注册失败');
    }
  } catch {
    ElMessage.error('服务器连接失败');
  }
  submitting.value = false;
}
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
.submit-btn {
  width: 100%; padding: 0.95rem 1rem; border: none; border-radius: 6px;
  background: #111; color: white; font-size: 1rem; cursor: pointer;
}
.submit-btn:hover { opacity: 0.85; }
.submit-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-loading { margin-right: 4px; }
.password-hint { margin: -0.5rem 0 0.5rem; color: #666; font-size: 0.9rem; }
.help-text { margin-top: 1rem; text-align: center; }
.highlight-link { color: #111; font-weight: 600; border-bottom: 1px solid transparent; transition: border-color 0.15s; }
.highlight-link:hover { border-bottom-color: #111; }
</style>
