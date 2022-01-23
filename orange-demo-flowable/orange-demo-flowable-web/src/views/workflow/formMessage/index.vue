<template>
  <div class="tab-dialog-box" style="position: relative;">
    <el-tabs v-model="activeFragmentId">
      <el-tab-pane label="催办消息" name="remindingMessage" style="width: 100%;">
        <el-form label-width="100px" :size="defaultFormItemSize" label-position="right" @submit.native.prevent>
          <filter-box :item-width="350">
            <el-button slot="operator" type="primary" :plain="true" :size="defaultFormItemSize" @click="refreshRemindingMessage(true)">查询</el-button>
          </filter-box>
        </el-form>
        <el-row>
          <el-col :span="24">
            <el-table ref="flowCategory" :data="remindingMessageWidget.dataList" :size="defaultFormItemSize" @sort-change="remindingMessageWidget.onSortChange"
              header-cell-class-name="table-header-gray">
              <el-table-column label="序号" header-align="center" align="center" type="index" width="55px" :index="remindingMessageWidget.getTableIndex" />
              <el-table-column label="流程名称" prop="processDefinitionName" />
              <el-table-column label="任务名称" prop="taskName" />
              <el-table-column label="催办人" prop="createUsername" />
              <el-table-column label="任务创建时间" prop="taskStartTime" />
              <el-table-column label="催办时间" prop="createTime" />
              <el-table-column label="操作" width="100px">
                <template slot-scope="scope">
                  <el-button type="text" :size="defaultFormItemSize" @click="onSubmit(scope.row)">办理</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-row type="flex" justify="end" style="margin-top: 10px;">
              <el-pagination
                :total="remindingMessageWidget.totalCount"
                :current-page="remindingMessageWidget.currentPage"
                :page-size="remindingMessageWidget.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, prev, pager, next, sizes"
                @current-change="remindingMessageWidget.onCurrentPageChange"
                @size-change="remindingMessageWidget.onPageSizeChange">
              </el-pagination>
            </el-row>
          </el-col>
        </el-row>
      </el-tab-pane>
      <el-tab-pane label="抄送消息" name="copyMessage" style="width: 100%;">
        <el-form label-width="0px" :size="defaultFormItemSize" label-position="right" @submit.native.prevent>
          <filter-box :item-width="350">
            <el-form-item label="">
              <el-radio-group v-model="messageStatus" @change="refreshCopyMessage(true)">
                <el-radio-button :label="1">已读消息</el-radio-button>
                <el-radio-button :label="0">未读消息</el-radio-button>
              </el-radio-group>
            </el-form-item>
            <el-button slot="operator" type="primary" :plain="true" :size="defaultFormItemSize" @click="refreshCopyMessage(true)">查询</el-button>
          </filter-box>
        </el-form>
        <el-row>
          <el-col :span="24">
            <el-table ref="flowCategory" :data="copyMessageWidget.dataList" :size="defaultFormItemSize" @sort-change="copyMessageWidget.onSortChange"
              header-cell-class-name="table-header-gray">
              <el-table-column label="序号" header-align="center" align="center" type="index" width="55px" :index="copyMessageWidget.getTableIndex" />
              <el-table-column label="流程名称" prop="processDefinitionName" />
              <el-table-column label="任务名称" prop="taskName" />
              <el-table-column label="抄送人" prop="createUsername" />
              <el-table-column label="任务创建时间" prop="taskStartTime" />
              <el-table-column label="抄送时间" prop="createTime" />
              <el-table-column label="操作" width="100px">
                <template slot-scope="scope">
                  <el-button type="text" :size="defaultFormItemSize" @click="onSubmit(scope.row, scope.row.messageId)">查看详情</el-button>
                </template>
              </el-table-column>
            </el-table>
            <el-row type="flex" justify="end" style="margin-top: 10px;">
              <el-pagination
                :total="copyMessageWidget.totalCount"
                :current-page="copyMessageWidget.currentPage"
                :page-size="copyMessageWidget.pageSize"
                :page-sizes="[10, 20, 50, 100]"
                layout="total, prev, pager, next, sizes"
                @current-change="copyMessageWidget.onCurrentPageChange"
                @size-change="copyMessageWidget.onPageSizeChange">
              </el-pagination>
            </el-row>
          </el-col>
        </el-row>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script>
import { uploadMixin, statsDateRangeMixin, cachePageMixin } from '@/core/mixins';
import { TableWidget } from '@/utils/widget.js';
/* eslint-disable-next-line */
import { FlowOperationController } from '@/api/flowController.js';

export default {
  name: 'formMessage',
  mixins: [uploadMixin, statsDateRangeMixin, cachePageMixin],
  props: {
    type: {
      type: String,
      default: 'remindingMessage'
    }
  },
  data () {
    return {
      activeFragmentId: 'remindingMessage',
      messageStatus: 1,
      remindingMessageWidget: new TableWidget(this.loadRemindingMessageData, this.loadRemindingMessageVerify, true, false, 'createTime', 1),
      copyMessageWidget: new TableWidget(this.loadCopyMessageData, this.loadCopyMessageVerify, true, false, 'createTime', 1)
    }
  },
  methods: {
    loadRemindingMessageData (params) {
      if (params == null) params = {};
      return new Promise((resolve, reject) => {
        FlowOperationController.listRemindingTask(this, params).then(res => {
          resolve({
            dataList: res.data.dataList,
            totalCount: res.data.totalCount
          });
        }).catch(e => {
          reject(e);
        });
      });
    },
    loadRemindingMessageVerify () {
      return true;
    },
    loadCopyMessageData (params) {
      if (params == null) params = {};
      params.read = this.messageStatus === 1;
      return new Promise((resolve, reject) => {
        FlowOperationController.listCopyMessage(this, params).then(res => {
          resolve({
            dataList: res.data.dataList,
            totalCount: res.data.totalCount
          });
        }).catch(e => {
          reject(e);
        });
      });
    },
    loadCopyMessageVerify () {
      return true;
    },
    refreshRemindingMessage (reloadData = false) {
      if (reloadData) {
        this.remindingMessageWidget.refreshTable(true, 1);
      } else {
        this.remindingMessageWidget.refreshTable();
      }
    },
    refreshCopyMessage (reloadData = false) {
      if (reloadData) {
        this.copyMessageWidget.refreshTable(true, 1);
      } else {
        this.copyMessageWidget.refreshTable();
      }
    },
    refreshFormMessage (reloadData = false) {
      if (reloadData) {
        this.remindingMessageWidget.refreshTable(true, 1);
        this.copyMessageWidget.refreshTable(true, 1);
      } else {
        this.remindingMessageWidget.refreshTable();
        this.copyMessageWidget.refreshTable();
      }
    },
    onSubmit (row, messageId) {
      // 是否抄送消息
      let isCopy = messageId != null;
      let params = {
        processInstanceId: row.processInstanceId,
        processDefinitionId: row.processDefinitionId,
        taskId: row.taskId
      }
      let httpCall = isCopy ? FlowOperationController.viewInitialHistoricTaskInfo(this, params) : FlowOperationController.viewRuntimeTaskInfo(this, params);
      httpCall.then(res => {
        if (res.data) {
          this.$router.push({
            name: res.data.routerName || 'handlerFlowTask',
            query: {
              isRuntime: !isCopy,
              taskId: row.taskId,
              messageId: messageId,
              processDefinitionKey: row.processDefinitionKey,
              processInstanceId: row.processInstanceId,
              processDefinitionId: row.processDefinitionId,
              formId: res.data.formId,
              routerName: res.data.routerName,
              readOnly: isCopy ? true : res.data.readOnly,
              taskName: row.taskName,
              flowEntryName: row.processDefinitionName,
              processInstanceInitiator: row.processInstanceInitiator,
              // 过滤掉加签和撤销操作，加签只有在已完成任务里可以操作
              operationList: (res.data.operationList || []).filter(item => {
                return item.type !== this.SysFlowTaskOperationType.CO_SIGN && item.type !== this.SysFlowTaskOperationType.REVOKE;
              }),
              variableList: res.data.variableList
            }
          });
        }
      }).catch(e => {});
    },
    onResume () {
      this.refreshFormMessage();
    },
    initFormData () {
    },
    formInit () {
      this.refreshFormMessage();
    }
  },
  mounted () {
    // 初始化页面数据
    this.formInit();
  },
  watch: {
    type: {
      handler (val) {
        this.activeFragmentId = val;
        this.messageStatus = 0;
      },
      immediate: true
    }
  }
}
</script>

<style>
</style>
