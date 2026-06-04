<template>
  <div class="custom-select" ref="wrapper" @click="toggle">
    <div class="cs-trigger" :class="{ open: open, disabled: props.disabled }">
      <!-- <span class="cs-logo-wrap">
        <img v-if="selected" :src="selected.logo || fallbackSvg(selected.label)" alt="" class="cs-logo" @error="onImgError($event, selected.label)" />
      </span> -->
      <span>{{ selected ? selected.label : placeholder }}</span>
      <span class="cs-arrow">
        <svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round">
          <polyline points="6 9 12 15 18 9"/>
        </svg>
      </span>
    </div>
    <div class="cs-dropdown" v-if="open">
      <div
        class="cs-item"
        v-for="item in items"
        :key="item.id"
        :class="{ active: modelValue === item.id }"
        @click.stop="choose(item)"
      >
        <!-- <span class="cs-logo-wrap">
          <img :src="item.logo || fallbackSvg(item.label)" alt="" class="cs-logo" @error="onImgError($event, item.label)" />
        </span> -->
        <span>{{ item.label }}</span>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed, onMounted, onUnmounted, ref } from 'vue';

const props = defineProps({ items: Array, modelValue: String, placeholder: { type: String, default: '— 请选择 —' }, brandColors: { type: Object, default: () => ({}) }, disabled: { type: Boolean, default: false } });
const emit = defineEmits(['update:modelValue']);

const open = ref(false);
const wrapper = ref(null);

function fallbackSvg(label) {
  const letter = (label || '?')[0];
  const color = props.brandColors?.[letter.toLowerCase()] || '#999';
  return `data:image/svg+xml,%3Csvg xmlns='http://www.w3.org/2000/svg' viewBox='0 0 24 24'%3E%3Ccircle cx='12' cy='12' r='12' fill='%23${color}'/%3E%3Ctext x='12' y='16' text-anchor='middle' fill='%23fff' font-size='13' font-weight='bold'%3E${letter}%3C/text%3E%3C/svg%3E`;
}

const selected = computed(() => props.items?.find(i => i.id === props.modelValue));

function toggle() { if (props.disabled) return; open.value = !open.value; }
function choose(item) { emit('update:modelValue', item.id); open.value = false; }
function onImgError(e, label) { e.target.src = fallbackSvg(label); }

function onClickOutside(e) {
  if (wrapper.value && !wrapper.value.contains(e.target)) open.value = false;
}
onMounted(() => document.addEventListener('click', onClickOutside));
onUnmounted(() => document.removeEventListener('click', onClickOutside));
</script>

<style scoped>
.custom-select { position: relative; flex: 1; min-width: 140px; user-select: none; }
.cs-trigger {
  display: flex; align-items: center; gap: 6px;
  padding: 0.85rem 1rem; border: 1px solid #d9d9d9; font-size: 1rem;
  background: #fff; cursor: pointer;
  box-sizing: border-box;
}
.cs-trigger.open { border-color: #aaa; }
.cs-trigger.disabled { opacity: 0.4; cursor: not-allowed; background: #f5f5f5; }
.cs-arrow { margin-left: auto; color: #999; font-size: 0.8rem; }
.cs-dropdown {
  position: absolute; top: 100%; left: 0; right: 0; z-index: 50;
  border: 1px solid #d9d9d9; border-top: none;
  background: #fff; max-height: 240px; overflow-y: auto;
}
.cs-item {
  display: flex; align-items: center; gap: 6px;
  padding: 0.6rem 0.9rem; cursor: pointer; font-size: 0.9rem;
}
.cs-item:hover { background: #f5f5f5; }
.cs-item.active { background: #eee; font-weight: 600; }
.cs-logo-wrap { width: 20px; height: 20px; flex-shrink: 0; display: grid; place-items: center; }
.cs-logo { height: 1.5em; width: auto; display: block; }
</style>
