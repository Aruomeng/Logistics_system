import request from '../utils/request'

// ========== 消费者 ==========
export const createOrder = (data: any) => request.post('/order/create', data)
export const consumerListOrders = (params: any) => request.get('/order/consumer/list', { params })
export const consumerConfirm = (orderId: number) => request.put('/order/consumer/confirm', null, { params: { orderId } })
export const consumerCancel = (orderId: number) => request.put('/order/consumer/cancel', null, { params: { orderId } })

// ========== 供应方 ==========
export const supplierListOrders = (params: any) => request.get('/order/supplier/list', { params })
export const supplierConfirm = (orderId: number) => request.put('/order/supplier/confirm', null, { params: { orderId } })
export const supplierConfirmAndAssign = (orderId: number, logisticsId: string, logisticsName: string) =>
  request.put('/order/supplier/confirmAndAssign', null, { params: { orderId, logisticsId, logisticsName } })
export const supplierReject = (orderId: number, reason?: string) =>
  request.put('/order/supplier/reject', null, { params: { orderId, reason } })

// ========== 物流方 ==========
export const logisticsListOrders = (params: any) => request.get('/order/logistics/list', { params })
export const logisticsPickup = (orderId: number) =>
  request.put('/order/logistics/pickup', null, { params: { orderId } })
export const logisticsUpdate = (orderId: number, trackInfo: string) =>
  request.put('/order/logistics/update', null, { params: { orderId, trackInfo } })
export const logisticsDelivered = (orderId: number) =>
  request.put('/order/logistics/delivered', null, { params: { orderId } })

// ========== 管理员 ==========
export const adminListOrders = (params: any) => request.get('/order/admin/list', { params })
export const assignLogistics = (orderId: number, logisticsId: number, logisticsName: string) =>
  request.put('/order/admin/assignLogistics', null, { params: { orderId, logisticsId, logisticsName } })

// ========== 公共 ==========
export const getOrderDetail = (orderId: number) => request.get('/order/detail', { params: { orderId } })
