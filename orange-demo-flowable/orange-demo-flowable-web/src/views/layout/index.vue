<template>
  <el-container :style="getMainStyle">
    <el-aside width='250px' class="sidebar">
      <side-bar style="overflow: hidden"></side-bar>
    </el-aside>
    <el-container style="background-color: #F5F8F9">
      <el-header class="header" style="box-shadow: 0px 2px 4px 0px rgba(206, 206, 206, 0.5);">
        <breadcrumb class="breadcrumb-container" />
        <div class="menu-column" v-if="getMultiColumn" style="margin-left: 20px;">
          <el-menu mode="horizontal" :default-active="getCurrentColumnId" @select="onColumnChange">
            <el-menu-item v-for="column in getColumnList" :key="column.columnId" :index="column.columnId">{{column.columnName}}</el-menu-item>
          </el-menu>
        </div>
        <div class="header-menu" style="flex-grow: 1;">
          <el-dropdown trigger="click" style="margin-right: 10px;" @command="handleMessage">
            <el-badge is-dot :hidden="(getMessageCount || {}).totalCount == null || (getMessageCount || {}).totalCount <= 0"
              style="height: 18px; line-height: 18px; cursor: pointer;">
              <i class="el-icon-bell" style="font-size: 18px;" />
            </el-badge>
            <el-dropdown-menu slot="dropdown" style="min-width: 130px;">
              <el-dropdown-item class="user-dropdown-item" command="remindingMessage">
                催办消息
                <el-badge :value="(getMessageCount || {}).remindingMessageCount"
                  :hidden="(getMessageCount || {}).remindingMessageCount == null || (getMessageCount || {}).remindingMessageCount <= 0"
                />
              </el-dropdown-item>
              <el-dropdown-item class="user-dropdown-item" command="copyMessage">
                抄送消息
                <el-badge :value="(getMessageCount || {}).copyMessageCount"
                  :hidden="(getMessageCount || {}).copyMessageCount == null || (getMessageCount || {}).copyMessageCount <= 0"
                />
              </el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <el-dropdown class="user-dropdown" trigger="click" @command="handleCommand">
            <span class="el-dropdown-link">{{(getUserInfo || {}).showName}}<i class="el-icon-arrow-down el-icon--right"></i>
            </span>
            <el-dropdown-menu slot="dropdown">
              <el-dropdown-item class="user-dropdown-item" command="modifyPassword">修改密码</el-dropdown-item>
              <el-dropdown-item class="user-dropdown-item" command="modifyHeadImage">修改头像</el-dropdown-item>
              <el-dropdown-item class="user-dropdown-item" command="logout">退出登录</el-dropdown-item>
            </el-dropdown-menu>
          </el-dropdown>
          <img :src="getHeadImageUrl ? getHeadImageUrl : header" class="header-img" />
        </div>
      </el-header>
      <el-main :style="{'padding-bottom': '15px', 'padding-top': (getMultiTags ? '0px' : '15px')}">
        <tag-panel v-if="getMultiTags" :tagList="getTagList" style="margin: 0px 20px;" />
        <el-scrollbar :style="getContextStyle" wrap-class="scrollbar_dropdown__wrap">
          <transition name="fade" mode="out-in">
            <keep-alive :include="getCachePages">
              <router-view style="margin: 0px 15px; background-color: white; overflow: hidden; padding: 20px;" :style="getRouterViewStyle" />
            </keep-alive>
          </transition>
        </el-scrollbar>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
import '@/staticDict/onlineStaticDict.js';
import SideBar from './components/sidebar/sidebar.vue';
import { mapGetters, mapMutations, mapActions } from 'vuex';
/* eslint-disable-next-line */
import { uploadMixin, statsDateRangeMixin } from '@/core/mixins';
import Breadcrumb from './components/breadcrumb';
import TagPanel from './components/tags/tagPanel.vue';
import formModifyPassword from './components/formModifyPassword/index.vue';
import formModifyHeadImage from './components/formModifyHeadImage/index.vue';
import { SystemController } from '@/api';
import { getToken, setToken } from '@/utils';

export default {
  data () {
    return {
      header: require('../../assets/img/default-header.jpg')
    };
  },
  components: {
    'side-bar': SideBar,
    'breadcrumb': Breadcrumb,
    'tag-panel': TagPanel
  },
  mixins: [uploadMixin, statsDateRangeMixin],
  methods: {
    toggleSideBar () {
      this.setCollapse(!this.getCollapse);
    },
    onColumnChange (columnId) {
      this.setCurrentColumnId(columnId);
      this.clearCachePage();
      this.$router.replace({
        name: 'welcome'
      });
    },
    resetDocumentClientHeight () {
      let timerID;
      let _this = this;
      return function () {
        clearTimeout(timerID);
        timerID = setTimeout(() => {
          var h = document.documentElement['clientHeight'];
          var w = document.documentElement['clientWidth'];
          _this.setClientHeight(h);
          _this.setClientWidth(w);
        }, 50);
      }
    },
    handleCommand (command) {
      if (command === 'logout') {
        this.$confirm('是否退出登录？', '', {
          confirmButtonText: '确定',
          cancelButtonText: '取消',
          type: 'warning'
        }).then(() => {
          let options = {
            headers: {
              Authorization: getToken()
            },
            showMask: false
          }
          SystemController.logout(this, {}, options).catch(e => {});
          this.clearAllTags();
          setToken();
          window.sessionStorage.removeItem('isUaaLogin');
          this.$router.replace({name: 'login'});
        }).catch(e => {});
      } else if (command === 'modifyPassword') {
        this.$dialog.show('修改密码', formModifyPassword, {
          area: ['500px']
        }, {}).catch(e => {});
      } else if (command === 'modifyHeadImage') {
        this.$dialog.show('修改头像', formModifyHeadImage, {
          area: ['500px']
        }, {}).catch(e => {});
      }
    },
    handleMessage (type) {
      this.onMoreMessageClick(type);
    },
    // 更多催办消息
    onMoreMessageClick (type) {
      this.$router.push({
        name: 'formMessage',
        query: {
          type: type
        }
      });
    },
    ...mapMutations([
      'setClientHeight',
      'setClientWidth',
      'setCurrentColumnId',
      'clearCachePage',
      'clearAllTags',
      'setUserInfo',
      'clearOnlineFormCache',
      'setMenuList'
    ]),
    ...mapActions([
      'startMessage',
      'stopMessage'
    ])
  },
  computed: {
    getMainStyle () {
      return [
        {'height': this.getClientHeight + 'px'}
      ]
    },
    getContextStyle () {
      return [
        {'height': this.getMainContextHeight + 'px'}
      ]
    },
    getRouterViewStyle () {
      return [
        {'min-height': this.getMainContextHeight + 'px'}
      ]
    },
    getHeadImageUrl () {
      if (this.getUserInfo && this.getUserInfo.headImageUrl != null && this.getUserInfo.headImageUrl !== '') {
        let temp = this.getUploadFileUrl(this.getUserInfo.headImageUrl, { filename: this.getUserInfo.headImageUrl.filename });
        return temp;
      } else {
        return null;
      }
    },
    ...mapGetters([
      'getMultiTags',
      'getClientHeight',
      'getUserInfo',
      'getCollapse',
      'getCachePages',
      'getTagList',
      'getMultiColumn',
      'getCurrentColumnId',
      'getColumnList',
      'getMenuItem',
      'getMessageCount',
      'getMainContextHeight'
    ])
  },
  mounted () {
    let resetHeight = this.resetDocumentClientHeight();
    resetHeight();
    
    window.onresize = () => {
      resetHeight();
    }

    // 重新获取登录信息
    if (getToken() != null && getToken() !== '' && this.getUserInfo == null) {
      SystemController.getLoginInfo(this, {}).then(data => {
        this.setMenuList(data.data.menuList);
        delete data.data.menuList;
        this.setUserInfo(data.data);
      }).catch(e => {});
    }

    // 获取催办消息
    this.startMessage(this);
  },
  watch: {
    getMenuItem: {
      handler (newValue) {
        if (newValue == null) {
          if (this.$route.name !== 'welcome') {
            this.$router.replace({
              name: 'welcome'
            });
          }
        } else {
          if (newValue.onlineFormId == null) {
            this.$router.replace({
              name: newValue.formRouterName
            });
          } else {
            this.clearOnlineFormCache();
            if (newValue.onlineFlowEntryId == null) {
              this.$router.replace({
                name: 'onlineForm',
                query: {
                  formId: newValue.onlineFormId,
                  formType: this.SysOnlineFormType.QUERY
                }
              });
            } else {
              this.$router.replace({
                name: 'onlineForm',
                query: {
                  formId: newValue.onlineFormId,
                  entryId: newValue.onlineFlowEntryId,
                  formType: this.SysOnlineFormType.WORK_ORDER
                }
              });
            }
          }
        }
      },
      immediate: true
    }
  },
  beforeRouteLeave (to, form, next) {
    this.stopMessage();
    next();
  },
  destoryed () {
    this.stopMessage();
  }
}
</script>

<style lang="scss">
  .message-popover {
    padding: 5px!important;
  }

  .message-popover .el-table::before {
    height: 0px!important;
  }

  .message-popover .el-table td {
    border: none;
  }
</style>
