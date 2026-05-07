import request from './request'

export function getChatList(params) {
  return request.get('/admin/chat/list', { params })
}

export function getUserChat(openid) {
  return request.get(`/admin/chat/user/${openid}`)
}
