import { createWebHistory, createRouter } from 'vue-router';
import LoginView from '../components/LoginView.vue';
import RegisterView from '../components/RegisterView.vue';
import HomeView from '../components/HomeView.vue';
import GalleryView from '../components/GalleryView.vue';
import AdminView from '../components/AdminView.vue';

const routes = [
  { path: '/', name: 'Login', component: LoginView },
  { path: '/register', name: 'Register', component: RegisterView },
  { path: '/home', name: 'Home', component: HomeView },
  { path: '/gallery/:groupId', name: 'Gallery', component: GalleryView, props: true },
  { path: '/admin', name: 'Admin', component: AdminView }
];

const router = createRouter({ history: createWebHistory(), routes });

router.beforeEach((to, from, next) => {
  const user = (() => { try { return JSON.parse(localStorage.getItem('user')); } catch { return null; } })();
  const isAdmin = user?.role === 'admin';

  if (to.name === 'Admin' && !isAdmin) return next({ name: 'Login' });
  if ((to.name === 'Login' || to.name === 'Register') && isAdmin) return next({ name: 'Admin' });
  if ((to.name === 'Login' || to.name === 'Register') && user && !isAdmin) return next({ name: 'Home' });

  next();
});

export default router;
