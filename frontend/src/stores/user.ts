import { defineStore } from 'pinia'
import { ref, computed } from 'vue'

export interface UserInfo {
  userId: number
  username: string
  roleType: number
  phone?: string
  email?: string
  avatar?: string
  gender?: number
}

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(
    JSON.parse(localStorage.getItem('userInfo') || 'null')
  )

  const isLoggedIn = computed(() => !!token.value)
  const roleType = computed(() => userInfo.value?.roleType ?? 0)

  /** 角色名称映射 */
  const roleName = computed(() => {
    const map: Record<number, string> = {
      1: '系统管理员',
      2: '供应方',
      3: '物流方',
      4: '消费者',
    }
    return map[roleType.value] || '未知'
  })

  function setLogin(t: string, info: UserInfo) {
    token.value = t
    userInfo.value = info
    localStorage.setItem('token', t)
    localStorage.setItem('userInfo', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('userInfo')
  }

  return { token, userInfo, isLoggedIn, roleType, roleName, setLogin, logout }
})
