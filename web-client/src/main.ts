import { createApp } from 'vue'
import { createPinia } from 'pinia'
import { ElBadge, ElButton, ElCard, ElConfigProvider, ElDivider, ElInput, ElTooltip } from 'element-plus'
import 'element-plus/dist/index.css'
import './style.css'
import App from './App.vue'
import { router } from './app/router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
;[ElBadge, ElButton, ElCard, ElConfigProvider, ElDivider, ElInput, ElTooltip].forEach((component) => {
  app.use(component)
})
app.mount('#app')
