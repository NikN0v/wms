import { createRouter, createWebHistory } from 'vue-router'
import AppShell from '../../layouts/AppShell.vue'

export const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      component: AppShell,
      children: [
        { path: '', name: 'dashboard', component: () => import('../../views/DashboardView.vue'), meta: { title: 'Обзор' } },
        { path: 'warehouses', name: 'warehouses', component: () => import('../../views/ModulePlaceholderView.vue'), meta: { title: 'Склады', description: 'Управление складами, зонами и адресами хранения.' } },
        { path: 'inventory', name: 'inventory', component: () => import('../../views/ModulePlaceholderView.vue'), meta: { title: 'Остатки', description: 'Остатки, доступное количество и движения товаров.' } },
        { path: 'products', name: 'products', component: () => import('../../views/ModulePlaceholderView.vue'), meta: { title: 'Номенклатура', description: 'Единый каталог товаров и SKU компании.' } },
        { path: 'operations', name: 'operations', component: () => import('../../views/ModulePlaceholderView.vue'), meta: { title: 'Операции', description: 'Приёмка, перемещения, резервы, отгрузки и инвентаризации.' } },
        { path: 'team', name: 'team', component: () => import('../../views/ModulePlaceholderView.vue'), meta: { title: 'Команда', description: 'Пользователи, роли и доступ сотрудников к складам.' } },
        { path: ':pathMatch(.*)*', redirect: '/' },
      ],
    },
  ],
  scrollBehavior: () => ({ top: 0, behavior: 'smooth' }),
})
