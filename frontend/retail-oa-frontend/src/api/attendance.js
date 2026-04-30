import request from './request'

export function getAttendanceEmployees() {
  return request({
    url: '/attendance/employees',
    method: 'get'
  })
}

export function getAttendanceApprovers() {
  return request({
    url: '/attendance/approvers',
    method: 'get'
  })
}

export function getWeeklySchedule(params) {
  return request({
    url: '/attendance/schedules/week',
    method: 'get',
    params
  })
}

export function upsertWeeklySchedule(data) {
  return request({
    url: '/attendance/schedules/week',
    method: 'put',
    data
  })
}

export function createLeaveRequest(data) {
  return request({
    url: '/attendance/leave-requests',
    method: 'post',
    data
  })
}

export function getMyLeaveRequests() {
  return request({
    url: '/attendance/leave-requests/mine',
    method: 'get'
  })
}

export function getPendingLeaveRequests() {
  return request({
    url: '/attendance/leave-requests/pending',
    method: 'get'
  })
}

export function approveLeaveRequest(id, data = {}) {
  return request({
    url: `/attendance/leave-requests/${id}/approve`,
    method: 'post',
    data
  })
}

export function rejectLeaveRequest(id, data = {}) {
  return request({
    url: `/attendance/leave-requests/${id}/reject`,
    method: 'post',
    data
  })
}

export function cancelLeaveRequest(id) {
  return request({
    url: `/attendance/leave-requests/${id}/cancel`,
    method: 'post'
  })
}

export function ingestPunchRecord(data) {
  return request({
    url: '/attendance/punch-records',
    method: 'post',
    data
  })
}

export function getPunchRecords(params) {
  return request({
    url: '/attendance/punch-records',
    method: 'get',
    params
  })
}

export function getWeeklySummary(params) {
  return request({
    url: '/attendance/weekly-summary',
    method: 'get',
    params
  })
}

export function getTeamWeeklySummary(params) {
  return request({
    url: '/attendance/weekly-summary/team',
    method: 'get',
    params
  })
}
