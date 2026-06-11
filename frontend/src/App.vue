<template>
  <div class="app-shell">
    <header class="app-header">
      <router-link to="/home" class="logo">MR WORRY'S PORTFOLIO</router-link>
      <nav class="app-nav">
        <template v-if="user">
          <span class="sort-icon-wrap" v-if="route.name === 'Home'">
            <button class="sort-btn" @click="sortOpen = !sortOpen" title="排序">
              <svg viewBox="0 0 24 24" width="19" height="19" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round">
                <line x1="3" y1="5" x2="11" y2="5"/>
                <line x1="3" y1="12" x2="11" y2="12"/>
                <line x1="3" y1="19" x2="11" y2="19"/>
                <polyline points="14,10 18,15 22,10" stroke-width="1.6"/>
              </svg>
            </button>
            <div class="sort-dropdown" v-if="sortOpen" @click.stop>
              <div class="sort-item" :class="{ active: currentSort === 'time-desc' }" @click="setSort('time-desc')">拍摄时间正序</div>
              <div class="sort-item" :class="{ active: currentSort === 'time-asc' }" @click="setSort('time-asc')">拍摄时间倒序</div>
              <div class="sort-item" :class="{ active: currentSort === 'name-asc' }" @click="setSort('name-asc')">名称正序</div>
              <div class="sort-item" :class="{ active: currentSort === 'name-desc' }" @click="setSort('name-desc')">名称倒序</div>
            </div>
          </span>
          <span class="app-user" :class="{ 'mobile-hide': user?.role === 'admin' }">{{ user.displayName || user.username }}</span>
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
          <span class="lang-globe-wrap">
            <button class="lang-globe-btn" @click="langOpen = !langOpen" title="语言 / Language">
              <svg viewBox="0 0 24 24" width="20" height="20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
                <circle cx="12" cy="12" r="10"/>
                <ellipse cx="12" cy="12" rx="4" ry="10"/>
                <line x1="2" y1="12" x2="22" y2="12"/>
              </svg>
            </button>
            <div class="lang-dropdown" v-if="langOpen" @click.stop>
              <div
                v-for="loc in locales"
                :key="loc.code"
                class="lang-dropdown-item"
                :class="{ active: getLocale().value === loc.code }"
                @click="setLocale(loc.code); langOpen = false"
              >{{ loc.label }}</div>
            </div>
          </span>
        </template>
      </nav>
    </header>
    <main class="app-main">
      <router-view />
    </main>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import { useI18n, locales, setLocale, getLocale } from './i18n';

const router = useRouter();
const route = useRoute();
const { t } = useI18n();

/* ---- 语言切换下拉 ---- */
const langOpen = ref(false);

/* ---- 排序 ---- */
const sortOpen = ref(false);
const currentSort = ref(localStorage.getItem('sortPreference') || 'time-desc');
function setSort(val) {
  currentSort.value = val;
  localStorage.setItem('sortPreference', val);
  sortOpen.value = false;
  window.dispatchEvent(new CustomEvent('sort-changed', { detail: val }));
}
function onDocClick(e) {
  // 关闭排序下拉
  if (sortOpen.value) {
    const el = document.querySelector('.sort-icon-wrap');
    if (el && !el.contains(e.target)) sortOpen.value = false;
  }
  // 关闭语言切换下拉
  if (langOpen.value) {
    const el = document.querySelector('.lang-globe-wrap');
    if (el && !el.contains(e.target)) langOpen.value = false;
  }
}

// 全局图片保护：禁止右键/拖拽保存
function onGlobalContextMenu(e) {
  if (e.target.tagName === 'IMG') {
    e.preventDefault();
  }
}
function onGlobalDragStart(e) {
  if (e.target.tagName === 'IMG') {
    e.preventDefault();
  }
}
onMounted(() => {
  document.addEventListener('contextmenu', onGlobalContextMenu);
  document.addEventListener('dragstart', onGlobalDragStart);
  document.addEventListener('click', onDocClick);
});
onUnmounted(() => {
  document.removeEventListener('contextmenu', onGlobalContextMenu);
  document.removeEventListener('dragstart', onGlobalDragStart);
  document.removeEventListener('click', onDocClick);
});

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
  router.push('/home');
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
  font-size: 1rem; font-weight: 700; letter-spacing: 0.15em; text-transform: uppercase;
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

/* 语言切换地球图标 + 下拉 */
.lang-globe-wrap { position: relative; display: inline-flex; }
.lang-globe-btn {
  display: grid; place-items: center;
  width: 30px; height: 30px; padding: 0;
  background: transparent; border: none; color: #888; cursor: pointer;
}
.lang-globe-btn:hover { color: #333; }
.lang-dropdown {
  position: absolute; top: 100%; right: 0; z-index: 100;
  background: #fff; border: 1px solid #ececec; box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  min-width: 110px; margin-top: 4px;
}
.lang-dropdown-item {
  padding: 0.5rem 0.8rem; font-size: 0.85rem; color: #444; cursor: pointer;
}
.lang-dropdown-item:hover { background: #f5f5f5; }
.lang-dropdown-item.active { font-weight: 600; color: #111; }

.logout-btn {
  display: grid; place-items: center;
  width: 32px; height: 32px; padding: 0;
  background: transparent; border: none; color: #888;
  cursor: pointer; transition: color 0.15s;
}
.logout-btn:hover { color: #333; }

.app-main { max-width: 1080px; margin: 0 auto; padding: 2rem; }

/* 排序控件 */
.sort-icon-wrap { position: relative; display: inline-flex; }
.sort-btn {
  display: grid; place-items: center;
  width: 30px; height: 30px; padding: 0;
  background: transparent; border: none; color: #888; cursor: pointer;
}
.sort-btn:hover { color: #333; }
.sort-dropdown {
  position: absolute; top: 100%; left: 0; z-index: 100;
  background: #fff; border: 1px solid #ececec; box-shadow: 0 4px 12px rgba(0,0,0,0.08);
  min-width: 120px; margin-top: 4px;
}
.sort-item {
  padding: 0.5rem 0.8rem; font-size: 0.85rem; color: #444; cursor: pointer;
}
.sort-item:hover { background: #f5f5f5; }
.sort-item.active { font-weight: 600; color: #111; }

/* 全局图片保护 */
img {
  -webkit-user-drag: none;
  user-select: none;
  -webkit-user-select: none;
  pointer-events: auto;
}

@media (max-width: 767px) {
  .app-header { padding: 0.75rem 1rem; gap: 0.5rem; }
  .app-nav { gap: 0.5rem; }
  .sort-icon-wrap { display: none; }
  .divider { display: none; }
  .app-user.mobile-hide { display: none; }
  .lang-globe-wrap { position: relative; }
  .lang-dropdown { right: 0; left: auto; }
  .app-main { padding: 1rem; }
}

/* ========== Element Plus 主题覆盖 ========== */
/* 主色调改为黑色 */
.el-button--primary {
  --el-button-bg-color: #111;
  --el-button-border-color: #111;
  --el-button-hover-bg-color: #333;
  --el-button-hover-border-color: #333;
  --el-button-active-bg-color: #000;
  --el-button-active-border-color: #000;
}

.el-button--primary.is-round {
  border-radius: 6px;
}

/* 输入框样式 */
.el-input__wrapper {
  border-radius: 6px;
  box-shadow: 0 0 0 1px #d9d9d9 inset;
}
.el-input__wrapper:hover {
  box-shadow: 0 0 0 1px #bbb inset;
}
.el-input__wrapper.is-focus {
  box-shadow: 0 0 0 1px #111 inset;
}

/* 选择器 */
.el-select .el-input__wrapper { border-radius: 6px; }
.el-select-dropdown__item.selected { font-weight: 600; }

/* 表单标签 */
.el-form-item__label { font-size: 0.95rem; color: #333; }

/* 消息提示 */
.el-message { min-width: auto; }

/* 对话框 */
.el-message-box { border-radius: 8px; }
.el-message-box__btns .el-button--primary {
  --el-button-bg-color: #111;
  --el-button-border-color: #111;
}

/* 复选框 */
.el-checkbox__input.is-checked .el-checkbox__inner {
  background-color: #111;
  border-color: #111;
}

/* 未登录时语言切换使用 router-link */
.app-nav .lang-link.router-link-active { font-weight: 600; color: #111; }

/* 防止 iOS 输入时自动放大 */
@media (max-width: 767px) {
  input, textarea, select, .el-input__inner { font-size: 16px !important; }
}
</style>
