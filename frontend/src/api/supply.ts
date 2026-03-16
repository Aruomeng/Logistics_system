import request from '../utils/request'

// ========== 供应方接口 ==========
export const addSupply = (data: any) => request.post('/supply/add', data)
export const updateSupply = (data: any) => request.put('/supply/update', data)
export const supplierListSupply = (params: any) => request.get('/supply/list', { params })
export const onlineSupply = (supplyId: number) => request.put('/supply/online', null, { params: { supplyId } })
export const offlineSupply = (supplyId: number) => request.put('/supply/offline', null, { params: { supplyId } })

// ========== 公共接口 ==========
export const commonListSupply = (params: any) => request.get('/supply/common/list', { params })
export const getSupplyDetail = (supplyIdOrCode: number | string) => {
  const isNum = typeof supplyIdOrCode === 'number' || /^\d+$/.test(String(supplyIdOrCode))
  const params = isNum ? { supplyId: Number(supplyIdOrCode) } : { traceCode: supplyIdOrCode }
  return request.get('/supply/common/detail', { params })
}

// ========== 管理员接口 ==========
export const adminListSupply = (params: any) => request.get('/supply/admin/list', { params })
export const auditSupply = (supplyId: number, auditStatus: number, auditRemark?: string) =>
  request.put('/supply/admin/audit', null, { params: { supplyId, auditStatus, auditRemark } })
export const forceOfflineSupply = (supplyId: number) =>
  request.put('/supply/admin/forceOffline', null, { params: { supplyId } })
