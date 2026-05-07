import request from './request'

export function getNoticeList(params) {
  return request.get('/admin/notice/list', { params })
}

export function createNotice(data) {
  return request.post('/admin/notice', data)
}

export function updateNotice(id, data) {
  return request.put(`/admin/notice/${id}`, data)
}

export function deleteNotice(id) {
  return request.delete(`/admin/notice/${id}`)
}
