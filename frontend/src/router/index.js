import { createWebHashHistory, createRouter } from 'vue-router';
import LoginView from '../components/LoginView.vue';
import RegisterView from '../components/RegisterView.vue';
import HomeView from '../components/HomeView.vue';
import GalleryView from '../components/GalleryView.vue';
import AdminView from '../components/AdminView.vue';

const routes = [
  { path: '/', redirect: '/home' },
  { path: '/login', name: 'Login', component: LoginView },
  { path: '/register', name: 'Register', component: RegisterView },
  { path: '/home', name: 'Home', component: HomeView },
  { path: '/gallery/:groupId', name: 'Gallery', component: GalleryView, props: true },
  { path: '/admin', name: 'Admin', component: AdminView }
];

const router = createRouter({ history: createWebHashHistory(), routes });

router.beforeEach((to, from, next) => {
  const user = (() => { try { return JSON.parse(localStorage.getItem('user')); } catch { return null; } })();

  // 已登录管理员访问登录/注册页 → 跳管理后台
  if ((to.name === 'Login' || to.name === 'Register') && user?.role === 'admin') return next({ name: 'Admin' });
  // 已登录普通用户访问登录/注册页 → 跳首页
  if ((to.name === 'Login' || to.name === 'Register') && user && user.role !== 'admin') return next({ name: 'Home' });

  next();
});

export default router;
