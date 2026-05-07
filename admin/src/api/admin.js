import request from './request'

export function adminLogin(data) {
  return request.post('/admin/login', data)
}

export function getAdminInfo() {
  return request.get('/admin/info')
}
