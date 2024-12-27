/**
 * 流程表单常量字典
 */
import { DictionaryBase } from './types';

const SysFlowEntryBindFormType = new DictionaryBase('流程绑定表单类型', [
  {
    id: 0,
    name: '动态表单',
    symbol: 'ONLINE_FORM',
  },
  {
    id: 1,
    name: '路由表单',
    symbol: 'ROUTER_FORM',
  },
]);

const SysFlowEntryPublishedStatus = new DictionaryBase('流程设计发布状态', [
  {
    id: 0,
    name: 'Unpublished',
    symbol: 'UNPUBLISHED',
  },
  {
    id: 1,
    name: 'Published',
    symbol: 'PUBLISHED',
  },
]);

const SysFlowEntryStep = new DictionaryBase('流程设计步骤', [
  {
    id: 0,
    name: '编辑基础信息',
    symbol: 'BASIC',
  },
  {
    id: 1,
    name: '流程变量设置',
    symbol: 'PROCESS_VARIABLE',
  },
  {
    id: 2,
    name: '设计流程',
    symbol: 'PROCESS_DESIGN',
  },
  {
    id: 3,
    name: '流程状态设置',
    symbol: 'PROCESS_STATUS',
  },
]);

const SysFlowTaskOperationType = new DictionaryBase('任务操作类型', [
  {
    id: 'agree',
    name: 'Agree',
    symbol: 'AGREE',
  },
  {
    id: 'refuse',
    name: 'Refuse',
    symbol: 'REFUSE',
  },
  {
    id: 'reject',
    name: 'Reject',
    symbol: 'REJECT',
  },
  {
    id: 'rejectToStart',
    name: 'Reject To Start',
    symbol: 'REJECT_TO_START',
  },
  {
    id: 'revoke',
    name: 'Revoke',
    symbol: 'REVOKE',
  },
  {
    id: 'transfer',
    name: 'Transfer',
    symbol: 'TRANSFER',
  },
  {
    id: 'multi_consign',
    name: 'CO SIGN',
    symbol: 'CO_SIGN',
  },
  {
    id: 'multi_minus_sign',
    name: 'SIGN REDUCTION',
    symbol: 'SIGN_REDUCTION',
  },
  {
    id: 'save',
    name: 'Save',
    symbol: 'SAVE',
  },
  {
    id: 'stop',
    name: 'Stop',
    symbol: 'STOP',
  },
  {
    id: 'multi_sign',
    name: 'MULTI SIGN',
    symbol: 'MULTI_SIGN',
  },
  {
    id: 'multi_agree',
    name: 'MULTI AGREE',
    symbol: 'MULTI_AGREE',
  },
  {
    id: 'multi_refuse',
    name: 'MULTI REFUSE',
    symbol: 'MULTI_REFUSE',
  },
  {
    id: 'multi_abstain',
    name: 'MULTI ABSTAIN',
    symbol: 'MULTI_ABSTAIN',
  },
  {
    id: 'set_assignee',
    name: 'SET ASSIGNEE',
    symbol: 'SET_ASSIGNEE',
  },
  {
    id: 'multi_before_consign',
    name: 'BFORE CONSIGN',
    symbol: 'BFORE_CONSIGN',
  },
  {
    id: 'multi_after_consign',
    name: 'AFTER  CONSIGN',
    symbol: 'AFTER_CONSIGN',
  },
]);

const SysFlowTaskType = new DictionaryBase('工作流任务类型', [
  {
    id: 0,
    name: '其他任务',
    symbol: 'OTHER_TASK',
  },
  {
    id: 1,
    name: '用户任务',
    symbol: 'USER_TASK',
  },
]);

const SysFlowVariableType = new DictionaryBase('工作流变量类型', [
  {
    id: 0,
    name: '流程变量',
    symbol: 'INSTANCE',
  },
  {
    id: 1,
    name: '任务变量',
    symbol: 'TASK',
  },
  {
    id: 2,
    name: '系统内置变量',
    symbol: 'SYSTEM',
  },
  {
    id: 4,
    name: '自定义变量',
    symbol: 'CUSTOM',
  },
]);

const SysFlowWorkOrderStatus = new DictionaryBase('工单状态', [
  {
    id: 0,
    name: '已提交',
    symbol: 'SUBMITED',
  },
  {
    id: 1,
    name: '审批中',
    symbol: 'APPROVING',
  },
  {
    id: 2,
    name: '已拒绝',
    symbol: 'REFUSED',
  },
  {
    id: 3,
    name: '已完成',
    symbol: 'FINISHED',
  },
  {
    id: 4,
    name: '终止',
    symbol: 'STOPPED',
  },
  {
    id: 5,
    name: '撤销',
    symbol: 'CANCEL',
  },
  {
    id: 6,
    name: '草稿',
    symbol: 'DRAFT',
  },
]);

const SysFlowCopyForType = new DictionaryBase('抄送类型', [
  {
    id: 'user',
    name: '抄送人',
    symbol: 'USER',
  },
  {
    id: 'dept',
    name: '抄送部门',
    symbol: 'DEPT',
  },
  {
    id: 'role',
    name: '抄送角色',
    symbol: 'ROLE',
  },
  {
    id: 'deptPostLeader',
    name: '审批人部门领导',
    symbol: 'SELF_DEPT_LEADER',
  },
  {
    id: 'upDeptPostLeader',
    name: '审批人上级部门领导',
    symbol: 'UP_DEPT_LEADER',
  },
  {
    id: 'allDeptPost',
    name: '抄送岗位',
    symbol: 'POST',
  },
  {
    id: 'selfDeptPost',
    name: '审批人部门岗位',
    symbol: 'SELF_DEPT_POST',
  },
  {
    id: 'siblingDeptPost',
    name: '审批人同级部门岗位',
    symbol: 'SLIBING_DEPT_POST',
  },
  {
    id: 'upDeptPost',
    name: '审批人上级部门岗位',
    symbol: 'UP_DEPT_POST',
  },
  {
    id: 'deptPost',
    name: '指定部门岗位',
    symbol: 'DEPT_POST',
  },
]);

const FlowNodeType = new DictionaryBase('钉钉节点类型', [
  {
    id: 0,
    name: '发起人',
    symbol: 'ORIGINATOR',
  },
  {
    id: 1,
    name: '审批人',
    symbol: 'APPROVED_BY',
  },
  {
    id: 2,
    name: '抄送人',
    symbol: 'CC_TO',
  },
  {
    id: 3,
    name: '连接线',
    symbol: 'CONNECTING_LINE',
  },
  {
    id: 4,
    name: '条件分支',
    symbol: 'CONDITIONAL_BRANCH',
  },
  {
    id: 5,
    name: '并行分支',
    symbol: 'PARALLEL_BRANCH',
  },
  {
    id: 6,
    name: '服务任务',
    symbol: 'SERVICE_TASK',
  },
  {
    id: 7,
    name: '接收任务',
    symbol: 'RECEIVE_TASK',
  },
  {
    id: 100,
    name: '子流程',
    symbol: 'SUB_PROCESS',
  },
]);

const DiagramType = new DictionaryBase('', [
  {
    id: 0,
    name: 'Ordinary',
    symbol: 'ORDINARY',
  },
  {
    id: 1,
    name: 'DingDing',
    symbol: 'DINGDING',
  },
]);

const SysAutoCodeType = new DictionaryBase('自动编码类型', [
  {
    id: 'DAYS',
    name: '精确到日',
    symbol: 'DAYS',
  },
  {
    id: 'HOURS',
    name: '精确到时',
    symbol: 'HOURS',
  },
  {
    id: 'MINUTES',
    name: '精确到分',
    symbol: 'MINUTES',
  },
  {
    id: 'SECONDS',
    name: '精确到秒',
    symbol: 'SECONDS',
  },
]);
const FlowEntryType = new DictionaryBase('Process Type', [
  {
    id: 0,
    name: 'Normal',
    symbol: 'NORMAL',
  },
  {
    id: 1,
    name: 'Auto Task',
    symbol: 'AUTO_TASK',
  },
]);

const AutoTaskValueType = new DictionaryBase('自动化任务值类型', [
  {
    id: 0,
    name: '固定值',
    symbol: 'FIXED',
  },
  {
    id: 1,
    name: '源表字段',
    symbol: 'FIELD',
  },
  {
    id: 2,
    name: '流程变量',
    symbol: 'VARIABLE',
  },
]);

const AutoTaskActionType = new DictionaryBase('自动化任务动作类型', [
  {
    id: 0,
    name: '数据插入',
    symbol: 'INSERT',
  },
  {
    id: 1,
    name: '数据更新',
    symbol: 'UPDATE',
  },
  {
    id: 2,
    name: '数据删除',
    symbol: 'DELETE',
  },
  {
    id: 3,
    name: '单条查询',
    symbol: 'QUERY_SINGLE_DATA',
  },
  {
    id: 10,
    name: 'HTTP请求',
    symbol: 'HTTP',
  },
]);

export {
  SysFlowEntryPublishedStatus,
  SysFlowEntryBindFormType,
  SysFlowEntryStep,
  SysFlowTaskOperationType,
  SysFlowTaskType,
  SysFlowVariableType,
  SysFlowWorkOrderStatus,
  SysFlowCopyForType,
  DiagramType,
  FlowNodeType,
  SysAutoCodeType,
  FlowEntryType,
  AutoTaskValueType,
  AutoTaskActionType,
};
