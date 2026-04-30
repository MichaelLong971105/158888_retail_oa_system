import request from './request'

export function getSales(params = {}) {
  return request({
    url: '/sales',
    method: 'get',
    params
  })
}

export function getSalesDashboard() {
  return request({
    url: '/sales/dashboard',
    method: 'get'
  })
}

export function createSale(data) {
  return request({
    url: '/sales',
    method: 'post',
    data
  })
}

export function ingestSale(data) {
  return request({
    url: '/sales/ingest',
    method: 'post',
    data
  })
}

export function generateMockSales(data) {
  return request({
    url: '/sales/mock',
    method: 'post',
    data
  })
}
