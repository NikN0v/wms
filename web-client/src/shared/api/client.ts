import axios from 'axios'

export const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 15_000,
  headers: { Accept: 'application/json' },
})

apiClient.interceptors.request.use((config) => {
  const token = localStorage.getItem('wms.access-token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

export function getApiErrorMessage(error: unknown): string {
  if (axios.isAxiosError<{ message?: string; detail?: string }>(error)) {
    return error.response?.data?.message ?? error.response?.data?.detail ?? error.message
  }
  return error instanceof Error ? error.message : 'Произошла непредвиденная ошибка'
}
