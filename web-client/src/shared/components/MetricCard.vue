<script setup lang="ts">
import type { Component } from 'vue'

withDefaults(defineProps<{
  label: string
  value: string
  detail: string
  icon: Component
  tone?: 'blue' | 'green' | 'amber' | 'violet'
}>(), { tone: 'blue' })
</script>

<template>
  <el-card class="metric-card" shadow="never">
    <div class="metric-card__top">
      <span class="metric-card__label">{{ label }}</span>
      <span class="metric-card__icon" :class="`metric-card__icon--${tone}`"><component :is="icon" :size="18" :stroke-width="1.8" /></span>
    </div>
    <div class="metric-card__value">{{ value }}</div>
    <div class="metric-card__detail">{{ detail }}</div>
  </el-card>
</template>

<style scoped>
.metric-card { min-height: 158px; border-radius: var(--radius-xl); }
.metric-card :deep(.el-card__body) { padding: 19px 20px; }
.metric-card__top { display: flex; align-items: center; justify-content: space-between; }
.metric-card__label { color: var(--color-ink-500); font-size: 13px; font-weight: 550; }
.metric-card__icon { display: grid; width: 38px; height: 38px; place-items: center; border-radius: 12px; }
.metric-card__icon--blue { color: var(--color-brand-600); background: var(--color-brand-100); }
.metric-card__icon--green { color: var(--color-success-700); background: var(--color-success-100); }
.metric-card__icon--amber { color: var(--color-warning-600); background: var(--color-warning-100); }
.metric-card__icon--violet { color: #7754c8; background: #f1edff; }
.metric-card__value { margin-top: 17px; color: var(--color-ink-950); font-size: 28px; font-weight: 730; letter-spacing: -.04em; line-height: 1; }
.metric-card__detail { margin-top: 9px; color: var(--color-ink-400); font-size: 12px; }
</style>
