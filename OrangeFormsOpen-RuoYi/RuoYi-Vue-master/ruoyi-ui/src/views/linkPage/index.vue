<template>
  <div>
    <iframe :src="getLinkUrl" style="width: 100%; height: calc(100vh - 87px); border: none;" />
  </div>
</template>

<script>
import { getToken } from '@/utils/auth'

export default {
  name: 'LinkPage',
  computed: {
    getLinkUrl() {
      console.log(this.url, this.$route.query, getToken())
      const tempUrl = this.$route.query ? this.$route.query.url : undefined
      if (tempUrl != null) {
        if (tempUrl.indexOf('?') === -1) {
          return tempUrl + '?appId=ruoyi&token=' + getToken()
        } else {
          return tempUrl + '&appId=ruoyi&token=' + getToken()
        }
      }
      return tempUrl
    }
  }
}
</script>

<style>
</style>
