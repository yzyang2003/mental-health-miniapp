import request from './request'

export function getMusicList(params) {
  return request.get('/admin/music/list', { params })
}

export function createMusic(data) {
  return request.post('/admin/music', data)
}

export function updateMusic(id, data) {
  return request.put(`/admin/music/${id}`, data)
}

export function deleteMusic(id) {
  return request.delete(`/admin/music/${id}`)
}
