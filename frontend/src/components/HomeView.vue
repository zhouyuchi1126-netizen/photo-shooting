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

const router = useRouter();
const sections = ref([]);

const cameraBrands = [
  { id: 'canon', label: 'CANON 佳能' }, { id: 'nikon', label: 'NIKON 尼康' },
  { id: 'fujifilm', label: 'FUJIFILM 富士' }, { id: 'pentax', label: 'PENTAX 宾得' },
  { id: 'panasonic', label: 'PANASONIC 松下' }, { id: 'hasselblad', label: 'HASSELBLAD 哈苏' },
  { id: 'ricoh', label: 'RICOH 理光' }, { id: 'contax', label: 'CONTAX 康泰时' },
  { id: 'phaseone', label: 'PHASE ONE 飞思' },
];
const filmStocks = [
  { id: 'kodak-portra160', label: 'KODAK PORTRA 160' },
  { id: 'kodak-portra400', label: 'KODAK PORTRA 400' },
  { id: 'kodak-portra800', label: 'KODAK PORTRA 800' },
  { id: 'kodak-ektar100', label: 'KODAK EKTAR 100' },
  { id: 'kodak-gold200', label: 'KODAK GOLD 200' },
  { id: 'kodak-triX400', label: 'KODAK TRI-X 400' },
  { id: 'kodak-tmax400', label: 'KODAK T-MAX 400' },
  { id: 'kodak-colorplus200', label: 'KODAK COLORPLUS 200' },
  { id: 'kodak-ultramax400', label: 'KODAK ULTRAMAX 400' },
  { id: 'kodak-ektachrome100', label: 'KODAK EKTACHROME E100' },
  { id: 'fuji-provia100f', label: 'FUJIFILM PROVIA 100F' },
  { id: 'fuji-velvia50', label: 'FUJIFILM VELVIA 50' },
  { id: 'fuji-velvia100', label: 'FUJIFILM VELVIA 100' },
  { id: 'fuji-superia400', label: 'FUJIFILM SUPERIA X-TRA 400' },
  { id: 'fuji-acros100', label: 'FUJIFILM NEOPAN 100 ACROS' },
  { id: 'fuji-neopan400', label: 'FUJIFILM NEOPAN 400' },
  { id: 'ilford-hp5', label: 'ILFORD HP5 PLUS 400' },
  { id: 'ilford-fp4', label: 'ILFORD FP4 PLUS 125' },
  { id: 'ilford-delta100', label: 'ILFORD DELTA 100' },
  { id: 'ilford-delta400', label: 'ILFORD DELTA 400' },
  { id: 'ilford-delta3200', label: 'ILFORD DELTA 3200' },
  { id: 'ilford-panf', label: 'ILFORD PAN F PLUS 50' },
  { id: 'lomo100', label: 'LOMOGRAPHY 100' },
  { id: 'lomo400', label: 'LOMOGRAPHY 400' },
  { id: 'lomo800', label: 'LOMOGRAPHY 800' },
  { id: 'lomo-metropolis', label: 'LOMOGRAPHY METROPOLIS' },
  { id: 'lomo-purple', label: 'LOMOGRAPHY PURPLE' },
  { id: 'cinestill-50d', label: 'CINESTILL 50D' },
  { id: 'cinestill-800t', label: 'CINESTILL 800T' },
  { id: 'cinestill-bwxx', label: 'CINESTILL BWXX' },
  { id: 'rollei-infrared', label: 'ROLLEI INFRARED 400' },
  { id: 'rollei-retro400s', label: 'ROLLEI RETRO 400S' },
];

function brandLabel(id) { const b = cameraBrands.find(x => x.id === id); return b ? b.label : id; }
function brandEnLabel(id) { const l = brandLabel(id); return l ? l.split(/[\s一-鿿]+/)[0] || l : l; }
function filmLabel(id) { const f = filmStocks.find(x => x.id === id); return f ? f.label : id; }

const placeholderSvg = "data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 4 3'%3E%3Crect width='4' height='3' fill='%23f0f0f0'/%3E%3Ccircle cx='2' cy='1.2' r='0.4' fill='%23ddd'/%3E%3Cpath d='M0.5 2.8 L1.5 1.5 L2.5 2.2 L3.5 1 L4 2.8 Z' fill='%23ddd'/%3E%3C/svg%3E";

function onImgError(e) { e.target.src = placeholderSvg; }

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
  else hasImg.sort((a, b) => (sortKey(b) || '').localeCompare(sortKey(a) || ''));
  return [...hasImg, ...empty];
}

async function loadGroups() {
  try {
    const groups = await getGroups();
    if (groups && groups.length) {
      const pref = localStorage.getItem('sortPreference') || 'time-desc';
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
