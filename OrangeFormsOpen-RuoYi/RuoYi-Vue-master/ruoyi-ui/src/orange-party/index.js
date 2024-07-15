import { getToken } from '@/utils/auth'
import $ from 'jquery'
import './index.css'
window.jQuery = $

const layer = require('layui-layer')
export default {
  data() {
    return {
      dialogMap: new Map()
    }
  },
  methods: {
    // 发送消息
    postMessage(sender, type, data) {
      if (sender != null && type != null) {
        sender.postMessage({
          type,
          data
        }, '*')
      }
    },
    // 打开弹窗
    handlerOpenDialog(data, event) {
      const this_ = this
      let area = [data.width || '40vw', data.height || '70vh']
      if (data.dlgFullScreen) {
        area = ['100vw', '100vh']
      }
      const layerOptions = {
        title: data.title,
        type: 2,
        skin: data.dlgFullScreen ? 'fullscreen-dialog' : 'layer-dialog',
        resize: false,
        area: area,
        offset: data.dlgFullScreen ? undefined : (data.top || '50px'),
        zIndex: data.zIndex || 1000,
        index: 0,
        content: data.url,
        success: function(res, index) {
          this_.dialogMap.set(index, {
            source: event.source
          })
          var iframe = $(res).find('iframe')
          if (data.dlgFullScreen) iframe[0].style.height = '100vh'
          this_.postMessage(iframe[0].contentWindow, 'dialogIndex', index)
        }
      }
      const index = layer.open(layerOptions)
      this.dialogMap.set(data.dialogKey, {
        source: event.source,
        index: index
      })
    },
    // 关闭弹窗
    handlerCloseDialog(data) {
      if (data != null) {
        const dialog = this.dialogMap.get(data.dialogKey)
        if (dialog && dialog.source) {
          this.postMessage(dialog.source, 'refreshData', data)
        }
        layer.close(dialog.index)
        this.dialogMap.delete(data.dialogKey)
      }
    },
    // 刷新token
    handlerRefreshToken(data, event) {
      this.postMessage(event.source, 'setToken', {
        token: getToken()
      })
    },
    // 通知消息，例如成功、错误通知等
    handlerUIMessage(data, event) {
      this.$message[data.type](data.text)
    },
    handlerMessage(type, data, event) {
      switch (type) {
        // 打开弹窗
        case 'openDialog':
          this.handlerOpenDialog(data, event)
          break
        // 关闭弹窗
        case 'closeDialog':
          this.handlerCloseDialog(data, event)
          break
        // 刷新token
        case 'refreshToken':
          this.handlerRefreshToken(data, event)
          break
        // 通知消息，例如成功、错误通知等
        case 'message':
          this.handlerUIMessage(data, event)
      }
    },
    eventListener(e) {
      if (e.data == null) return
      this.handlerMessage(e.data.type, e.data.data, e)
    }
  },
  created() {
    window.addEventListener('message', this.eventListener, false)
  },
  destoryed() {
    window.removeEventListener('message', this.eventListener)
  }
}
