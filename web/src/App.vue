<script setup>
</script>

<template>
  <div style="padding:24px">
    <a-typography-title :level="4">Eazy-Upload 多文件上传</a-typography-title>
    <Eazy-Upload :multiple="true" :max-count="0" :size-limit-m-b="0" />
  </div>
</template>

<style scoped>
.logo {
  height: 6em;
  padding: 1.5em;
  will-change: filter;
  transition: filter 300ms;
}
.logo:hover {
  filter: drop-shadow(0 0 2em #646cffaa);
}
.logo.vue:hover {
  filter: drop-shadow(0 0 2em #42b883aa);
}
</style>
