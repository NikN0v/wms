<script setup lang="ts">
defineProps<{ eyebrow?: string; title: string; description?: string }>()
</script>

<template>
  <header class="page-heading">
    <div>
      <p v-if="eyebrow" class="page-heading__eyebrow">{{ eyebrow }}</p>
      <h1 class="page-heading__title">{{ title }}</h1>
      <p v-if="description" class="page-heading__description">{{ description }}</p>
    </div>
    <div v-if="$slots.actions" class="page-heading__actions"><slot name="actions" /></div>
  </header>
</template>

<style scoped>
.page-heading { display: flex; align-items: flex-end; justify-content: space-between; gap: 24px; margin-bottom: 28px; }
.page-heading__eyebrow { margin: 0 0 8px; color: var(--color-brand-600); font-size: 11px; font-weight: 750; letter-spacing: .12em; text-transform: uppercase; }
.page-heading__title { margin: 0; color: var(--color-ink-950); font-size: clamp(26px, 3vw, 34px); font-weight: 720; letter-spacing: -.045em; line-height: 1.1; }
.page-heading__description { max-width: 640px; margin: 10px 0 0; color: var(--color-ink-500); font-size: 14px; line-height: 1.6; }
.page-heading__actions { display: flex; align-items: center; gap: 10px; }
@media (max-width: 640px) { .page-heading { align-items: flex-start; flex-direction: column; margin-bottom: 22px; } }
</style>
