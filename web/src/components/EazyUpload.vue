<script setup>
import { ref, computed, nextTick, watch } from 'vue'
import { message } from 'ant-design-vue'
import { uploadFile } from '@/utils/uploader'
import VueOfficeDocx from '@vue-office/docx'
import VueOfficeExcel from '@vue-office/excel'
import VueOfficePptx from '@vue-office/pptx'
import '@vue-office/docx/lib/index.css'
import '@vue-office/excel/lib/index.css'

const props = defineProps({
  action: { type: String, default: '/api/upload' },
  headers: { type: Object, default: () => ({}) },
  extraData: { type: Object, default: () => ({}) },
  withCredentials: { type: Boolean, default: false },
  accept: { type: String, default: '' },
  maxCount: { type: Number, default: 0 },
  multiple: { type: Boolean, default: true },
  buttonText: { type: String, default: '选择文件并上传' },
  sizeLimitMB: { type: Number, default: 0 },
  previewHeight: { type: String, default: '60vh' },
})

const emit = defineEmits(['change', 'success', 'error'])

const fileList = ref([])
const activeUrl = ref('')

function beforeUploadCheck(file) {
  if (props.sizeLimitMB > 0 && file.size > props.sizeLimitMB * 1024 * 1024) {
    message.error(`文件超出大小限制：${props.sizeLimitMB}MB`)
    return false
  }
  return true
}

async function onCustomRequest({ file, onProgress }) {
  const uid = file.uid || `${Date.now()}_${Math.random().toString(36).slice(2)}`
  const item = { uid, name: file.name, status: 'uploading', percent: 0, url: '', raw: file }
  fileList.value = [...fileList.value, item]

  try {
    const resp = await uploadFile({
      file,
      url: props.action,
      headers: props.headers,
      extraData: props.extraData,
      withCredentials: props.withCredentials,
      onProgress: ({ percent }) => { item.percent = percent; onProgress?.({ percent }) },
    })
    item.status = 'done'
    item.url = resp.url
    emit('success', { file: item, response: resp })
    message.success(`${file.name} 上传成功`)
    if (!activeUrl.value) activeUrl.value = item.url
  } catch (e) {
    item.status = 'error'
    message.error(`${file.name} 上传失败：${e.message || ''}`)
    emit('error', { file: item, error: e })
  } finally {
    emit('change', fileList.value)
  }
}

function onRemove(file) {
  fileList.value = fileList.value.filter(f => f.uid !== file.uid)
  if (activeUrl.value === file.url) {
    const next = fileList.value.find(f => f.status === 'done' && f.url)
    activeUrl.value = next ? next.url : ''
  }
  emit('change', fileList.value)
}

// 对外暴露：父组件可调用进行“外部上传”
async function uploadFiles(files) {
  if (!files || files.length === 0) return
  for (const f of Array.from(files)) {
    if (beforeUploadCheck(f) === false) continue
    await onCustomRequest({ file: f })
  }
}

function clear() {
  fileList.value = []
  activeUrl.value = ''
  emit('change', fileList.value)
}

function getFiles() {
  return fileList.value.slice()
}

defineExpose({ uploadFiles, clear, getFiles })

const isImage = computed(() => /\.(png|jpe?g|gif|webp|svg)$/i.test(activeUrl.value))
const isPdf = computed(() => /\.(pdf)$/i.test(activeUrl.value))
const isAudio = computed(() => /\.(mp3|wav|ogg|aac)$/i.test(activeUrl.value))
const isVideo = computed(() => /\.(mp4|webm|ogg)$/i.test(activeUrl.value))
const isCsv = computed(() => /\.(csv)$/i.test(activeUrl.value))
const isText = computed(() => /\.(txt|log|json|xml|md|sql)$/i.test(activeUrl.value))
const isArchive = computed(() => /\.(zip|rar|7z)$/i.test(activeUrl.value))

const fileExt = computed(() => {
  const url = activeUrl.value
  const m = url.match(/\.([a-zA-Z0-9]+)(?:\?.*)?$/)
  return m ? m[1].toLowerCase() : ''
})
const isDocx = computed(() => /(docx)$/i.test(fileExt.value))
const isXlsx = computed(() => /(xlsx|xls)$/i.test(fileExt.value))
const isPptx = computed(() => /(pptx|ppt)$/i.test(fileExt.value))
const isLegacyOffice = computed(() => /(doc|xls|ppt)$/i.test(fileExt.value))

const textContent = ref('')
const csvRows = ref([])

watch(activeUrl, async (url) => {
  textContent.value = ''
  csvRows.value = []
  if (!url) return
  try {
    if (isText.value || isCsv.value) {
      const resp = await fetch(url)
      const txt = await resp.text()
      if (isCsv.value) {
        const lines = txt.split(/\r?\n/).filter(l => l.length > 0)
        csvRows.value = lines.map(l => l.split(','))
      } else {
        textContent.value = txt
      }
    }
  } catch (e) {
    // ignore preview fetch errors; UI will show fallback
  }
})

function buildExternalViewer(url) {
  const absolute = url.startsWith('http') ? url : `${window.location.origin}${url}`
  const gdoc = `https://docs.google.com/gview?embedded=1&url=${encodeURIComponent(absolute)}`
  const office = `https://view.officeapps.live.com/op/embed.aspx?src=${encodeURIComponent(absolute)}`
  return { gdoc, office }
}
</script>

<template>
  <div>
    <a-upload
      :custom-request="onCustomRequest"
      :file-list="fileList"
      :multiple="multiple"
      :accept="accept"
      :max-count="maxCount > 0 ? maxCount : undefined"
      :show-upload-list="{ showRemoveIcon: true, showPreviewIcon: true }"
      @remove="onRemove"
      :before-upload="beforeUploadCheck"
      @preview="(file) => activeUrl = file.url"
    >
      <a-button>
        {{ buttonText }}
      </a-button>
    </a-upload>

    <div v-if="fileList.length" style="margin-top:12px; display:flex; gap:12px; align-items:flex-start;">
      <div style="flex: 1 1 300px; min-width: 300px;">
        <a-typography-text>已上传：</a-typography-text>
        <ul style="margin: 8px 0 0 18px;">
          <li v-for="f in fileList" :key="f.uid">
            <a :href="f.url" target="_blank">{{ f.name }}</a>
            <a-tag v-if="f.status==='done'" color="success" style="margin-left:6px">完成</a-tag>
            <a-tag v-else-if="f.status==='uploading'" color="processing" style="margin-left:6px">上传中 {{ f.percent }}%</a-tag>
            <a-tag v-else color="error" style="margin-left:6px">失败</a-tag>
          </li>
        </ul>
      </div>
      <div style="flex: 2 1 600px; min-width: 400px; border: 1px solid #f0f0f0; padding: 12px;">
        <a-typography-title :level="5">预览</a-typography-title>
        <template v-if="activeUrl">
          <template v-if="isImage">
            <img :src="activeUrl" alt="预览" style="max-width: 100%;" />
          </template>
          <template v-else-if="isPdf">
            <iframe :src="activeUrl" style="width: 100%;" :style="{ height: previewHeight }" frameborder="0"></iframe>
          </template>
          <template v-else-if="isAudio">
            <audio :src="activeUrl" controls style="width: 100%" />
          </template>
          <template v-else-if="isVideo">
            <video :src="activeUrl" controls style="width: 100%" :style="{ height: previewHeight }"></video>
          </template>
          <template v-else-if="isDocx">
            <VueOfficeDocx :src="activeUrl" :style="{ height: previewHeight }" />
          </template>
          <template v-else-if="isXlsx">
            <VueOfficeExcel :src="activeUrl" :style="{ height: previewHeight }" />
          </template>
          <template v-else-if="isPptx">
            <VueOfficePptx :src="activeUrl" :style="{ height: previewHeight }" />
          </template>
          <template v-else-if="isCsv">
            <div style="overflow:auto; max-width:100%; max-height: 60vh; border: 1px dashed #eee; padding: 8px;">
              <table class="csv-table">
                <tbody>
                  <tr v-for="(row, i) in csvRows" :key="i">
                    <td v-for="(cell, j) in row" :key="j" style="padding:4px 8px; border-bottom:1px solid #f5f5f5; white-space:nowrap;">{{ cell }}</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </template>
          <template v-else-if="isText">
            <pre style="max-height:60vh; overflow:auto; background:#fafafa; padding:12px; border:1px dashed #eee;">{{ textContent }}</pre>
          </template>
          <template v-else-if="isArchive">
            <a-alert type="info" show-icon message="压缩包不支持在线预览，请下载到本地解压查看。" />
          </template>
          <template v-else-if="isLegacyOffice">
            <div>
              <a-alert type="warning" show-icon message="旧版 Office 格式（doc/xls/ppt）不直接支持在线预览，已提供在线查看器链接：" />
              <div style="margin-top:8px; display:flex; gap:8px; flex-wrap:wrap;">
                <a :href="buildExternalViewer(activeUrl).gdoc" target="_blank">Google Docs 预览</a>
                <a :href="buildExternalViewer(activeUrl).office" target="_blank">Office Online 预览</a>
              </div>
            </div>
          </template>
          <template v-else>
            <a-alert type="info" show-icon message="该格式暂不支持在线预览，可点击左侧链接下载查看。" />
          </template>
        </template>
        <template v-else>
          <a-empty description="暂无预览" />
        </template>
      </div>
    </div>
  </div>
</template>

<style scoped>
.csv-table {
  border-collapse: collapse;
}
</style> 