import request from './request'

export function getUserList(params) {
  return request.get('/admin/user/list', { params })
}

export function getUserDetail(openid) {
  return request.get(`/admin/user/${openid}`)
}

export function getUserQuizzes(openid, params) {
  return request.get(`/admin/user/${openid}/quizzes`, { params })
}

export function getUserChats(openid, params) {
  return request.get(`/admin/user/${openid}/chats`, { params })
}
