import { ANY_OBJECT } from '@/types/generic';

/**
 * This is a sample file that should be replaced with the actual translation.
 *
 * Checkout https://github.com/bpmn-io/bpmn-js-i18n for a list of available
 * translations and labels to translate.
 */
const translationsCN: ANY_OBJECT = {
  // 添加部分
  'Append EndEvent': 'Append end event',
  'Append Gateway': 'Append gateway',
  'Append Task': 'Append task',
  'Append Intermediate/Boundary Event': 'Append intermediate/boundary event',

  'Activate the global connect tool': 'Active global connect tool',
  'Append {type}': 'Append {type}',
  'Add Lane above': 'Add lane above',
  'Divide into two Lanes': 'Divide into two Lanes',
  'Divide into three Lanes': 'Divide into three Lanes',
  'Add Lane below': 'Add lane below',
  'Append compensation activity': '追加补偿活动',
  'Change type': 'Change element',
  'Connect using Association': '使用关联连接',
  'Connect using Sequence/MessageFlow or Association': 'Connect to other element',
  'Connect using DataInputAssociation': '使用数据输入关联连接',
  Remove: 'Delete',
  'Activate the hand tool': 'Activate hand tool',
  'Activate the lasso tool': 'Activate lasso tool',
  'Activate the create/remove space tool': 'Activate create/remove space tool',
  'Create expanded SubProcess': 'Create expanded sub-process',
  'Create IntermediateThrowEvent/BoundaryEvent': '创建中间抛出事件/边界事件',
  'Create Pool/Participant': 'Create pool/participant',
  'Parallel Multi Instance': 'Parallel multi-instance',
  'Sequential Multi Instance': 'Sequential multi-instance',
  DataObjectReference: '数据对象参考',
  DataStoreReference: '数据存储参考',
  Loop: 'Loop',
  'Ad-hoc': 'Ad-hoc',
  'Create {type}': '创建 {type}',
  Task: 'Task',
  'Send Task': 'Send task',
  'Receive Task': 'Receive task',
  'User Task': 'User task',
  'Manual Task': 'Manual task',
  'Business Rule Task': 'Business rule task',
  'Service Task': 'Service task',
  'Script Task': 'Script task',
  'Call Activity': 'Call activity',
  'Sub Process (collapsed)': 'Sub-process (collapsed)',
  'Sub Process (expanded)': 'Sub-process (expanded)',
  'Start Event': 'Start event',
  StartEvent: 'Start event',
  'Intermediate Throw Event': 'Intermediate throw event',
  'End Event': 'End event',
  EndEvent: 'End event',
  'Create StartEvent': 'Create start event',
  'Create EndEvent': 'Create end event',
  'Create Task': '创建任务',
  'Create User Task': 'Create user task',
  'Create Gateway': 'Create gateway',
  'Create DataObjectReference': 'Create data object reference',
  'Create DataStoreReference': 'Create data store reference',
  'Create Group': 'Create group',
  'Create Intermediate/Boundary Event': 'Create intermediate/boundary event',
  'Message Start Event': 'Message start event',
  'Timer Start Event': 'Timer start event',
  'Conditional Start Event': 'Conditional start event',
  'Signal Start Event': 'Signal start event',
  'Error Start Event': '错误开始事件',
  'Escalation Start Event': '升级开始事件',
  'Compensation Start Event': '补偿开始事件',
  'Message Start Event (non-interrupting)': '消息开始事件（非中断）',
  'Timer Start Event (non-interrupting)': '定时开始事件（非中断）',
  'Conditional Start Event (non-interrupting)': '条件开始事件（非中断）',
  'Signal Start Event (non-interrupting)': '信号开始事件（非中断）',
  'Escalation Start Event (non-interrupting)': '升级开始事件（非中断）',
  'Message Intermediate Catch Event': 'Message intermediate catch event',
  'Message Intermediate Throw Event': 'Message intermediate throw event',
  'Timer Intermediate Catch Event': 'Timer intermediate catch event',
  'Escalation Intermediate Throw Event': 'Escalation intermediate throw event',
  'Conditional Intermediate Catch Event': 'Conditional intermediate catch event',
  'Link Intermediate Catch Event': 'Link intermediate catch event',
  'Link Intermediate Throw Event': 'Link intermediate throw event',
  'Compensation Intermediate Throw Event': 'Compensation intermediate throw event',
  'Signal Intermediate Catch Event': 'Signal intermediate catch event',
  'Signal Intermediate Throw Event': 'Signal intermediate throw event',
  'Message End Event': 'Message end event',
  'Escalation End Event': 'Escalation end event',
  'Error End Event': 'Error end event',
  'Cancel End Event': '取消结束事件',
  'Compensation End Event': 'Compensation end event',
  'Signal End Event': 'Signal end event',
  'Terminate End Event': 'Terminate end event',
  'Message Boundary Event': '消息边界事件',
  'Message Boundary Event (non-interrupting)': '消息边界事件（非中断）',
  'Timer Boundary Event': '定时边界事件',
  'Timer Boundary Event (non-interrupting)': '定时边界事件（非中断）',
  'Escalation Boundary Event': '升级边界事件',
  'Escalation Boundary Event (non-interrupting)': '升级边界事件（非中断）',
  'Conditional Boundary Event': '条件边界事件',
  'Conditional Boundary Event (non-interrupting)': '条件边界事件（非中断）',
  'Error Boundary Event': '错误边界事件',
  'Cancel Boundary Event': '取消边界事件',
  'Signal Boundary Event': '信号边界事件',
  'Signal Boundary Event (non-interrupting)': '信号边界事件（非中断）',
  'Compensation Boundary Event': '补偿边界事件',
  'Exclusive Gateway': '互斥网关',
  'Parallel Gateway': 'Parallel gateway',
  'Inclusive Gateway': 'Inclusive gateway',
  'Complex Gateway': 'Complex gateway',
  'Event based Gateway': 'Event-based gateway',
  Transaction: 'Transaction',
  'Sub Process': '子流程',
  'Event Sub Process': 'Event sub-process',
  'Collapsed Pool': 'Empty pool/participant (removes content)',
  'Expanded Pool': '展开池',

  // Errors
  'no parent for {element} in {parent}': '在{parent}里，{element}没有父类',
  'no shape type specified': '没有指定的形状类型',
  'flow elements must be children of pools/participants': 'flow elements must be children of pools/participants',
  'out of bounds release': 'out of bounds release',
  'more than {count} child lanes': '子道大于{count} ',
  'element required': '元素不能为空',
  'diagram not part of bpmn:Definitions': '流程图不符合bpmn规范',
  'no diagram to display': '没有可展示的流程图',
  'no process or collaboration to display': '没有可展示的流程/协作',
  'element {element} referenced by {referenced}#{property} not yet drawn':
    '由{referenced}#{property}引用的{element}元素仍未绘制',
  'already rendered {element}': '{element} 已被渲染',
  'failed to import {element}': '导入{element}失败',
  //属性面板的参数
  Id: '编号',
  Name: '名称',
  General: '常规',
  Details: '详情',
  'Message Name': '消息名称',
  Message: '消息',
  Initiator: '创建者',
  'Asynchronous Continuations': '持续异步',
  'Asynchronous Before': '异步前',
  'Asynchronous After': '异步后',
  'Job Configuration': '工作配置',
  Exclusive: '排除',
  'Job Priority': '工作优先级',
  'Retry Time Cycle': '重试时间周期',
  Documentation: '文档',
  'Element Documentation': '元素文档',
  'History Configuration': '历史配置',
  'History Time To Live': '历史的生存时间',
  Forms: '表单',
  'Form Key': '表单key',
  'Form Fields': '表单字段',
  'Business Key': '业务key',
  'Form Field': '表单字段',
  ID: '编号',
  Type: '类型',
  Label: '名称',
  'Default Value': '默认值',
  'Default Flow': '默认流转路径',
  'Conditional Flow': '条件流转路径',
  'Sequence Flow': '普通流转路径',
  Validation: '校验',
  'Add Constraint': '添加约束',
  Config: '配置',
  Properties: '属性',
  'Add Property': '添加属性',
  Value: '值',
  Listeners: '监听器',
  'Execution Listener': '执行监听',
  'Event Type': '事件类型',
  'Listener Type': '监听器类型',
  'Java Class': 'Java类',
  Expression: '表达式',
  'Must provide a value': '必须提供一个值',
  'Delegate Expression': '代理表达式',
  Script: '脚本',
  'Script Format': '脚本格式',
  'Script Type': '脚本类型',
  'Inline Script': '内联脚本',
  'External Script': '外部脚本',
  Resource: '资源',
  'Field Injection': '字段注入',
  Extensions: '扩展',
  'Input/Output': '输入/输出',
  'Input Parameters': '输入参数',
  'Output Parameters': '输出参数',
  Parameters: '参数',
  'Output Parameter': '输出参数',
  'Timer Definition Type': '定时器定义类型',
  'Timer Definition': '定时器定义',
  Date: '日期',
  Duration: '持续',
  Cycle: '循环',
  Signal: '信号',
  'Signal Name': '信号名称',
  Escalation: '升级',
  Error: '错误',
  'Link Name': '链接名称',
  Condition: '条件名称',
  'Variable Name': '变量名称',
  'Variable Event': '变量事件',
  'Specify more than one variable change event as a comma separated list.':
    '多个变量事件以逗号隔开',
  'Wait for Completion': '等待完成',
  'Activity Ref': '活动参考',
  'Version Tag': '版本标签',
  Executable: '可执行文件',
  'External Task Configuration': '扩展任务配置',
  'Task Priority': '任务优先级',
  External: '外部',
  Connector: '连接器',
  'Must configure Connector': '必须配置连接器',
  'Connector Id': '连接器编号',
  Implementation: '实现方式',
  'Field Injections': '字段注入',
  Fields: '字段',
  'Result Variable': '结果变量',
  Topic: '主题',
  'Configure Connector': '配置连接器',
  'Input Parameter': '输入参数',
  Assignee: '代理人',
  'Candidate Users': '候选用户',
  'Candidate Groups': '候选组',
  'Due Date': '到期时间',
  'Follow Up Date': '跟踪日期',
  Priority: '优先级',
  'The follow up date as an EL expression (e.g. ${someDate} or an ISO date (e.g. 2015-06-26T09:54:00)':
    '跟踪日期必须符合EL表达式，如： ${someDate} ,或者一个ISO标准日期，如：2015-06-26T09:54:00',
  'The due date as an EL expression (e.g. ${someDate} or an ISO date (e.g. 2015-06-26T09:54:00)':
    '跟踪日期必须符合EL表达式，如： ${someDate} ,或者一个ISO标准日期，如：2015-06-26T09:54:00',
  Variables: '变量',
  'Candidate Starter Configuration': '候选人起动器配置',
  'Candidate Starter Groups': '候选人起动器组',
  'This maps to the process definition key.': '这映射到流程定义键。',
  'Candidate Starter Users': '候选人起动器的用户',
  'Specify more than one user as a comma separated list.': '指定多个用户作为逗号分隔的列表。',
  'Tasklist Configuration': 'Tasklist配置',
  Startable: '启动',
  'Specify more than one group as a comma separated list.': '指定多个组作为逗号分隔的列表。',
};

export default translationsCN;
