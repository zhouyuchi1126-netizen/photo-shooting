<template>
  <section class="home-view">
    <div class="portfolio-grid">
      <article class="portfolio-card" v-for="item in sections" :key="item.id" @click="goToGallery(item.id)">
        <div class="card-img-wrap">
          <img v-if="item.coverImage" :src="item.coverImage" :alt="item.title" />
        </div>
        <h2>{{ item.title }}</h2>
        <div class="card-exif" v-if="item.cameraBrand">
          <span class="card-brand">{{ brandEnLabel(item.cameraBrand) }}</span>
          <span v-if="item.cameraModel" class="card-model"> {{ item.cameraModel }}</span>
          <span v-if="item.film" class="card-film"> {{ filmLabel(item.filmStock) }}</span>
        </div>
      </article>
    </div>
  </section>
</template>

<script setup>
import { onMounted, ref } from 'vue';
import { useRouter } from 'vue-router';
import { getGroups } from '../api/gallery';
import { brandEnLabel, filmLabel, PLACEHOLDER_SVG } from '../constants';

const router = useRouter();
const sections = ref([]);

function onImgError(e) { e.target.src = PLACEHOLDER_SVG; }

function sortGroups(groups, pref) {
  if (!groups) return groups;
  const empty = [], hasImg = [];
  for (const g of groups) {
    if (g.imageCount > 0) hasImg.push(g); else empty.push(g);
  }
  const sortKey = (g) => g.shootDate || g.createdAt || '';
  if (pref === 'name-asc') hasImg.sort((a, b) => (a.title || '').localeCompare(b.title || ''));
  else if (pref === 'name-desc') hasImg.sort((a, b) => (b.title || '').localeCompare(a.title || ''));
  else if (pref === 'time-asc') hasImg.sort((a, b) => (sortKey(a) || '').localeCompare(sortKey(b) || ''));
  else if (pref === 'time-desc') hasImg.sort((a, b) => (sortKey(b) || '').localeCompare(sortKey(a) || ''));
  // 'default' 不做排序，使用后端 sort_order 顺序
  return [...hasImg, ...empty];
}

async function loadGroups() {
  try {
    const groups = await getGroups();
    if (groups && groups.length) {
      const pref = localStorage.getItem('sortPreference') || 'default';
      sections.value = sortGroups(groups, pref);
    }
  } catch (error) {
    console.warn('无法加载后台分组', error);
  }
}

function onSortChange(e) {
  const pref = e.detail || 'time-desc';
  sections.value = sortGroups(sections.value, pref);
}

function goToGallery(groupId) {
  router.push({ name: 'Gallery', params: { groupId } });
}

onMounted(() => {
  loadGroups();
  window.addEventListener('sort-changed', onSortChange);
});
</script>

<style scoped>
.home-view { display: grid; gap: 2rem; }
.portfolio-grid {
  display: grid; grid-template-columns: repeat(4, 1fr); gap: 1.75rem;
}

@media (max-width: 1023px) {
  .portfolio-grid { grid-template-columns: repeat(2, 1fr); gap: 1.25rem; }
}

@media (max-width: 767px) {
  .portfolio-grid { grid-template-columns: 1fr; gap: 1rem; }
}

.portfolio-card {
  border: 1px solid #ececec; overflow: hidden;
  transition: transform 0.25s ease, box-shadow 0.25s ease; cursor: pointer;
}
.portfolio-card:hover { transform: scale(1.03); box-shadow: 0 18px 45px rgba(0,0,0,0.08); }

.card-img-wrap { position: relative; width: 100%; aspect-ratio: 3/4; overflow: hidden; }
.card-img-wrap img { width: 100%; height: 100%; object-fit: cover; display: block; }
.card-img-placeholder {
  position: absolute; inset: 0; display: grid; place-items: center;
}
.card-img-placeholder svg { width: 100%; height: 100%; }

.portfolio-card h2 { margin: 0.75rem 1rem 0; font-size: 1.05rem; }

.card-exif {
  display: flex; align-items: center; gap: 4px; flex-wrap: wrap;
  margin: 0.3rem 1rem 0.8rem; font-size: 0.75rem; color: #888;
}
.card-brand { font-weight: 600; color: #555; }
.card-model { color: #888; }
.card-film { color: #8a7e5c; }
.card-divider { color: #ccc; }
</style>
