<template>
  <section class="gallery-view" @wheel="onWheel">
    <div class="gallery-header">
      <div class="gallery-top">
        <button class="back-button" @click="goBack"><<</button>
        <!-- 管理员工具栏 -->
        <div class="admin-tools" v-if="isAdmin">
          <button class="admin-icon-btn" data-tip="上传图片" @click="showUpload = !showUpload">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
              <rect x="3" y="3" width="7" height="7" rx="1"/>
              <rect x="14" y="3" width="7" height="7" rx="1"/>
              <rect x="3" y="14" width="7" height="7" rx="1"/>
              <rect x="14" y="14" width="7" height="7" rx="1"/>
            </svg>
          </button>
          <button class="admin-icon-btn icon-danger" data-tip="删除当前相册" @click="handleDeleteGroup">
            <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
              <polyline points="3 6 5 6 21 6"/>
              <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
            </svg>
          </button>
        </div>
      </div>
      <!-- 管理员上传面板 -->
      <div class="admin-upload-bar" v-if="isAdmin && showUpload">
        <button class="up-btn" :disabled="uploading" @click="triggerFileInput">
          <svg v-if="uploading" class="btn-spinner" viewBox="0 0 24 24" width="14" height="14">
            <circle cx="12" cy="12" r="10" stroke="rgba(255,255,255,0.3)" stroke-width="3" fill="none"/>
            <path d="M12 2a10 10 0 0 1 10 10" stroke="#fff" stroke-width="3" fill="none" stroke-linecap="round"/>
          </svg>
          {{ uploading ? '上传中...' : '上传文件' }}
        </button>
        <!-- 每个文件进度条 -->
        <div class="upload-items" v-if="uploadItems.length">
          <div class="upload-item" v-for="item in uploadItems" :key="item.id">
            <svg class="upload-spinner" v-if="item.status === 'uploading'" viewBox="0 0 24 24" width="14" height="14">
              <circle cx="12" cy="12" r="10" stroke="#ddd" stroke-width="3" fill="none"/>
              <path d="M12 2a10 10 0 0 1 10 10" stroke="#111" stroke-width="3" fill="none" stroke-linecap="round"/>
            </svg>
            <svg class="upload-done" v-else-if="item.status === 'done'" viewBox="0 0 24 24" width="14" height="14">
              <circle cx="12" cy="12" r="10" fill="#111"/>
              <path d="M8 12l3 3 5-5" stroke="#fff" stroke-width="2" fill="none" stroke-linecap="round"/>
            </svg>
            <span class="upload-item-name">{{ item.name }}</span>
            <div class="upload-item-bar">
              <div class="upload-item-fill" :style="{ width: item.percent + '%' }"></div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="image-grid" :class="{ blurred: overlayOpen }">
      <article
        class="image-card"
        v-for="(image, index) in currentGroup.images"
        :key="image"
        @click="openImage(index)">
        <img :src="image" :alt="currentGroup.title + ' ' + (index + 1)" />
        <button v-if="isAdmin" class="img-del-btn" @click.stop="handleDeleteImage(image)" title="删除此图片">×</button>
      </article>
    </div>

    <div v-if="overlayOpen" class="overlay" @click.self="closeOverlay">
      <div class="overlay-content">
        <button class="nav-button left" @click.stop="changePage(-1)" :disabled="!hasPrev">◀</button>
        <button class="nav-button right" @click.stop="changePage(1)" :disabled="!hasNext">▶</button>
        <button class="close-button" @click="closeOverlay">×</button>
        <img :src="currentImage" :alt="currentTitle" />
        <div class="overlay-footer">
          <span>{{ currentGroup.title }} · {{ currentIndex + 1 }} / {{ currentGroup.images.length }}</span>
        </div>
      </div>
    </div>
  </section>
</template>

<script setup>
import { computed, onMounted, reactive, ref, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import {
  getGroups, getImages,
  deleteGroup as deleteGroupApi,
  deleteImage as deleteImageApi,
  uploadImage as uploadImageApi
} from '../api/gallery';

const route = useRoute();
const router = useRouter();
const groups = ref([]);
const currentGroupId = ref(route.params.groupId || '');
const selectedIndex = ref(null);

const currentGroup = computed(() => {
  return groups.value.find((item) => item.id === currentGroupId.value) || { title: '', description: '', images: [] };
});


const overlayOpen = computed(() => selectedIndex.value !== null);
const currentImage = computed(() => (selectedIndex.value !== null ? currentGroup.value.images[selectedIndex.value] : ''));
const currentIndex = computed(() => selectedIndex.value ?? 0);
const currentTitle = computed(() => `${currentGroup.value.title} ${selectedIndex.value !== null ? selectedIndex.value + 1 : ''}`);
const hasPrev = computed(() => selectedIndex.value !== null && (selectedIndex.value > 0 || groups.value.findIndex((item) => item.id === currentGroupId.value) > 0));
const hasNext = computed(() => selectedIndex.value !== null && (selectedIndex.value < currentGroup.value.images.length - 1 || groups.value.findIndex((item) => item.id === currentGroupId.value) < groups.value.length - 1));

// --- 管理员状态 ---
const isAdmin = computed(() => {
  try {
    const raw = localStorage.getItem('user');
    return raw ? JSON.parse(raw).role === 'admin' : false;
  } catch { return false; }
});
const showUpload = ref(false);
const uploadFile = ref(null);
const ALLOWED_MAGIC = [
  { magic: [0xFF, 0xD8, 0xFF], name: 'JPEG' },
  { magic: [0x89, 0x50, 0x4E, 0x47], name: 'PNG' },
  { magic: [0x42, 0x4D], name: 'BMP' },
  { magic: [0x49, 0x49, 0x2A, 0x00], name: 'TIFF' },
  { magic: [0x4D, 0x4D, 0x00, 0x2A], name: 'TIFF' },
];

function readFileHeader(file, len) {
  return new Promise(r => {
    const fr = new FileReader();
    fr.onload = () => r(Array.from(new Uint8Array(fr.result)));
    fr.onerror = () => r([]);
    fr.readAsArrayBuffer(file.slice(0, len));
  });
}

async function validateImageFile(file) {
  if (file.size > 20 * 1024 * 1024)
    return { ok: false, msg: `文件大小超过 20MB: ${file.name}` };
  const header = await readFileHeader(file, 8);
  const match = ALLOWED_MAGIC.some(t => t.magic.every((b, i) => header[i] === b));
  if (!match)
    return { ok: false, msg: `不支持的文件格式（仅 JPEG/PNG/BMP/TIFF）: ${file.name}` };
  return { ok: true };
}

const uploading = ref(false);
const uploadItems = ref([]);
let uploadId = 0;
const CONCURRENCY = 3;

async function handleDeleteGroup() {
  if (!confirm('确定删除此分类及其所有图片？此操作不可恢复！')) return;
  try {
    await deleteGroupApi(currentGroupId.value);
    router.push('/home');
  } catch (e) {
    console.error('删除失败', e);
    alert('删除失败');
  }
}

async function uploadFiles(groupId, files) {
  const arr = Array.from(files);
  for (const f of arr) {
    const v = await validateImageFile(f);
    if (!v.ok) { alert(v.msg); return; }
  }
  const items = arr.map(f => ({ id: ++uploadId, name: f.name, status: 'pending', percent: 0 }));
  uploadItems.value = items;
  uploading.value = true;

  const pool = async (file, item) => {
    item.status = 'uploading';
    item.percent = 10;
    try {
      const res = await uploadImageApi(groupId, file);
      if (res && res.message !== '上传成功') throw new Error(res.message || '上传失败');
      item.status = 'done';
      item.percent = 100;
    } catch (e) {
      item.status = 'error';
      console.error('上传失败:', file.name, e);
    }
  };

  for (let i = 0; i < arr.length; i += CONCURRENCY) {
    const batch = arr.slice(i, i + CONCURRENCY);
    const batchItems = items.slice(i, i + CONCURRENCY);
    await Promise.allSettled(batch.map((f, idx) => pool(f, batchItems[idx])));
  }

  uploading.value = false;
  setTimeout(() => { uploadItems.value = []; }, 1500);
  await loadImages(groupId);
  const ok = items.filter(it => it.status === 'done').length;
  if (ok > 0) alert(`已上传 ${ok}/${arr.length} 张图片`);
}

function triggerFileInput() {
  if (uploading.value) return;
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.multiple = true;
  input.onchange = (e) => {
    const gid = currentGroupId.value;
    if (!gid || !e.target.files?.length) return;
    uploadFiles(gid, e.target.files);
  };
  input.click();
}

async function handleDeleteImage(imgUrl) {
  if (!confirm('确定删除此图片？')) return;
  const fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1);
  try {
    await deleteImageApi(currentGroupId.value, fileName);
    await loadImages(currentGroupId.value);
  } catch (e) {
    console.error('删除图片失败', e);
    alert('删除图片失败');
  }
}

async function loadGroups() {
  try {
    const list = await getGroups();
    groups.value = list;
  } catch (error) {
    console.warn('无法加载后台分组', error);
  }
  if (!currentGroupId.value && groups.value.length) {
    currentGroupId.value = groups.value[0].id;
  }
  if (currentGroupId.value) {
    await loadImages(currentGroupId.value);
  }
}

async function loadImages(groupId) {
  const found = groups.value.find((item) => item.id === groupId);
  if (!found) {
    router.push('/home');
    return;
  }
  try {
    found.images = await getImages(groupId);
  } catch (error) {
    console.warn('加载图片失败', error);
    found.images = [];
  }
}

function goBack() {
  router.push('/home');
}

function openImage(index) {
  selectedIndex.value = index;
}

function closeOverlay() {
  selectedIndex.value = null;
}

async function changePage(delta) {
  if (selectedIndex.value === null) return;
  const currentIndexInGroups = groups.value.findIndex((item) => item.id === currentGroupId.value);

  if (delta > 0) {
    if (selectedIndex.value < currentGroup.value.images.length - 1) {
      selectedIndex.value += 1;
      return;
    }
    if (currentIndexInGroups < groups.value.length - 1) {
      const next = groups.value[currentIndexInGroups + 1];
      await router.push({ name: 'Gallery', params: { groupId: next.id } });
      currentGroupId.value = next.id;
      selectedIndex.value = 0;
      await loadImages(next.id);
    }
  } else {
    if (selectedIndex.value > 0) {
      selectedIndex.value -= 1;
      return;
    }
    if (currentIndexInGroups > 0) {
      const prev = groups.value[currentIndexInGroups - 1];
      await router.push({ name: 'Gallery', params: { groupId: prev.id } });
      currentGroupId.value = prev.id;
      await loadImages(prev.id);
      selectedIndex.value = groups.value.find((item) => item.id === prev.id).images.length - 1;
    }
  }
}

function onWheel(event) {
  if (!overlayOpen.value) return;
  event.preventDefault();
  changePage(event.deltaY);
}

onMounted(loadGroups);

watch(
  () => route.params.groupId,
  async (value) => {
    if (!value) {
      return;
    }
    currentGroupId.value = value;
    selectedIndex.value = null;
    await loadImages(value);
  }
);
</script>

<style scoped>
.gallery-view {
  min-height: 100vh;
  padding: 2rem 1rem 3rem;
}

.gallery-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  max-width: 1200px;
  margin: 0 auto 1.5rem;
}

.back-button {
  border: none;
  background: transparent;
  color: #bbb;
  font-size: 1.2rem;
  cursor: pointer;
  padding: 0.4rem 0.6rem;
  transition: opacity 0.15s;
}
.back-button:hover {
  opacity: 0.6;
}

.image-grid {
  column-count: 4;
  column-gap: 1.5rem;
  max-width: 1200px;
  margin: 0 auto;
  transition: filter 0.3s ease;
}

.image-grid.blurred {
  filter: blur(9px) brightness(0.8);
  pointer-events: none;
}

.image-card {
  position: relative;
  overflow: hidden;
  cursor: pointer;
  break-inside: avoid;
  margin-bottom: 1.5rem;
  line-height: 0;
}

.image-card img {
  display: block;
  width: 100%;
  height: auto;
  transition: transform 0.25s ease;
}

.image-card:hover img {
  transform: scale(1.04);
}

.overlay {
  position: fixed;
  inset: 0;
  background: rgba(20, 20, 20, 0.82);
  display: grid;
  place-items: center;
  z-index: 20;
}

.overlay-content {
  position: relative;
  width: min(92vw, 1200px);
  max-height: 92vh;
  display: grid;
  gap: 1rem;
}

.nav-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  border: none;
  background: transparent;
  color: #fff;
  font-size: 2rem;
  cursor: pointer;
  opacity: 0.6;
  padding: 0.5rem;
  transition: opacity 0.2s ease;
}

.nav-button:hover {
  opacity: 1;
}

.nav-button.left {
  left: -1rem;
}

.nav-button.right {
  right: -1rem;
}

.nav-button:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

.overlay-content img {
  width: 100%;
  max-height: 84vh;
  object-fit: contain;
}

.close-button {
  position: absolute;
  top: 0;
  right: 0;
  border: none;
  background: transparent;
  color: #fff;
  font-size: 2rem;
  cursor: pointer;
  opacity: 0.6;
  padding: 0.5rem;
  transition: opacity 0.2s ease;
  z-index: 3;
  line-height: 1;
}

.close-button:hover {
  opacity: 1;
}

.overlay-footer {
  display: flex;
  justify-content: space-between;
  color: #f7f7f7;
  font-size: 0.95rem;
  gap: 1rem;
}

/* --- 即时 tooltip --- */
[data-tip] {
  position: relative;
}
[data-tip]:hover::after {
  content: attr(data-tip);
  position: absolute;
  top: 100%;
  left: 50%;
  transform: translateX(-50%);
  white-space: nowrap;
  background: rgba(0,0,0,0.82);
  color: #fff;
  font-size: 0.7rem;
  padding: 3px 8px;
  border-radius: 3px;
  pointer-events: none;
  z-index: 100;
  margin-top: 5px;
  opacity: 1;
  transition: opacity 0.05s;
}
[data-tip]::after {
  opacity: 0;
  transition: opacity 0.05s;
}

/* --- 管理员控件 --- */
.admin-tools {
  display: flex;
  gap: 0.35rem;
  align-items: center;
}

.admin-icon-btn {
  width: 34px;
  height: 34px;
  padding: 0;
  display: grid;
  place-items: center;
  background: transparent;
  color: #bbb;
  border: none;
  cursor: pointer;
  transition: color 0.15s;
}

.admin-icon-btn:hover {
  color: #555;
}

.admin-icon-btn.icon-danger {
  color: #d4a0a0;
}

.admin-icon-btn.icon-danger:hover {
  color: #c0392b;
}

.admin-upload-bar {
  max-width: 1200px;
  margin: 0 auto 1rem;
  display: flex;
  gap: 0.5rem;
  padding: 0.75rem 1rem;
  background: #f9f9f9;
  flex-wrap: wrap;
}

.admin-upload-bar .up-btn {
  padding: 0.5rem 1rem;
  border: none;
  background: #111;
  color: #fff;
  cursor: pointer;
  font-size: 0.85rem;
}

.admin-upload-bar .up-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}

/* --- 上传项列表（每个文件进度条）--- */
.upload-items {
  display: flex; flex-direction: column; gap: 0.3rem;
  width: 100%;
}
.upload-item {
  display: flex; align-items: center; gap: 0.5rem;
  font-size: 0.8rem; color: #444;
}
.upload-item-bar {
  flex: 1; height: 4px;
  background: #eee; border-radius: 2px; overflow: hidden;
}
.upload-item-fill {
  height: 100%; background: #111; border-radius: 2px;
  transition: width 0.3s;
}
.upload-item-name {
  min-width: 50px; max-width: 100px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}
.upload-spinner { animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }
.btn-spinner { animation: spin 0.8s linear infinite; vertical-align: middle; margin-right: 4px; }

.img-del-btn {
  position: absolute;
  top: 6px;
  right: 6px;
  width: 28px;
  height: 28px;
  padding: 0;
  border: none;
  background: rgba(192, 57, 43, 0.85);
  color: #fff;
  font-size: 1.1rem;
  cursor: pointer;
  display: none;
  place-items: center;
  line-height: 1;
  z-index: 2;
}

.image-card:hover .img-del-btn {
  display: grid;
}
</style>
