<script setup lang="ts">
import { useDateFormat, useNow } from '@vueuse/core'
import { ArrowRight, ArrowUpRight, Boxes, ClipboardList, Package, Plus, Warehouse } from '@lucide/vue'
import { RouterLink } from 'vue-router'
import MetricCard from '../shared/components/MetricCard.vue'
import PageHeading from '../shared/components/PageHeading.vue'
import { notify } from '../shared/feedback/notifications'

const dateLabel = useDateFormat(useNow(), 'dddd, D MMMM', { locales: 'ru-RU' })
const metrics = [
  { label: 'Активные склады', value: '—', detail: 'Подключите API для загрузки данных', icon: Warehouse, tone: 'blue' as const },
  { label: 'Позиции на хранении', value: '—', detail: 'Текущий остаток по всем складам', icon: Boxes, tone: 'green' as const },
  { label: 'Операции сегодня', value: '—', detail: 'Приёмка, перемещения и отгрузки', icon: ClipboardList, tone: 'amber' as const },
  { label: 'Номенклатура', value: '—', detail: 'Товары в каталоге компании', icon: Package, tone: 'violet' as const },
]
</script>

<template>
  <PageHeading :eyebrow="dateLabel" title="Обзор склада" description="Сводная картина операций и запасов вашей компании.">
    <template #actions>
      <el-button type="primary" :icon="Plus" @click="notify.info('Подключение складских операций будет следующим этапом.')">Новая операция</el-button>
    </template>
  </PageHeading>

  <section class="metric-grid" aria-label="Основные показатели">
    <MetricCard v-for="metric in metrics" :key="metric.label" v-bind="metric" />
  </section>

  <section class="dashboard-grid">
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-card__content">
        <div class="welcome-card__copy">
          <span class="welcome-kicker"><span class="welcome-kicker__dot" /> WMS WORKSPACE</span>
          <h2>Порядок на складе<br /><span>начинается здесь.</span></h2>
          <p>Подключите данные компании, чтобы видеть остатки, контролировать операции и управлять командой из одного места.</p>
          <RouterLink to="/warehouses"><el-button type="primary">Перейти к складам <ArrowRight :size="16" /></el-button></RouterLink>
        </div>
        <div class="welcome-visual" aria-hidden="true">
          <div class="visual-orbit visual-orbit--outer" />
          <div class="visual-orbit visual-orbit--inner" />
          <div class="visual-warehouse"><Warehouse :size="58" :stroke-width="1.2" /></div>
          <span class="visual-chip visual-chip--top"><Boxes :size="15" /> Остатки</span>
          <span class="visual-chip visual-chip--bottom"><ClipboardList :size="15" /> Операции</span>
          <span class="visual-spark visual-spark--one" />
          <span class="visual-spark visual-spark--two" />
        </div>
      </div>
      <div class="welcome-card__footer"><span>Ваше рабочее пространство готово</span><span class="footer-check">✓</span></div>
    </el-card>

    <el-card class="activity-card" shadow="never">
      <div class="section-heading">
        <div><h2>Последние операции</h2><p>Активность по всем складам</p></div>
        <RouterLink to="/operations" class="text-link">Все операции <ArrowUpRight :size="15" /></RouterLink>
      </div>
      <div class="activity-empty">
        <div class="activity-empty__icon"><ClipboardList :size="21" /></div>
        <strong>Пока нет операций</strong>
        <span>Здесь появятся последние складские события.</span>
      </div>
    </el-card>
  </section>

  <section class="quick-access">
    <div class="quick-access__heading"><div><h2>Быстрый доступ</h2><p>Основные разделы системы</p></div></div>
    <div class="quick-links">
      <RouterLink to="/inventory" class="quick-link"><span class="quick-link__icon quick-link__icon--blue"><Boxes :size="18" /></span><span><strong>Остатки</strong><small>Просмотр запасов</small></span><ArrowRight :size="16" /></RouterLink>
      <RouterLink to="/products" class="quick-link"><span class="quick-link__icon quick-link__icon--violet"><Package :size="18" /></span><span><strong>Номенклатура</strong><small>Каталог товаров</small></span><ArrowRight :size="16" /></RouterLink>
      <RouterLink to="/team" class="quick-link"><span class="quick-link__icon quick-link__icon--green"><Warehouse :size="18" /></span><span><strong>Команда</strong><small>Пользователи и доступ</small></span><ArrowRight :size="16" /></RouterLink>
    </div>
  </section>
</template>

<style scoped>
.metric-grid { display: grid; grid-template-columns: repeat(4,minmax(0,1fr)); gap: 16px; }
.dashboard-grid { display: grid; grid-template-columns: minmax(0,1.55fr) minmax(300px,1fr); gap: 16px; margin-top: 18px; }
.welcome-card,.activity-card { min-height: 286px; overflow: hidden; border-radius: var(--radius-xl); }
.welcome-card :deep(.el-card__body) { display: flex; min-height: 286px; flex-direction: column; justify-content: space-between; padding: 0; }
.welcome-card__content { position: relative; display: flex; min-height: 238px; align-items: center; justify-content: space-between; overflow: hidden; padding: 27px 30px 18px; background: radial-gradient(ellipse at 82% 45%,#e8f0ff 0,transparent 44%),linear-gradient(110deg,#fff 20%,#f8faff 100%); }
.welcome-card__copy { position: relative; z-index: 2; max-width: 380px; }
.welcome-kicker { display: inline-flex; align-items: center; gap: 7px; color: var(--color-brand-600); font-size: 9px; font-weight: 780; letter-spacing: .14em; }
.welcome-kicker__dot { width: 6px; height: 6px; border-radius: 50%; background: var(--color-brand-600); box-shadow: 0 0 0 3px #dce8ff; }
.welcome-card h2 { margin: 14px 0 10px; color: var(--color-ink-950); font-size: clamp(24px,2.8vw,32px); font-weight: 750; letter-spacing: -.05em; line-height: 1.1; }
.welcome-card h2 span { color: var(--color-brand-600); }
.welcome-card__copy p { max-width: 360px; margin: 0 0 17px; color: var(--color-ink-500); font-size: 12px; line-height: 1.65; }
.welcome-card__copy :deep(.el-button) { height: 37px; border-radius: 9px; font-size: 11px; }
.welcome-card__copy :deep(.el-button .el-icon) { margin-left: 4px; }
.welcome-card__footer { display: flex; min-height: 46px; align-items: center; justify-content: space-between; padding: 0 20px; border-top: 1px solid var(--color-border-subtle); color: var(--color-ink-400); font-size: 10px; }
.footer-check { display: grid; width: 18px; height: 18px; place-items: center; border-radius: 50%; color: var(--color-success-700); background: var(--color-success-100); font-size: 11px; }
.welcome-visual { position: absolute; top: 0; right: 15px; bottom: 0; display: grid; width: 250px; place-items: center; color: #7ba3ec; }
.visual-orbit { position: absolute; border: 1px solid rgb(105 151 231 / 15%); border-radius: 50%; }
.visual-orbit--outer { width: 220px; height: 220px; }
.visual-orbit--inner { width: 160px; height: 160px; border-style: dashed; }
.visual-warehouse { position: relative; display: grid; width: 112px; height: 112px; place-items: center; border: 1px solid #e1eaff; border-radius: 34px; color: var(--color-brand-600); background: linear-gradient(145deg,#fff,#eff5ff); box-shadow: 0 20px 45px rgb(47 98 188 / 13%); transform: rotate(-8deg); }
.visual-warehouse svg { transform: rotate(8deg); }
.visual-chip { position: absolute; display: flex; align-items: center; gap: 6px; padding: 8px 10px; border: 1px solid #e7ebf3; border-radius: 10px; color: var(--color-ink-700); background: #fff; box-shadow: 0 7px 18px rgb(30 50 82 / 8%); font-size: 9px; font-weight: 650; }
.visual-chip--top { top: 54px; right: 0; }
.visual-chip--bottom { bottom: 48px; left: 6px; }
.visual-spark { position: absolute; width: 6px; height: 6px; border-radius: 50%; background: #f3b45c; box-shadow: 0 0 0 5px rgb(243 180 92 / 12%); }
.visual-spark--one { top: 39px; left: 47px; }
.visual-spark--two { right: 35px; bottom: 53px; width: 4px; height: 4px; background: #6c9df3; }
.activity-card :deep(.el-card__body) { height: 100%; padding: 22px; }
.section-heading,.quick-access__heading { display: flex; align-items: flex-start; justify-content: space-between; gap: 12px; }
.section-heading h2,.quick-access h2 { margin: 0; color: var(--color-ink-900); font-size: 14px; font-weight: 700; letter-spacing: -.02em; }
.section-heading p,.quick-access p { margin: 6px 0 0; color: var(--color-ink-400); font-size: 10px; }
.text-link { display: flex; align-items: center; gap: 4px; color: var(--color-brand-600); font-size: 10px; font-weight: 650; white-space: nowrap; }
.activity-empty { display: flex; min-height: 190px; flex-direction: column; align-items: center; justify-content: center; text-align: center; }
.activity-empty__icon { display: grid; width: 44px; height: 44px; margin-bottom: 12px; place-items: center; border-radius: 14px; color: #7e8da5; background: #f2f5fa; }
.activity-empty strong { color: var(--color-ink-700); font-size: 11px; font-weight: 680; }
.activity-empty span { margin-top: 6px; color: var(--color-ink-400); font-size: 10px; }
.quick-access { margin-top: 27px; }
.quick-access__heading { margin-bottom: 13px; }
.quick-links { display: grid; grid-template-columns: repeat(3,minmax(0,1fr)); gap: 13px; }
.quick-link { display: flex; min-width: 0; align-items: center; gap: 11px; padding: 14px; border: 1px solid var(--color-border-subtle); border-radius: 13px; background: var(--color-surface); transition: border-color .15s,transform .15s,box-shadow .15s; }
.quick-link:hover { border-color: #cddafd; box-shadow: var(--shadow-card); transform: translateY(-2px); }
.quick-link__icon { display: grid; width: 38px; height: 38px; flex: 0 0 auto; place-items: center; border-radius: 11px; }
.quick-link__icon--blue { color: var(--color-brand-600); background: var(--color-brand-100); }
.quick-link__icon--violet { color: #7754c8; background: #f1edff; }
.quick-link__icon--green { color: var(--color-success-700); background: var(--color-success-100); }
.quick-link > span:nth-child(2) { display: flex; min-width: 0; flex: 1; flex-direction: column; gap: 4px; }
.quick-link strong { overflow: hidden; color: var(--color-ink-700); font-size: 11px; font-weight: 680; text-overflow: ellipsis; white-space: nowrap; }
.quick-link small { overflow: hidden; color: var(--color-ink-400); font-size: 9px; text-overflow: ellipsis; white-space: nowrap; }
.quick-link > svg { flex: 0 0 auto; color: #a1aabd; }
@media (max-width: 1180px) { .metric-grid { grid-template-columns: repeat(2,minmax(0,1fr)); } .dashboard-grid { grid-template-columns: 1fr; } .activity-card { min-height: 250px; } }
@media (max-width: 700px) { .welcome-visual { right: -95px; opacity: .38; } .welcome-card__content { padding-inline: 20px; } .welcome-card__copy { max-width: 100%; } .welcome-card__copy p { max-width: 310px; } .quick-links { grid-template-columns: 1fr; gap: 9px; } .quick-link { padding: 11px; } }
@media (max-width: 420px) { .metric-grid { gap: 9px; } .metric-card { min-height: 145px; } .metric-card :deep(.el-card__body) { padding: 14px; } .metric-card__value { font-size: 24px; } .metric-card__detail { font-size: 10px; } }
</style>
