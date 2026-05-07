import request from './request'

export function getQuizList() {
  return request.get('/admin/quiz/list')
}

export function getQuizDetail(id) {
  return request.get(`/admin/quiz/${id}`)
}

export function createQuiz(data) {
  return request.post('/admin/quiz', data)
}

export function updateQuiz(id, data) {
  return request.put(`/admin/quiz/${id}`, data)
}

export function updateQuizStatus(id, status) {
  return request.put(`/admin/quiz/${id}/status`, { status })
}

export function getQuizQuestions(id) {
  return request.get(`/admin/quiz/${id}/questions`)
}

export function saveQuizQuestions(id, questions) {
  return request.post(`/admin/quiz/${id}/questions`, questions)
}

export function deleteQuiz(id) {
  return request.delete(`/admin/quiz/${id}`)
}
