<script setup lang="ts">
import { computed, ref, watch } from 'vue'
import { RouterLink, RouterView, useRoute } from 'vue-router'
import { useMediaQuery } from '@vueuse/core'
import {
  Activity, ArrowLeftRight, Bell, Boxes, ChevronDown, CircleHelp,
  LayoutDashboard, Menu, Package, Search, Settings,
  Users, Warehouse, X,
} from '@lucide/vue'
import { useUiStore } from '../app/stores/ui'

const route = useRoute()
const ui = useUiStore()
const isMobile = useMediaQuery('(max-width: 900px)')
const mobileMenuOpen = ref(false)

const navigation = [
  { label: 'Обзор', to: '/', icon: LayoutDashboard },
  { label: 'Склады', to: '/warehouses', icon: Warehouse },
  { label: 'Остатки', to: '/inventory', icon: Boxes },
  { label: 'Номенклатура', to: '/products', icon: Package },
  { label: 'Операции', to: '/operations', icon: ArrowLeftRight },
  { label: 'Команда', to: '/team', icon: Users },
]

const pageTitle = computed(() => String(route.meta.title ?? 'WMS'))
watch(() => route.fullPath, () => { mobileMenuOpen.value = false })
</script>

<template>
  <div class="app-shell" :class="{ 'app-shell--collapsed': ui.sidebarCollapsed && !isMobile }">
    <div v-if="isMobile && mobileMenuOpen" class="sidebar-backdrop" @click="mobileMenuOpen = false" />
    <aside class="sidebar" :class="{ 'sidebar--mobile-open': isMobile && mobileMenuOpen, 'sidebar--collapsed': ui.sidebarCollapsed && !isMobile }">
      <RouterLink to="/" class="brand-lockup" aria-label="WMS, на главную">
        <span class="brand-mark"><Warehouse :size="20" :stroke-width="2.2" /></span>
        <span class="brand-copy"><strong>northstar</strong><small>WAREHOUSE OS</small></span>
        <button v-if="isMobile" class="icon-button sidebar-close" aria-label="Закрыть меню" @click.prevent="mobileMenuOpen = false"><X :size="19" /></button>
      </RouterLink>

      <div class="tenant-switcher">
        <span class="tenant-logo">N</span>
        <span class="tenant-copy"><strong>Northstar Supply</strong><small>Основной аккаунт</small></span>
        <ChevronDown :size="15" class="tenant-chevron" />
      </div>

      <p class="nav-caption">РАБОЧЕЕ ПРОСТРАНСТВО</p>
      <nav class="main-nav" aria-label="Основная навигация">
        <RouterLink v-for="item in navigation" :key="item.to" :to="item.to" class="nav-link" :class="{ 'nav-link--active': route.path === item.to }" :title="ui.sidebarCollapsed ? item.label : undefined">
          <component :is="item.icon" :size="19" :stroke-width="1.8" />
          <span>{{ item.label }}</span>
          <span v-if="item.to === '/operations'" class="nav-count">3</span>
        </RouterLink>
      </nav>

      <div class="sidebar-spacer" />
      <div class="sidebar-status"><span class="status-dot" /><span>Все системы работают</span><Activity :size="15" /></div>
      <div class="sidebar-footer">
        <a class="nav-link" href="#help"><CircleHelp :size="19" :stroke-width="1.8" /><span>Помощь и поддержка</span></a>
        <a class="nav-link" href="#settings"><Settings :size="19" :stroke-width="1.8" /><span>Настройки</span></a>
        <div class="sidebar-user">
          <div class="user-avatar">АК</div>
          <div class="user-copy"><strong>Алексей Кузнецов</strong><small>Администратор</small></div>
          <ChevronDown :size="15" class="user-chevron" />
        </div>
      </div>
    </aside>

    <section class="shell-main">
      <header class="topbar">
        <div class="topbar-left">
          <button class="icon-button sidebar-toggle" aria-label="Открыть меню" @click="isMobile ? mobileMenuOpen = true : ui.toggleSidebar()"><Menu v-if="isMobile" :size="20" /><span v-else><span v-if="ui.sidebarCollapsed">☰</span><span v-else>‹</span></span></button>
          <div class="breadcrumbs"><span>Рабочее пространство</span><span class="breadcrumb-separator">/</span><strong>{{ pageTitle }}</strong></div>
        </div>
        <div class="topbar-actions">
          <el-input class="global-search" placeholder="Поиск по системе" :prefix-icon="Search" clearable />
          <el-tooltip content="Уведомления" placement="bottom">
            <el-button class="notification-button" circle aria-label="Уведомления"><el-badge is-dot><Bell :size="18" /></el-badge></el-button>
          </el-tooltip>
          <el-divider direction="vertical" />
          <el-button class="profile-button" text>
            <span class="top-avatar">АК</span><span class="profile-name">Алексей</span><ChevronDown :size="14" />
          </el-button>
        </div>
      </header>
      <main class="page-content"><RouterView /></main>
    </section>
  </div>
</template>

<style scoped>
.app-shell { min-height: 100vh; }
.sidebar { position: fixed; z-index: 20; inset: 0 auto 0 0; display: flex; width: var(--sidebar-width); flex-direction: column; padding: 22px 14px 16px; border-right: 1px solid var(--color-border-subtle); background: var(--color-surface); transition: width .22s ease, transform .22s ease; }
.brand-lockup { display: flex; align-items: center; gap: 11px; padding: 0 9px 25px; }
.brand-mark { display: grid; width: 38px; height: 38px; flex: 0 0 auto; place-items: center; border-radius: 12px; color: white; background: linear-gradient(145deg,#3478f6,#1c55c5); box-shadow: 0 6px 14px rgb(37 99 235 / 24%); }
.brand-copy { display: flex; min-width: 0; flex-direction: column; line-height: 1.05; }
.brand-copy strong { color: var(--color-ink-950); font-size: 19px; font-weight: 760; letter-spacing: -.055em; }
.brand-copy small { margin-top: 5px; color: var(--color-ink-400); font-size: 8px; font-weight: 750; letter-spacing: .17em; }
.tenant-switcher { display: flex; align-items: center; gap: 10px; padding: 11px 10px; border: 1px solid var(--color-border-subtle); border-radius: 12px; background: var(--color-surface-muted); }
.tenant-logo { display: grid; width: 33px; height: 33px; flex: 0 0 auto; place-items: center; border-radius: 10px; color: #7754c8; background: #eee9ff; font-size: 14px; font-weight: 750; }
.tenant-copy,.user-copy { display: flex; min-width: 0; flex: 1; flex-direction: column; gap: 4px; }
.tenant-copy strong,.user-copy strong { overflow: hidden; color: var(--color-ink-700); font-size: 11px; font-weight: 700; text-overflow: ellipsis; white-space: nowrap; }
.tenant-copy small,.user-copy small { color: var(--color-ink-400); font-size: 10px; }
.tenant-chevron,.user-chevron { flex: 0 0 auto; color: var(--color-ink-400); }
.nav-caption { margin: 29px 10px 11px; color: #9ba4b3; font-size: 9px; font-weight: 750; letter-spacing: .13em; white-space: nowrap; }
.main-nav { display: grid; gap: 4px; }
.nav-link { display: flex; min-height: 42px; align-items: center; gap: 12px; padding: 0 11px; border-radius: 10px; color: var(--color-ink-500); font-size: 12px; font-weight: 570; transition: color .16s ease, background .16s ease; }
.nav-link:hover { color: var(--color-ink-900); background: #f5f7fb; }
.nav-link--active { color: var(--color-brand-700); background: var(--color-brand-50); font-weight: 680; }
.nav-link--active :deep(svg) { color: var(--color-brand-600); }
.nav-count { display: grid; width: 19px; height: 19px; margin-left: auto; place-items: center; border-radius: 6px; color: #916311; background: #fff3d9; font-size: 10px; font-weight: 700; }
.sidebar-spacer { flex: 1; }
.sidebar-status { display: flex; align-items: center; gap: 8px; margin: 14px 2px; padding: 10px 9px; border: 1px solid #e3f3e9; border-radius: 10px; color: #4d8568; background: #f4fbf6; font-size: 10px; font-weight: 600; white-space: nowrap; }
.sidebar-status svg { margin-left: auto; color: #68a481; }
.status-dot { width: 7px; height: 7px; border-radius: 50%; background: #36ad70; box-shadow: 0 0 0 3px #e0f5e8; }
.sidebar-footer { display: grid; gap: 2px; border-top: 1px solid var(--color-border-subtle); padding-top: 11px; }
.sidebar-footer .nav-link { min-height: 38px; font-size: 11px; }
.sidebar-user { display: flex; align-items: center; gap: 9px; margin-top: 10px; padding: 12px 5px 2px; border-top: 1px solid var(--color-border-subtle); }
.user-avatar,.top-avatar { display: grid; width: 32px; height: 32px; flex: 0 0 auto; place-items: center; border-radius: 50%; color: #75408f; background: #f4eafa; font-size: 10px; font-weight: 750; }
.shell-main { min-height: 100vh; margin-left: var(--sidebar-width); transition: margin-left .22s ease; }
.topbar { position: sticky; z-index: 10; top: 0; display: flex; height: var(--topbar-height); align-items: center; justify-content: space-between; gap: 16px; padding: 0 34px; border-bottom: 1px solid var(--color-border-subtle); background: rgb(255 255 255 / 92%); backdrop-filter: blur(14px); }
.topbar-left,.topbar-actions { display: flex; min-width: 0; align-items: center; gap: 14px; }
.icon-button { display: grid; width: 34px; height: 34px; place-items: center; border: 0; border-radius: 9px; color: var(--color-ink-500); background: transparent; font-size: 22px; }
.icon-button:hover { color: var(--color-ink-900); background: var(--color-surface-muted); }
.breadcrumbs { display: flex; align-items: center; gap: 10px; overflow: hidden; color: var(--color-ink-400); font-size: 11px; white-space: nowrap; }
.breadcrumbs strong { overflow: hidden; color: var(--color-ink-700); font-weight: 650; text-overflow: ellipsis; }
.breadcrumb-separator { color: #c6ccd6; }
.global-search { width: 215px; }
.global-search :deep(.el-input__wrapper) { min-height: 36px; border-radius: 10px; background: #f7f8fb; box-shadow: none; }
.global-search :deep(.el-input__inner) { font-size: 11px; }
.notification-button { width: 36px; height: 36px; border-color: var(--color-border-subtle); color: var(--color-ink-500); }
.topbar-actions :deep(.el-divider--vertical) { height: 24px; margin: 0 0 0 1px; }
.profile-button { display: flex; gap: 8px; color: var(--color-ink-700); }
.top-avatar { width: 29px; height: 29px; font-size: 9px; }
.profile-name { font-size: 11px; font-weight: 600; }
.page-content { max-width: 1600px; margin: 0 auto; padding: 34px 36px 52px; }
.app-shell--collapsed .sidebar { width: 76px; padding-inline: 10px; }
.app-shell--collapsed .shell-main { margin-left: 76px; }
.app-shell--collapsed .brand-copy,.app-shell--collapsed .tenant-copy,.app-shell--collapsed .tenant-chevron,.app-shell--collapsed .nav-caption,.app-shell--collapsed .nav-link span,.app-shell--collapsed .sidebar-status span,.app-shell--collapsed .sidebar-status svg,.app-shell--collapsed .user-copy,.app-shell--collapsed .user-chevron { display: none; }
.app-shell--collapsed .brand-lockup { justify-content: center; padding-inline: 0; }
.app-shell--collapsed .tenant-switcher,.app-shell--collapsed .nav-link,.app-shell--collapsed .sidebar-status,.app-shell--collapsed .sidebar-user { justify-content: center; padding-inline: 0; }
.app-shell--collapsed .nav-link { min-height: 44px; }
.app-shell--collapsed .sidebar-user { border-top: 0; }
.sidebar-backdrop { position: fixed; z-index: 19; inset: 0; background: rgb(18 28 45 / 38%); backdrop-filter: blur(2px); }
.sidebar-close { margin-left: auto; }
.sidebar-toggle { flex: 0 0 auto; }
@media (max-width: 1100px) { .topbar { padding-inline: 24px; } .page-content { padding-inline: 24px; } }
@media (max-width: 900px) {
  .sidebar { width: min(290px, 82vw); transform: translateX(-102%); box-shadow: var(--shadow-floating); }
  .sidebar--mobile-open { transform: translateX(0); }
  .shell-main,.app-shell--collapsed .shell-main { margin-left: 0; }
  .app-shell--collapsed .sidebar { width: min(290px, 82vw); padding: 22px 14px 16px; }
  .app-shell--collapsed .brand-copy,.app-shell--collapsed .tenant-copy,.app-shell--collapsed .tenant-chevron,.app-shell--collapsed .nav-caption,.app-shell--collapsed .nav-link span,.app-shell--collapsed .sidebar-status span,.app-shell--collapsed .sidebar-status svg,.app-shell--collapsed .user-copy,.app-shell--collapsed .user-chevron { display: revert; }
  .app-shell--collapsed .brand-lockup,.app-shell--collapsed .tenant-switcher,.app-shell--collapsed .nav-link,.app-shell--collapsed .sidebar-status,.app-shell--collapsed .sidebar-user { justify-content: initial; padding-inline: initial; }
  .app-shell--collapsed .sidebar-user { border-top: 1px solid var(--color-border-subtle); }
  .global-search { width: min(190px, 34vw); }
}
@media (max-width: 600px) {
  .topbar { height: 62px; padding-inline: 14px; }
  .breadcrumbs > span:first-child,.global-search,.profile-name,.topbar-actions :deep(.el-divider--vertical) { display: none; }
  .breadcrumbs { font-size: 12px; }
  .topbar-left,.topbar-actions { gap: 8px; }
  .page-content { padding: 24px 16px 36px; }
  .profile-button { padding-inline: 3px; }
}
</style>
