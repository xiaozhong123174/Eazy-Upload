import EazyUpload from '@/components/EazyUpload.vue'
import { uploadFile } from '@/utils/uploader'

const UploaderPlugin = {
	install(app) {
		app.component('Eazy-Upload', EazyUpload)
		app.component('EazyUpload', EazyUpload)
		app.config.globalProperties.$uploadFile = uploadFile
		if (typeof window !== 'undefined') {
			window.UploadUtils = Object.assign(window.UploadUtils || {}, { uploadFile })
		}
	},
}

export default UploaderPlugin 