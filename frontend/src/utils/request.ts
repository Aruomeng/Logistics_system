import axios from 'axios'
import type { AxiosInstance, InternalAxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import router from '../router'

let isReloginAlert = false

const service: AxiosInstance = axios.create({
  baseURL: '/api/v1',
  timeout: 15000,
})

// 请求拦截器：自动携带 Token
service.interceptors.request.use(
  (config: InternalAxiosRequestConfig) => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => Promise.reject(error)
)

// 响应拦截器：统一处理错误
service.interceptors.response.use(
  (response: AxiosResponse) => {
    const res = response.data
    if (res.code !== 200) {
      // Token 过期或非法
      if (res.code === 401) {
        if (!isReloginAlert) {
          isReloginAlert = true
          ElMessageBox.confirm('当前登录状态已过期或不合法，您可以继续留在该页面，或者重新登录', '系统提示', {
            confirmButtonText: '重新登录',
            cancelButtonText: '取消',
            type: 'warning'
          }).then(() => {
            isReloginAlert = false
            localStorage.removeItem('token')
            localStorage.removeItem('userInfo')
            router.replace('/login')
          }).catch(() => {
            isReloginAlert = false
          })
        }
      } else {
        ElMessage.error(res.msg || '请求失败')
      }
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    return res
  },
  (error) => {
    const msg = error.response?.data?.msg || error.message || '网络异常'
    if (error.response?.status === 401) {
      if (!isReloginAlert) {
        isReloginAlert = true
        ElMessageBox.confirm('当前登录状态已过期或不合法，您可以继续留在该页面，或者重新登录', '系统提示', {
          confirmButtonText: '重新登录',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          isReloginAlert = false
          localStorage.removeItem('token')
          localStorage.removeItem('userInfo')
          router.replace('/login')
        }).catch(() => {
          isReloginAlert = false
        })
      }
    } else {
      ElMessage.error(msg)
    }
    return Promise.reject(error)
  }
)

export default service
