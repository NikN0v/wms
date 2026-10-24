import { ElNotification } from 'element-plus'

type NoticeOptions = { title?: string; duration?: number }

function show(type: 'success' | 'warning' | 'info' | 'error', message: string, options: NoticeOptions = {}) {
  ElNotification({
    type,
    title: options.title ?? ({ success: 'Готово', warning: 'Внимание', info: 'Информация', error: 'Ошибка' }[type]),
    message,
    duration: options.duration ?? 4000,
    position: 'top-right',
    customClass: 'wms-notification',
    showClose: true,
  })
}

export const notify = {
  success: (message: string, options?: NoticeOptions) => show('success', message, options),
  warning: (message: string, options?: NoticeOptions) => show('warning', message, options),
  info: (message: string, options?: NoticeOptions) => show('info', message, options),
  error: (message: string, options?: NoticeOptions) => show('error', message, options),
}
