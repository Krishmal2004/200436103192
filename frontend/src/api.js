import axios from 'axios'

const api = axios.create({
  baseURL: 'http://localhost:8080/api',
})

export const getDepartments = () => api.get('/departments').then((res) => res.data)

export const getOfficers = () => api.get('/officers').then((res) => res.data)

export const getProgrammes = () => api.get('/programmes').then((res) => res.data)

export const createProgramme = (payload) => api.post('/programmes', payload).then((res) => res.data)

export const getNominations = (programmeId) =>
  api.get('/nominations', { params: { programmeId } }).then((res) => res.data)

export const createNomination = (payload) => api.post('/nominations', payload).then((res) => res.data)

export const cancelNomination = (id) => api.delete(`/nominations/${id}`).then((res) => res.data)

export default api
