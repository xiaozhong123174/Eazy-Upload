import { createApp } from 'vue'
import './style.css'
import App from './App.vue'

import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import UploaderPlugin from './plugins/uploader'

const app = createApp(App)
app.use(Antd)
app.use(UploaderPlugin)
app.mount('#app')
