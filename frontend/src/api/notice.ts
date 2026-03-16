import request from '../utils/request'

/** 管理员发布公告 */
export const addNotice = (data: FormData) =>
  request.post('/notice/admin/add', data, { headers: { 'Content-Type': 'multipart/form-data' } })

/** 管理员查询公告列表 */
export const adminListNotices = (params: any) =>
  request.get('/notice/admin/list', { params })

/** 管理员编辑公告 */
export const updateNotice = (data: FormData) =>
  request.put('/notice/admin/update', data, { headers: { 'Content-Type': 'multipart/form-data' } })

/** 管理员删除公告 */
export const deleteNotice = (noticeId: number) =>
  request.delete('/notice/admin/delete', { params: { noticeId } })

/** 管理员置顶/取消置顶 */
export const toggleTop = (noticeId: number, isTop: number) =>
  request.put('/notice/admin/top', null, { params: { noticeId, isTop } })

/** 全角色查询公告列表 */
export const commonListNotices = (params: any) =>
  request.get('/notice/common/list', { params })

/** 查看公告详情 */
export const getNoticeDetail = (noticeId: number) =>
  request.get('/notice/common/detail', { params: { noticeId } })
