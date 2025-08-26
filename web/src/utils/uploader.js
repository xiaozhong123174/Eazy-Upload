import http from './http'

export function uploadFile({ file, url = '/api/upload', fieldName = 'file', headers = {}, extraData = {}, withCredentials = false, onProgress }) {
	return new Promise(async (resolve, reject) => {
		try {
			if (!file) throw new Error('未提供文件')
			const formData = new FormData()
			Object.keys(extraData).forEach((key) => formData.append(key, extraData[key]))
			formData.append(fieldName, file)

			const resp = await http.post(url, formData, {
				headers: Object.assign({}, headers, { 'Content-Type': 'multipart/form-data' }),
				withCredentials,
				onUploadProgress: (event) => {
					if (event.total) {
						const percent = Math.round((event.loaded / event.total) * 100)
						onProgress?.({ percent, loaded: event.loaded, total: event.total })
					}
				},
			})
			resolve(resp)
		} catch (e) {
			reject(e)
		}
	})
} 