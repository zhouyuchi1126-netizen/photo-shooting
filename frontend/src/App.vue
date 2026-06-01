<template>
  <div class="app-shell">
    <header class="app-header">
      <router-link to="/home" class="logo">MR WORRY'S PORTFOLIO</router-link>
      <nav class="app-nav">
        <template v-if="user">
          <span class="app-user">{{ user.displayName || user.username }}</span>
          <router-link v-if="user.role === 'admin'" to="/admin">相册管理</router-link>
          <span class="divider">|</span>
          <button class="logout-btn" @click="logout" title="退出登录">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2">
              <path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4"/>
              <polyline points="16 17 21 12 16 7"/>
              <line x1="21" y1="12" x2="9" y2="12"/>
            </svg>
          </button>
        </template>
        <template v-else>
          <span
            v-for="loc in locales"
            :key="loc.code"
            class="lang-link"
            :class="{ active: getLocale().value === loc.code }"
            @click="setLocale(loc.code)"
          >{{ loc.label }}</span>
        </template>
      </nav>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useI18n, locales, setLocale, getLocale } from './i18n';

const router = useRouter();
const route = useRoute();
const { t } = useI18n();

const user = computed(() => {
  try {
    route.fullPath;
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw) : null;
  } catch {
    return null;
  }
});

function logout() {
  localStorage.removeItem('user');
  router.push('/');
}
</script>

<style>
:root {
  font-family: "Helvetica Neue", Arial, "PingFang SC", "Noto Sans", sans-serif;
  color: #111;
  background: #fff;
}

body { margin: 0; }

.app-shell { min-height: 100vh; padding: 0; }

.app-header {
  position: sticky; top: 0; z-index: 20;
  display: flex; justify-content: space-between; align-items: center;
  gap: 1rem; padding: 1rem 2rem;
  background: rgba(255,255,255,0.98);
  border-bottom: 1px solid #ececec;
  backdrop-filter: blur(10px);
}

.logo {
  font-size: 1rem; letter-spacing: 0.15em; text-transform: uppercase;
  cursor: pointer; color: #111; text-decoration: none;
}
.logo:hover { opacity: 0.7; }

.app-nav { display: flex; align-items: center; gap: 0.85rem; flex-wrap: wrap; }

.divider { color: #ddd; }

a { color: #111; text-decoration: none; }
a.router-link-active { font-weight: 700; }

.lang-link {
  font-size: 0.8rem; color: #999; cursor: pointer; transition: color 0.15s;
}
.lang-link:hover { color: #333; }
.lang-link.active { color: #111; font-weight: 600; }

.logout-btn {
  display: grid; place-items: center;
  width: 32px; height: 32px; padding: 0;
  background: transparent; border: none; color: #888;
  cursor: pointer; transition: color 0.15s;
}
.logout-btn:hover { color: #333; }

.app-main { max-width: 1080px; margin: 0 auto; padding: 2rem; }
</style>
