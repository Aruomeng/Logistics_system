import request from '../utils/request'

export const getOverview = () => request.get('/analysis/overview')
export const getOrderTrend = () => request.get('/analysis/order/trend')
export const getCategoryDistribution = () => request.get('/analysis/category/distribution')
export const getPlaceRanking = () => request.get('/analysis/place/ranking')
export const getSupplierRanking = () => request.get('/analysis/supplier/ranking')
export const getInventoryWarning = () => request.get('/analysis/inventory/warning')
export const getInventoryTurnover = () => request.get('/analysis/inventory/turnover')
export const getColdChainWarning = () => request.get('/analysis/cold-chain-warning')
