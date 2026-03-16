import request from '../utils/request'

// ========== 采购方（消费者）操作 ==========

/** 发布采购需求 */
export const addDemand = (data: any) => request.post('/demand/add', data)

/** 编辑需求 */
export const updateDemand = (data: any) => request.put('/demand/update', data)

/** 采购方查询自有需求列表 */
export const myDemandList = (params: any) => request.get('/demand/my/list', { params })

/** 需求上架 */
export const onlineDemand = (demandId: number) => request.put('/demand/online', null, { params: { demandId } })

/** 需求下架 */
export const offlineDemand = (demandId: number) => request.put('/demand/offline', null, { params: { demandId } })

/** 关闭需求 */
export const closeDemand = (demandId: number, closeReason?: string) =>
  request.put('/demand/close', null, { params: { demandId, closeReason } })

// ========== 公共接口 ==========

/** 公开需求列表 */
export const commonDemandList = (params: any) => request.get('/demand/common/list', { params })

/** 需求详情 */
export const getDemandDetail = (demandId: number) =>
  request.get('/demand/common/detail', { params: { demandId } })

// ========== 供应方承接 ==========

/** 发起承接 */
export const addAccept = (data: any) => request.post('/demand/accept/add', data)

/** 承接列表 */
export const getAcceptList = (params: any) => request.get('/demand/accept/list', { params })

/** 确认承接 */
export const confirmAccept = (acceptId: number) =>
  request.put('/demand/accept/confirm', null, { params: { acceptId } })

/** 驳回承接 */
export const rejectAccept = (acceptId: number, rejectReason?: string) =>
  request.put('/demand/accept/reject', null, { params: { acceptId, rejectReason } })

// ========== 供需匹配 ==========

/** 智能匹配 */
export const matchDemandSupply = (params: { demandId?: number; supplyId?: number }) =>
  request.get('/demand/match', { params })

// ========== 管理员操作 ==========

/** 管理员审核 */
export const auditDemand = (demandId: number, auditStatus: number, auditRemark?: string) =>
  request.put('/demand/admin/audit', null, { params: { demandId, auditStatus, auditRemark } })

/** 管理员需求列表 */
export const adminDemandList = (params: any) => request.get('/demand/admin/list', { params })

/** 管理员强制下架 */
export const forceOfflineDemand = (demandId: number) =>
  request.put('/demand/admin/forceOffline', null, { params: { demandId } })
