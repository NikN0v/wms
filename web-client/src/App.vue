<script setup lang="ts">
import { ElConfigProvider } from 'element-plus'
import ru from 'element-plus/es/locale/lang/ru'
</script>

<template>
  <ElConfigProvider :locale="ru">
    <RouterView />
  </ElConfigProvider>
</template>
