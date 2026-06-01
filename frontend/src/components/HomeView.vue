<template>
  <section class="home-view">
    <div class="portfolio-grid">
      <article class="portfolio-card" v-for="item in sections" :key="item.id" @click="goToGallery(item.id)">
        <img :src="item.coverImage || item.image" :alt="item.title" />
        <h2>{{ item.title }}</h2>
        <p>{{ item.description }}</p>
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

async function loadGroups() {
  try {
    const groups = await getGroups();
    if (groups && groups.length) {
      sections.value = groups;
    }
  } catch (error) {
    console.warn('无法加载后台分组', error);
  }
}

function goToGallery(groupId) {
  router.push({ name: 'Gallery', params: { groupId } });
}

onMounted(loadGroups);
</script>

<style scoped>
.home-view {
  display: grid;
  gap: 2rem;
}

.portfolio-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 1.75rem;
}

.portfolio-card {
  border: 1px solid #ececec;
  overflow: hidden;
  transition: transform 0.25s ease, box-shadow 0.25s ease;
  cursor: pointer;
}

.portfolio-card:hover {
  transform: scale(1.03);
  box-shadow: 0 18px 45px rgba(0, 0, 0, 0.08);
}

.portfolio-card img {
  width: 100%;
  display: block;
}

.portfolio-card h2 {
  margin: 1rem;
  font-size: 1.15rem;
}

.portfolio-card p {
  margin: 0 1rem 1.25rem;
  color: #575757;
}
</style>
