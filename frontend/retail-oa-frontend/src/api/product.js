import request from './request'

export function getAllProducts() {
  return request({
    url: '/products',
    method: 'get'
  })
}

export function createProduct(data) {
  return request({
    url: '/products',
    method: 'post',
    data
  })
}

export function updateProduct(id, data) {
  return request({
    url: `/products/${id}`,
    method: 'put',
    data
  })
}

export function deleteProduct(id) {
  return request({
    url: `/products/${id}`,
    method: 'delete'
  })
}

export function stockInProduct(id, data) {
  return request({
    url: `/products/${id}/stock/in`,
    method: 'put',
    data
  })
}

export function stockOutProduct(id, data) {
  return request({
    url: `/products/${id}/stock/out`,
    method: 'put',
    data
  })
}

export function getProductInventoryLogs(id) {
  return request({
    url: `/products/${id}/inventory-logs`,
    method: 'get'
  })
}
