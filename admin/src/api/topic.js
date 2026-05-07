import request from './request'

export function getTopicList(params) {
  return request.get('/admin/topic/list', { params })
}

export function getTopicDetail(id) {
  return request.get(`/admin/topic/${id}`)
}

export function updateTopicStatus(id, status) {
  return request.put(`/admin/topic/${id}/status`, { status })
}

export function getTopicReplies(id) {
  return request.get(`/admin/topic/${id}/replies`)
}

export function updateReplyStatus(id, status) {
  return request.put(`/admin/topic/reply/${id}/status`, { status })
}
