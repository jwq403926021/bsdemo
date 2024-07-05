import { createApp } from 'vue';
import * as ElementPlusIconsVue from '@element-plus/icons-vue';
import '@/common/http/request';
import { VxeTable, VxeColumn, Edit } from 'vxe-table';
import App from '@/App.vue';
import { router } from '@/router/index';
import pinia from '@/store';

// 表格样式
import 'vxe-table/lib/style.css';
// layui样式
import '@layui/layui-vue/lib/index.css';
// vant样式
import 'vant/lib/index.css';
// element-plus 按需导入缺少的样式
import 'element-plus/theme-chalk/el-loading.css';
import 'element-plus/theme-chalk/el-message.css';
import 'element-plus/theme-chalk/el-message-box.css';
import 'element-plus/theme-chalk/el-notification.css';
import 'element-plus/theme-chalk/el-cascader.css';
import 'element-plus/theme-chalk/el-cascader-panel.css';
import 'element-plus/theme-chalk/el-tree.css';
import 'element-plus/theme-chalk/el-date-picker.css';
import 'element-plus/theme-chalk/el-input-number.css';

// 其它样式
import '@/assets/online-icon/iconfont.css';
// 静态字典
import * as staticDict from '@/common/staticDict/index';
import * as olineDicgt from '@/common/staticDict/online';
import * as flowDict from '@/common/staticDict/flow';

import { ANY_OBJECT } from '@/types/generic';

function useTable(app: ANY_OBJECT) {
  app.use(VxeTable).use(VxeColumn).use(Edit);
}
function useStaticDict(app: ANY_OBJECT, staticDict: ANY_OBJECT) {
  Object.keys(staticDict).forEach(key => {
    app.config.globalProperties[key] = staticDict[key];
  });
}

const app = createApp(App);
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component);
}
useStaticDict(app, staticDict);
useStaticDict(app, olineDicgt);
useStaticDict(app, flowDict);
app.use(pinia).use(router).use(useTable);
app.mount('#app');
