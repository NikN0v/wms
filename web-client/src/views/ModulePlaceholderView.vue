<script setup lang="ts">
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { ArrowLeft, Layers3 } from '@lucide/vue'
import PageHeading from '../shared/components/PageHeading.vue'

const route = useRoute()
const title = computed(() => String(route.meta.title ?? 'Раздел'))
const description = computed(() => String(route.meta.description ?? ''))
</script>

<template>
  <PageHeading eyebrow="РАБОЧЕЕ ПРОСТРАНСТВО" :title="title" :description="description" />
  <el-card class="module-card" shadow="never">
    <div class="module-state">
      <div class="module-state__icon"><Layers3 :size="24" :stroke-width="1.7" /></div>
      <span class="module-state__tag">РАЗДЕЛ ПОДГОТОВЛЕН</span>
      <h2>{{ title }}</h2>
      <p>Структура приложения и общие компоненты готовы. Следующим этапом подключим этот раздел к WMS API.</p>
      <RouterLink to="/"><el-button><ArrowLeft :size="15" /> Вернуться к обзору</el-button></RouterLink>
    </div>
  </el-card>
</template>

<style scoped>
.module-card { min-height: 430px; border-radius: var(--radius-xl); }
.module-card :deep(.el-card__body) { display: grid; min-height: 430px; place-items: center; }
.module-state { display: flex; max-width: 420px; flex-direction: column; align-items: center; padding: 30px 20px; text-align: center; }
.module-state__icon { display: grid; width: 62px; height: 62px; margin-bottom: 18px; place-items: center; border: 1px solid #dce7ff; border-radius: 19px; color: var(--color-brand-600); background: var(--color-brand-50); }
.module-state__tag { color: var(--color-brand-600); font-size: 9px; font-weight: 750; letter-spacing: .14em; }
.module-state h2 { margin: 10px 0 8px; color: var(--color-ink-950); font-size: 23px; font-weight: 720; letter-spacing: -.04em; }
.module-state p { margin: 0 0 20px; color: var(--color-ink-500); font-size: 12px; line-height: 1.7; }
.module-state :deep(.el-button) { height: 36px; gap: 7px; border-radius: 9px; color: var(--color-ink-700); font-size: 11px; }
</style>
