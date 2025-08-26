import axios from 'axios'

const http = axios.create({
	baseURL: '/',
	timeout: 30000,
	withCredentials: false,
})

http.interceptors.request.use((config) => {
	return config
})

http.interceptors.response.use(
	(response) => response.data,
	(error) => {
		const msg = error?.response?.data?.message || error.message || '请求错误'
		return Promise.reject(new Error(msg))
	}
)

export default http 