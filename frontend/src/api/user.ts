import request from '../utils/request'

/** 用户登录 */
export const login = (data: { username: string; password: string }) =>
  request.post('/user/login', data)

/** 用户登出 */
export const logout = () => request.post('/user/logout')

/** 获取个人信息 */
export const getUserInfo = () => request.get('/user/info')

/** 更新个人信息 */
export const updateUserInfo = (data: any) => request.put('/user/info/update', data)

/** 修改密码 */
export const modifyPassword = (data: { oldPwd: string; newPwd: string }) =>
  request.put('/user/password/modify', data)

/** 高危操作二次验证 */
export const verifyPassword = (password: string) =>
  request.post('/user/password/verify', { password })

// ========== 管理员接口 ==========

/** 管理员查询用户列表 */
export const listUsers = (params: {
  page: number
  size: number
  username?: string
  roleType?: number
  status?: number
}) => request.get('/user/admin/list', { params })

/** 管理员新增用户 */
export const addUser = (data: any) => request.post('/user/admin/add', data)

/** 管理员编辑用户 */
export const adminUpdateUser = (data: any) => request.put('/user/admin/update', data)

/** 管理员删除用户 */
export const deleteUser = (userId: number) =>
  request.delete('/user/admin/delete', { params: { userId } })

/** 管理员重置密码 */
export const resetPassword = (userId: number) =>
  request.put('/user/admin/password/reset', null, { params: { userId } })
