import { defineStore } from 'pinia'
import { computed, ref } from 'vue'

const ACCESS_TOKEN_KEY = 'wms.access-token'

export interface AuthUser {
  id: number
  tenantId: number
  username: string
  role: 'ADMIN' | 'MANAGER' | 'OPERATOR'
}

export const useAuthStore = defineStore('auth', () => {
  const accessToken = ref<string | null>(localStorage.getItem(ACCESS_TOKEN_KEY))
  const user = ref<AuthUser | null>(null)
  const isAuthenticated = computed(() => Boolean(accessToken.value))

  function setSession(token: string, currentUser: AuthUser) {
    accessToken.value = token
    user.value = currentUser
    localStorage.setItem(ACCESS_TOKEN_KEY, token)
  }

  function clearSession() {
    accessToken.value = null
    user.value = null
    localStorage.removeItem(ACCESS_TOKEN_KEY)
  }

  return { accessToken, user, isAuthenticated, setSession, clearSession }
})
