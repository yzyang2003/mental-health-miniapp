import request from './request'

export function getHealingList(params) {
  return request.get('/admin/healing/list', { params })
}

export function createHealing(data) {
  return request.post('/admin/healing', data)
}

export function updateHealing(id, data) {
  return request.put(`/admin/healing/${id}`, data)
}

export function deleteHealing(id) {
  return request.delete(`/admin/healing/${id}`)
}
