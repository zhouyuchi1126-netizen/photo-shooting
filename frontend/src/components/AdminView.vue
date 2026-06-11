<template>
  <section class="admin-view">
    <!-- 管理员验证码登录 -->
    <div class="auth-view" v-if="!isAdminAuthed">
      <div class="auth-card">
        <h1>管理员登录</h1>
        <p class="auth-desc">验证码将发送到管理员邮箱</p>
        <div class="login-form">
          <button class="submit-btn" :disabled="sending" @click="handleSendCode">
            <span v-if="sending" class="btn-loading">···</span>
            {{ sending ? '发送中...' : '发送验证码' }}
          </button>
          <el-input v-model="adminCode" placeholder="" maxlength="6" class="code-input" />
          <button class="submit-btn" :disabled="adminCode.length !== 6 || verifying" @click="handleVerifyCode">
            <span v-if="verifying" class="btn-loading">···</span>
            {{ verifying ? '验证中...' : '登录' }}
          </button>
        </div>
        <p class="help-text"><router-link to="/home" class="highlight-link">返回首页</router-link></p>
      </div>
    </div>
    <template v-if="isAdminAuthed">
    <div class="admin-header">
      <button class="add-icon-btn" data-tip="创建新分类" @click.stop="showCreateForm = !showCreateForm">
        <svg viewBox="0 0 24 24" width="22" height="22" fill="none" stroke="currentColor" stroke-width="2">
          <line x1="12" y1="5" x2="12" y2="19"/>
          <line x1="5" y1="12" x2="19" y2="12"/>
        </svg>
      </button>
    </div>

    <!-- 创建相册 -->
    <form class="card-section" v-if="showCreateForm" @submit.prevent="handleCreateGroup">
      <h2>创建新相册</h2>
      <div class="form-row">
        <el-input v-model="form.title" placeholder="相册标题" />
      </div>
      <div class="form-row">
        <el-select v-model="form.cameraBrand" placeholder="— 选择相机品牌 —" clearable filterable style="flex:1;min-width:140px">
          <el-option v-for="b in CAMERA_BRANDS" :key="b.id" :label="b.label" :value="b.id" />
        </el-select>
        <el-input v-model="form.cameraModel" placeholder="相机型号" :disabled="!form.cameraBrand" style="flex:1;min-width:140px" />
      </div>
      <div class="form-row">
        <el-select v-model="form.filmStock" placeholder="— 选择胶卷型号 —" clearable filterable :disabled="!form.isFilm" style="flex:2;min-width:140px">
          <el-option v-for="f in FILM_STOCKS" :key="f.id" :label="f.label" :value="f.id" />
        </el-select>
        <el-checkbox v-model="form.isFilm" label="胶片拍摄" />
      </div>
      <button type="submit" class="create-btn">创建</button>
    </form>

    <!-- 分组列表 -->
    <div class="group-table-wrap">
      <h2 class="section-heading">当前分类</h2>
      <div class="group-item" v-for="group in groups" :key="group.id">

        <!-- 基本信息行 -->
        <div class="group-info">
          <div class="group-meta">
            <div class="meta-row">
              <h3
                :contenteditable="editingGroup === group.id && editingField === 'title'"
                @blur="onFieldBlur($event, group.id, 'title')"
                @keydown.enter.prevent="$event.target.blur()"
                :class="{ 'is-editing': editingGroup === group.id && editingField === 'title' }"
              >{{ group.title || group.id }}</h3>
              <button class="icon-edit-inline" data-tip="编辑相册名称" @click="startEdit(group, 'title')">
                <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="1.8">
                  <path d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"/>
                </svg>
              </button>
            </div>
            <span class="image-count" v-if="editingGroup !== group.id">{{ group.imageCount }} 张图片</span>
            <!-- 相机/胶片编辑面板 -->
            <div class="camera-edit-panel" v-if="editingGroup === group.id && editingField === 'camera'">
              <div class="form-row">
                <el-select v-model="editForm.cameraBrand" placeholder="相机品牌" clearable filterable style="flex:1;min-width:130px">
                  <el-option v-for="b in CAMERA_BRANDS" :key="b.id" :label="b.label" :value="b.id" />
                </el-select>
                <el-input v-model="editForm.cameraModel" placeholder="相机型号" style="flex:1;min-width:130px" />
              </div>
              <div class="form-row">
                <el-select v-model="editForm.filmStock" placeholder="胶卷型号" clearable filterable :disabled="!editForm.isFilm" style="flex:2;min-width:130px">
                  <el-option v-for="f in FILM_STOCKS" :key="f.id" :label="f.label" :value="f.id" />
                </el-select>
                <el-checkbox v-model="editForm.isFilm" label="胶片" />
              </div>
            </div>
            <!-- 展示 -->
            <div class="meta-row" v-if="editingGroup !== group.id">
              <div class="exif-display" v-if="group.cameraBrand">
                <span class="exif-part" @click="startEdit(group, 'camera')">
                  <!-- <img v-if="getBrandLogo(group.cameraBrand)" :src="getBrandLogo(group.cameraBrand)" class="exif-logo" /> -->
                  <strong>{{ brandEnLabel(group.cameraBrand) }}</strong>
                </span>
                <span v-if="group.cameraModel" class="exif-part" @click="startEdit(group, 'camera')">{{ group.cameraModel }}</span>
                <span v-if="group.film" class="exif-divider">|</span>
                <span v-if="group.film" class="exif-part exif-film" @click="startEdit(group, 'camera')">{{ filmLabel(group.filmStock) }}</span>
                <button class="icon-inline-edit" @click.stop="startEdit(group, 'camera')" data-tip="编辑相机信息">
                  <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="#bbb" stroke-width="1.8">
                    <path d="M15.232 5.232l3.536 3.536m-2.036-5.036a2.5 2.5 0 113.536 3.536L6.5 21.036H3v-3.572L16.732 3.732z"/>
                  </svg>
                </button>
              </div>
              <button v-else class="add-exif-btn" @click.stop="startEdit(group, 'camera')" data-tip="添加相机信息">
                + 相机信息
              </button>
            </div>
          </div>
          <div class="group-actions">
            <button class="icon-btn" data-tip="上传图片" @click="toggleExpand(group)">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
                <rect x="3" y="3" width="7" height="7" rx="1"/>
                <rect x="14" y="3" width="7" height="7" rx="1"/>
                <rect x="3" y="14" width="7" height="7" rx="1"/>
                <rect x="14" y="14" width="7" height="7" rx="1"/>
              </svg>
            </button>
            <button class="icon-btn icon-danger" data-tip="删除当前相册" @click="handleDeleteGroup(group, group.id)">
              <svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="1.8">
                <polyline points="3 6 5 6 21 6"/>
                <path d="M19 6v14a2 2 0 01-2 2H7a2 2 0 01-2-2V6m3 0V4a2 2 0 012-2h4a2 2 0 012 2v2"/>
              </svg>
            </button>
          </div>
        </div>

        <!-- 预览图（前4张 / 空相册占位图） -->
        <div class="preview-strip" v-if="editingGroup !== group.id && expandedGroupId !== group.id">
          <template v-if="group.previewImages && group.previewImages.length">
            <div
              class="preview-thumb"
              v-for="(img, pi) in group.previewImages.slice(0, 4)"
              :key="pi"
            >
              <img :src="img" :alt="'预览 ' + (pi + 1)" />
            </div>
          </template>
          <div class="preview-thumb placeholder" v-else>
            <svg viewBox="0 0 4 3" width="100%" height="100%" preserveAspectRatio="none">
              <rect width="4" height="3" fill="#f0f0f0"/>
              <circle cx="2" cy="1.2" r="0.4" fill="#ddd"/>
              <path d="M0.5 2.8 L1.5 1.5 L2.5 2.2 L3.5 1 L4 2.8 Z" fill="#ddd"/>
            </svg>
          </div>
        </div>

        <!-- 图片管理（展开后内联显示） -->
        <div class="group-images" v-if="expandedGroupId === group.id">
          <div class="img-action-bar">
            <label class="select-all" v-if="groupImages.length">
              <input type="checkbox" :checked="allSelected" @change="toggleAll" />
              <span>全选</span>
            </label>
            <span class="img-select-info" v-if="selectedUrls.size">已选 {{ selectedUrls.size }} 张</span>
            <button class="img-action-btn danger-btn" :disabled="!selectedUrls.size" @click="handleBatchDelete(group.id)">
              删除选中
            </button>
            <span class="img-hint">拖拽图片调整顺序 · 单击设封面 · 勾选删除</span>
          </div>
          <div class="img-grid" v-if="groupImages.length">
            <div
              class="img-item"
              v-for="(imgUrl, idx) in groupImages"
              :key="imgUrl"
              draggable="true"
              :class="{
                'is-cover': isCoverImage(group, imgUrl),
                'is-selected': selectedUrls.has(imgUrl),
                'drag-over': dragOverIdx === idx
              }"
              @dragstart="onDragStart(idx, $event)"
              @dragover.prevent="onDragOver(idx)"
              @dragleave="dragOverIdx = -1"
              @drop="onDrop(idx)"
            >
              <img :src="imgUrl" alt="图片" @click="handleSetCover(group.id, imgUrl)" />
              <div class="cover-overlay" @click="handleSetCover(group.id, imgUrl)">
                <span>设为封面</span>
              </div>
              <div class="cover-badge" v-if="isCoverImage(group, imgUrl)">封面</div>
              <label class="img-checkbox" @click.stop>
                <input type="checkbox" :checked="selectedUrls.has(imgUrl)" @change="toggleSelect(imgUrl)" />
                <span class="checkmark"></span>
              </label>
            </div>
          </div>
          <div class="img-empty" v-else-if="!uploadItems.length">暂无图片</div>

          <!-- 上传中（含每个文件的进度条） -->
          <div class="upload-items" v-if="uploadItems.length">
            <div
              class="upload-item"
              v-for="item in uploadItems"
              :key="item.id"
            >
              <svg class="upload-spinner" v-if="item.status === 'uploading'" viewBox="0 0 24 24" width="16" height="16">
                <circle cx="12" cy="12" r="10" stroke="#ddd" stroke-width="3" fill="none"/>
                <path d="M12 2a10 10 0 0 1 10 10" stroke="#111" stroke-width="3" fill="none" stroke-linecap="round"/>
              </svg>
              <svg class="upload-done" v-else-if="item.status === 'done'" viewBox="0 0 24 24" width="16" height="16">
                <circle cx="12" cy="12" r="10" fill="#111"/>
                <path d="M8 12l3 3 5-5" stroke="#fff" stroke-width="2" fill="none" stroke-linecap="round"/>
              </svg>
              <svg class="upload-error" v-else-if="item.status === 'error'" viewBox="0 0 24 24" width="16" height="16">
                <circle cx="12" cy="12" r="10" fill="#c0392b"/>
                <line x1="8" y1="8" x2="16" y2="16" stroke="#fff" stroke-width="2"/>
                <line x1="16" y1="8" x2="8" y2="16" stroke="#fff" stroke-width="2"/>
              </svg>
              <span class="upload-item-name">{{ item.name }}</span>
              <div class="upload-item-bar">
                <div class="upload-item-fill" :style="{ width: item.percent + '%' }"></div>
              </div>
            </div>
          </div>

          <!-- 上传 -->
          <div class="img-upload-row">
            <button class="img-action-btn" :disabled="uploading" @click="triggerFileInput">
              <svg v-if="uploading" class="btn-spinner" viewBox="0 0 24 24" width="14" height="14">
                <circle cx="12" cy="12" r="10" stroke="rgba(255,255,255,0.3)" stroke-width="3" fill="none"/>
                <path d="M12 2a10 10 0 0 1 10 10" stroke="#fff" stroke-width="3" fill="none" stroke-linecap="round"/>
              </svg>
              {{ uploading ? '上传中...' : '上传文件' }}
            </button>
            <button class="img-action-btn close-btn" @click="collapseGroup">保存</button>
          </div>
      </div>
    </div>
    </div>
    </template>

  </section>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, reactive, ref } from 'vue';
import {
  createGroup as createGroupApi,
  getGroups,
  getImages,
  uploadImage as uploadImageApi,
  updateGroup as updateGroupApi,
  deleteGroup as deleteGroupApi,
  deleteImage as deleteImageApi,
  setCover as setCoverApi,
  reorderImages as reorderImagesApi
} from '../api/gallery';
import { sendAdminCode, verifyAdminCode } from '../api/auth';
import {
  CAMERA_BRANDS, FILM_STOCKS, BRAND_COLOR_MAP,
  brandLabel, brandEnLabel, filmLabel, getBrandLogo,
  validateImageFile
} from '../constants';
import { ElMessage, ElMessageBox } from 'element-plus';

const groups = ref([]);
const editingGroup = ref(null);
const editingField = ref(null);

const adminAuthKey = ref(0);
const isAdminAuthed = computed(() => {
  adminAuthKey.value; // force reactivity on localStorage changes
  try {
    const u = JSON.parse(localStorage.getItem('user'));
    return u?.role === 'admin';
  } catch { return false; }
});

const sending = ref(false);
const verifying = ref(false);
const adminCode = ref('');

async function handleSendCode() {
  sending.value = true;
  try {
    const res = await sendAdminCode();
    if (res.success) {
      ElMessage.success(res.message);
    } else {
      ElMessage.error(res.message);
    }
  } catch { ElMessage.error('发送失败，请检查服务器配置'); }
  sending.value = false;
}

async function handleVerifyCode() {
  if (adminCode.value.length !== 6) return;
  verifying.value = true;
  try {
    const res = await verifyAdminCode(adminCode.value);
    if (res.success) {
      localStorage.setItem('user', JSON.stringify(res));
      adminAuthKey.value++;
      ElMessage.success('登录成功');
      await loadGroups();
    } else {
      ElMessage.error(res.message);
    }
  } catch { ElMessage.error('验证失败'); }
  verifying.value = false;
}

const form = reactive({ title: '', cameraBrand: '', cameraModel: '', isFilm: false, filmStock: '' });
const editForm = reactive({ title: '', cameraBrand: '', cameraModel: '', filmStock: '', isFilm: false });

function showToast(msg, duration = 4000) {
  ElMessage({ message: msg, duration });
}

const showCreateForm = ref(false);

/* ---- 展开的图片管理 ---- */
const expandedGroupId = ref(null);
const groupImages = ref([]);
const selectedUrls = ref(new Set());

/* ---- 并发上传 + 每个文件进度条 ---- */
const uploading = ref(false);
const uploadItems = ref([]);
let uploadId = 0;
const CONCURRENCY = 3;

/* ---- 全选 ---- */
const allSelected = computed(() => groupImages.value.length > 0 && selectedUrls.value.size === groupImages.value.length);
function toggleAll() {
  selectedUrls.value = allSelected.value ? new Set() : new Set(groupImages.value);
}

/* ---- 拖拽排序（放下即保存） ---- */
const dragFromIdx = ref(-1);
const dragOverIdx = ref(-1);

function onDragStart(idx, e) {
  dragFromIdx.value = idx;
  e.dataTransfer.effectAllowed = 'move';
}

function onDragOver(idx) {
  dragOverIdx.value = idx;
}

async function onDrop(idx) {
  dragOverIdx.value = -1;
  if (dragFromIdx.value === idx) return;
  const arr = [...groupImages.value];
  const [moved] = arr.splice(dragFromIdx.value, 1);
  arr.splice(idx, 0, moved);
  groupImages.value = arr;
  // 保存新顺序
  const filenames = arr.map(u => u.substring(u.lastIndexOf('/') + 1));
  try {
    await reorderImagesApi(expandedGroupId.value, filenames);
  } catch (e) {
    console.error('排序保存失败', e);
  }
}

async function uploadFiles(groupId, files) {
  const arr = Array.from(files);

  // 逐个校验
  for (const f of arr) {
    const v = await validateImageFile(f);
    if (!v.ok) { showToast(v.msg); return; }
  }

  const items = arr.map(f => ({ id: ++uploadId, name: f.name, status: 'pending', percent: 0 }));
  uploadItems.value = items;
  uploading.value = true;

  // 并发上传（最多 CONCURRENCY 个同时进行）
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
      item.percent = 0;
      console.error('上传失败:', file.name, e);
    }
  };

  // 按 CONCURRENCY 分批
  for (let i = 0; i < arr.length; i += CONCURRENCY) {
    const batch = arr.slice(i, i + CONCURRENCY);
    const batchItems = items.slice(i, i + CONCURRENCY);
    await Promise.allSettled(batch.map((f, idx) => pool(f, batchItems[idx])));
  }

  uploading.value = false;
  // 延迟清除上传列表以便用户看到完成状态
  setTimeout(() => { uploadItems.value = []; }, 1500);

  // 刷新
  groupImages.value = await getImages(groupId);
  await loadGroups();
  const ok = items.filter(it => it.status === 'done').length;
  showToast(`已上传 ${ok}/${arr.length} 张图片`);
}

function triggerFileInput() {
  if (uploading.value) return;
  const input = document.createElement('input');
  input.type = 'file';
  input.accept = 'image/*';
  input.multiple = true;
  input.onchange = (e) => {
    const gid = expandedGroupId.value;
    if (!gid || !e.target.files?.length) return;
    uploadFiles(gid, e.target.files);
  };
  input.click();
}

/* ---- 自动保存 debounce ---- */
let saveTimer = null;
function scheduleSave() {
  if (saveTimer) clearTimeout(saveTimer);
  saveTimer = setTimeout(() => {
    if (editingGroup.value) handleUpdateGroup(editingGroup.value);
  }, 400);
}

function onFieldBlur(e, groupId, field) {
  if (field === 'title') editForm.title = e.target.innerText.trim();
  else if (field === 'cameraModel') editForm.cameraModel = e.target.value.trim();
  if (editingGroup.value === groupId) scheduleSave();
}

async function finishEdit(groupId) {
  if (saveTimer) clearTimeout(saveTimer);
  await handleUpdateGroup(groupId);
}

/* ---- 点击外侧关闭创建表单 + 自动保存 ---- */
function onDocumentClick(e) {
  // 点击 icon 按钮时不触发保存（避免编辑刚打开就保存）
  if (e.target.closest('.icon-edit-inline, .add-icon-btn, .img-checkbox, .cover-overlay, .img-item, .camera-edit-panel, .el-select, .el-checkbox, .el-button')) return;

  if (showCreateForm.value) {
    const wrap = document.querySelector('.card-section');
    const btn = document.querySelector('.add-icon-btn');
    if (wrap && !wrap.contains(e.target) && btn && !btn.contains(e.target)) {
      showCreateForm.value = false;
    }
  }

  if (saveTimer) { clearTimeout(saveTimer); saveTimer = null; }
  if (editingGroup.value) handleUpdateGroup(editingGroup.value);
}

/* ---- 数据加载 ---- */
async function loadGroups() {
  try {
    groups.value = await getGroups();
  } catch (error) {
    showToast('无法加载分组列表');
  }
}

/* ---- 创建分类 ---- */
function generateGroupId(title) {
  const base = title.trim().toLowerCase()
    .replace(/[^a-z0-9一-鿿]+/g, '-')
    .replace(/^-+|-+$/g, '')
    .substring(0, 30);
  return base || 'group-' + Date.now();
}

async function handleCreateGroup() {
  if (!form.title) { showToast('请输入相册标题'); return; }
  try {
    await createGroupApi({
      groupId: generateGroupId(form.title),
      title: form.title.trim(),
      cameraBrand: form.cameraBrand,
      cameraModel: form.cameraModel,
      isFilm: form.isFilm ? 'true' : 'false',
      filmStock: form.filmStock
    });
    showToast('相册创建成功');
    form.title = '';
    form.cameraBrand = '';
    form.cameraModel = '';
    form.isFilm = false;
    form.filmStock = '';
    showCreateForm.value = false;
    await loadGroups();
  } catch (error) {
    showToast('创建相册失败');
    console.error(error);
  }
}

/* ---- 编辑分类 ---- */
function startEdit(group, field) {
  editingGroup.value = group.id;
  editingField.value = field;
  editForm.title = group.title || '';
  editForm.cameraBrand = group.cameraBrand || '';
  editForm.cameraModel = group.cameraModel || '';
  editForm.filmStock = group.filmStock || '';
  editForm.isFilm = group.film || false;
  nextTick(() => {
    const sel = `.group-item [contenteditable="true"]`;
    const el = document.querySelector(sel);
    if (!el) return;
    el.focus();
    // 定位光标到文本末尾
    const range = document.createRange();
    range.selectNodeContents(el);
    range.collapse(false);
    const sel2 = window.getSelection();
    if (sel2) {
      sel2.removeAllRanges();
      sel2.addRange(range);
    }
  });
}

async function handleUpdateGroup(groupId) {
  try {
    await updateGroupApi(groupId, {
      title: editForm.title,
      cameraBrand: editForm.cameraBrand || null,
      cameraModel: editForm.cameraModel || null,
      isFilm: editForm.isFilm ? 'true' : 'false',
      filmStock: editForm.filmStock || null
    });
    editingGroup.value = null;
    editingField.value = null;
    await loadGroups();
  } catch (error) {
    console.error('更新失败', error);
  }
}

/* ---- 删除分类 ---- */
async function handleDeleteGroup(group, groupId) {
  const name = group?.title || groupId;
  try {
    await ElMessageBox.confirm(`确定删除「${name}」及其所有图片？此操作不可恢复。`, '确认删除', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    });
  } catch { return; }
  try {
    await deleteGroupApi(groupId);
    showToast(`相册「${name}」已删除`);
    if (expandedGroupId.value === groupId) collapseGroup();
    await loadGroups();
  } catch (error) {
    showToast('删除失败');
    console.error(error);
  }
}

/* ---- 展开/收缩图片管理 ---- */
async function toggleExpand(group) {
  if (expandedGroupId.value === group.id) {
    expandedGroupId.value = null;
    groupImages.value = [];
    selectedUrls.value = new Set();
    return;
  }
  expandedGroupId.value = group.id;
  selectedUrls.value = new Set();
  try {
    groupImages.value = await getImages(group.id);
  } catch {
    groupImages.value = [];
  }
}

function collapseGroup() {
  expandedGroupId.value = null;
  groupImages.value = [];
  selectedUrls.value = new Set();
}

function isCoverImage(group, imgUrl) {
  return group.coverImage === imgUrl;
}

/* ---- 设为封面 ---- */
async function handleSetCover(groupId, imgUrl) {
  const fileName = imgUrl.substring(imgUrl.lastIndexOf('/') + 1);
  if (!fileName) return;
  try {
    await setCoverApi(groupId, fileName);
    await loadGroups(); // 刷新预览缩略图顺序
    if (expandedGroupId.value === groupId) {
      groupImages.value = await getImages(groupId);
    }
  } catch (error) {
    showToast('设置封面失败');
    console.error(error);
  }
}

/* ---- 多选 ---- */
function toggleSelect(imgUrl) {
  const next = new Set(selectedUrls.value);
  if (next.has(imgUrl)) next.delete(imgUrl);
  else next.add(imgUrl);
  selectedUrls.value = next;
}

/* ---- 批量删除 ---- */
async function handleBatchDelete(groupId) {
  if (!selectedUrls.value.size) return;
  try {
    await ElMessageBox.confirm(`确定删除选中的 ${selectedUrls.value.size} 张图片？`, '确认删除', {
      confirmButtonText: '删除', cancelButtonText: '取消', type: 'warning'
    });
  } catch { return; }
  for (const url of selectedUrls.value) {
    const fileName = url.substring(url.lastIndexOf('/') + 1);
    try {
      await deleteImageApi(groupId, fileName);
    } catch (e) {
      console.error('删除失败', fileName, e);
    }
  }
  selectedUrls.value = new Set();
  try {
    groupImages.value = await getImages(groupId);
  } catch {
    groupImages.value = [];
  }
  await loadGroups();
  showToast('删除完成');
}

onMounted(() => {
  loadGroups();
  document.addEventListener('click', onDocumentClick);
});

onUnmounted(() => {
  document.removeEventListener('click', onDocumentClick);
});
</script>

<style scoped>
.admin-view {
  max-width: 980px;
  margin: 0 auto;
  display: grid;
  gap: 1.5rem;
}

/* --- 头部 --- */
.admin-header {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.admin-header h1 {
  margin: 0;
  font-size: clamp(2rem, 3vw, 2.6rem);
}

.add-icon-btn {
  width: 32px; height: 32px; padding: 0;
  display: grid; place-items: center;
  background: transparent; border: none; color: #888;
  cursor: pointer; transition: color 0.15s;
}
.add-icon-btn:hover { color: #333; }

/* --- 卡片区域 --- */
.card-section {
  display: grid; gap: 1rem; padding: 1.5rem;
  border: 1px solid #ececec;
}

.card-section h2 { margin: 0; font-size: 1.15rem; }

.form-row { display: flex; gap: 0.75rem; flex-wrap: wrap; }

.create-btn {
  padding: 0.7rem 1.2rem; border: none; width: auto; align-self: flex-start;
  background: #111; color: #fff; cursor: pointer; font-size: 0.9rem;
}
.create-btn:hover { opacity: 0.85; }

/* --- 分组列表 --- */
.group-table-wrap { display: grid; gap: 0.75rem; }

/* 隐藏文字但保持空间占位 */
.section-heading {
  margin: 0; font-size: 1.15rem;
  visibility: hidden; height: 0; overflow: visible;
}

.group-item { border: 1px solid #ececec; }

/* --- 预览图条 --- */
.preview-strip {
  display: flex; gap: 0.65rem; padding: 0 1rem 0.75rem;
}
.preview-thumb {
  width: 72px; height: 72px; flex-shrink: 0;
  border: 1px solid #eee; overflow: hidden;
}
.preview-thumb img {
  width: 100%; height: 100%; object-fit: cover; display: block;
}

/* --- 基本信息行 --- */
.group-info {
  display: flex; justify-content: space-between; align-items: center;
  gap: 1rem; padding: 0.75rem 1rem;
}

.group-meta {
  flex: 1; min-width: 0;
  display: flex; flex-direction: column;
  gap: 0.2rem;
}

.meta-row {
  display: flex; align-items: center; gap: 0.35rem;
}

.meta-row h3 {
  margin: 0; color: #555; font-size: 1.05rem;
  font-weight: 600; padding: 2px 0;
}

/* 内联编辑图标 */
.icon-edit-inline {
  width: 24px; height: 24px; padding: 0;
  display: grid; place-items: center;
  background: transparent; color: #bbb; border: none;
  cursor: pointer; transition: color 0.15s;
  flex-shrink: 0;
}
.icon-edit-inline:hover { color: #555; }

.meta-row h3.is-editing { color: #000; outline: none; caret-color: #333; }

.image-count { font-size: 0.8rem; color: #aaa; margin-top: 0.1rem; }
.meta-exif { display: flex; gap: 0.4rem; flex-wrap: wrap; margin-top: 0.25rem; }
.exif-display {
  font-size: 0.8rem; color: #555; display: flex; align-items: center; gap: 2px;
  flex-wrap: wrap;
}
.exif-part { cursor: pointer; display: inline-flex; align-items: center; gap: 2px; }
.exif-part:hover strong, .exif-part:hover { color: #333; }
.exif-divider { color: #ccc; margin: 0 3px; }
.exif-film { color: #8a7e5c; }
.icon-inline-edit {
  width: 24px; height: 24px; padding: 0; margin-left: 2px;
  display: grid; place-items: center;
  background: transparent; border: none; cursor: pointer; opacity: 0.4;
  transition: opacity 0.15s;
}
.icon-inline-edit:hover { opacity: 1; }

/* 相机编辑面板 */
.camera-edit-panel {
  display: grid; gap: 0.5rem; padding: 0.5rem 0; width: 100%;
}
.add-exif-btn {
  padding: 2px 10px; font-size: 0.8rem; color: #999;
  background: transparent; border: 1px dashed #d9d9d9; border-radius: 3px;
  cursor: pointer; transition: all 0.15s;
}
.add-exif-btn:hover { color: #333; border-color: #aaa; }

/* --- 图标按钮 --- */
.group-actions {
  display: flex; gap: 0.25rem; flex-shrink: 0; align-items: center;
}
/* 即时 tooltip（替换浏览器原生缓慢的 title） */
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

.icon-btn {
  width: 34px; height: 34px; padding: 0;
  display: grid; place-items: center;
  background: transparent; color: #bbb; border: none;
  cursor: pointer; transition: color 0.15s;
}
.icon-btn:hover { color: #555; }
.icon-btn.icon-danger { color: #d4a0a0; }
.icon-btn.icon-danger:hover { color: #c0392b; }

/* --- 内联图片管理 --- */
.group-images {
  border-top: 1px solid #ececec;
  padding: 0.75rem 1rem;
  display: grid;
  gap: 0.75rem;
}

.img-action-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  flex-wrap: wrap;
}

.img-select-info {
  font-size: 0.85rem;
  color: #333;
  font-weight: 500;
}

.img-hint {
  font-size: 0.8rem;
  color: #bbb;
  margin-right: auto;
}

.img-action-btn {
  padding: 0.4rem 0.8rem;
  font-size: 0.72rem;
  background: #111;
  color: #fff;
  border: none;
  cursor: pointer;
}
.img-action-btn:disabled { opacity: 0.35; cursor: not-allowed; }

.danger-btn { background: #c0392b; }
.danger-btn:disabled { background: #c0392b; opacity: 0.35; }

.close-btn { background: transparent; color: #555; border: 1px solid #d9d9d9; }

.file-count {
  font-size: 0.85rem;
  color: #555;
}

/* 图片网格 */
.img-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(130px, 1fr));
  gap: 0.65rem;
}

.img-item {
  position: relative;
  aspect-ratio: 1;
  overflow: hidden;
  cursor: pointer;
  border: 2px solid transparent;
  transition: border-color 0.15s;
}
.img-item.is-selected { border-color: #111; }
.img-item.drag-over { border-color: #111; border-style: dashed; background: #f5f5f5; }
.img-item img {
  width: 100%; height: 100%;
  object-fit: cover; display: block;
}

/* 封面色罩 */
.cover-overlay {
  position: absolute; inset: 0;
  background: rgba(0,0,0,0.45);
  display: grid; place-items: center;
  opacity: 0; transition: opacity 0.2s;
  z-index: 1;
}
.cover-overlay span {
  color: #fff; font-size: 0.8rem;
  border: 1px solid rgba(255,255,255,0.7);
  padding: 0.3rem 0.5rem;
}
.img-item:hover .cover-overlay { opacity: 1; }

/* 封面标记 */
.cover-badge {
  position: absolute; top: 4px; left: 4px;
  background: #111; color: #fff;
  font-size: 0.7rem; padding: 1px 6px;
  z-index: 2;
}

/* 多选复选框 */
.img-checkbox {
  position: absolute;
  bottom: 4px; right: 4px;
  width: 20px; height: 20px;
  z-index: 3;
  cursor: pointer;
  display: grid; place-items: center;
}
.img-checkbox input { display: none; }
.checkmark {
  width: 18px; height: 18px;
  border: 2px solid rgba(255,255,255,0.85);
  background: rgba(0,0,0,0.3);
  display: grid; place-items: center;
  transition: background 0.15s;
}
.img-checkbox input:checked + .checkmark {
  background: #111;
  border-color: #111;
}
.img-checkbox input:checked + .checkmark::after {
  content: '✓';
  color: #fff;
  font-size: 13px;
  line-height: 1;
}

.img-empty { color: #999; font-size: 0.9rem; padding: 0.5rem 0; }

/* --- 全选 --- */
.select-all {
  display: flex; align-items: center; gap: 0.3rem;
  font-size: 0.85rem; color: #555; cursor: pointer;
}
.select-all input { width: auto; margin: 0; }

/* --- 上传项列表（每个文件进度条）--- */
.upload-items {
  display: grid; gap: 0.35rem; padding: 0.3rem 0;
}
.upload-item {
  display: flex; align-items: center; gap: 0.5rem;
  font-size: 0.82rem; color: #444;
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
  min-width: 60px; max-width: 120px;
  overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
}

/* 上传中旋转图标 */
.upload-spinner { animation: spin 0.8s linear infinite; }
@keyframes spin { to { transform: rotate(360deg); } }

.btn-spinner { animation: spin 0.8s linear infinite; vertical-align: middle; margin-right: 4px; }

.img-upload-row {
  display: flex; gap: 0.75rem; align-items: center;
  padding-top: 0.5rem;
}

/* ========== 响应式 ========== */
@media (max-width: 767px) {
  .admin-view { padding: 0 0.5rem; gap: 1rem; }
  .group-info { flex-direction: column; align-items: flex-start; gap: 0.5rem; }
  .group-actions { align-self: flex-end; }
  .preview-strip { padding: 0 0.75rem 0.75rem; }
  .preview-thumb { width: 56px; height: 56px; }
  .group-images { padding: 0.75rem; }
  .img-grid { grid-template-columns: repeat(auto-fill, minmax(90px, 1fr)); }
  .img-hint { display: none; }
  .card-section { padding: 1rem; }
}

/* --- 管理员登录（与 LoginView 样式一致） --- */
.auth-view { display: grid; place-items: center; min-height: 70vh; }
.auth-card { width: min(420px, 100%); box-sizing: border-box; border: 1px solid #e6e6e6; padding: 2rem; border-radius: 8px; box-shadow: 0 0 24px rgba(0,0,0,0.04); }
.auth-card h1 { margin: 0 0 1rem; font-size: 2rem; text-align: center; }
.auth-desc { margin: 0 0 1rem; color: #555; font-size: 0.9rem; text-align: center; }
.login-form { display: grid; gap: 1rem; }
.login-form .submit-btn {
  width: 100%; padding: 0.85rem 1rem; border: none; border-radius: 6px;
  background: #111; color: white; font-size: 1rem; cursor: pointer;
}
.login-form .submit-btn:hover { opacity: 0.85; }
.login-form .submit-btn:disabled { opacity: 0.4; cursor: not-allowed; }
.btn-loading { margin-right: 4px; }
.code-input :deep(.el-input__inner) { text-align: center; font-size: 1.2rem; letter-spacing: 0.3em; }
.help-text { margin-top: 1rem; text-align: center; font-size: 0.95rem; }
.highlight-link { color: #111; font-weight: 600; border-bottom: 1px solid transparent; transition: border-color 0.15s; }
.highlight-link:hover { border-bottom-color: #111; }
</style>
