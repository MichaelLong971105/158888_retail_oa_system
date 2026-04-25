import request from './request'

export function getAllOrders(status) {
  return request({
    url: '/orders',
    method: 'get',
    params: status ? { status } : {}
  })
}

export function createOrder(data) {
  return request({
    url: '/orders',
    method: 'post',
    data
  })
}

export function updateOrderStatus(id, status) {
  return request({
    url: `/orders/${id}/status`,
    method: 'put',
    data: { status }
  })
}

export function getOrderStats() {
  return request({
    url: '/orders/stats',
    method: 'get'
  })
}
