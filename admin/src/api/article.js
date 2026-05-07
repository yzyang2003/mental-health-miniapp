import request from './request'

export function getArticleList(params) {
  return request.get('/admin/article/list', { params })
}

export function getArticleDetail(id) {
  return request.get(`/admin/article/${id}`)
}

export function createArticle(data) {
  return request.post('/admin/article', data)
}

export function updateArticle(id, data) {
  return request.put(`/admin/article/${id}`, data)
}

export function updateArticleStatus(id, status) {
  return request.put(`/admin/article/${id}/status`, { status })
}

export function deleteArticle(id) {
  return request.delete(`/admin/article/${id}`)
}

export function aiFillArticle(url) {
  return request.post('/admin/article/ai-fill', { url }, { timeout: 30000 })
}
