<template>
  <HandlerFlowTask
    ref="handlerFlowTaskRef"
    :processInstanceId="dialogParams.processInstanceId"
    :processDefinitionId="dialogParams.processDefinitionId"
    :flowEntryName="dialogParams.flowEntryName"
    :processInstanceInitiator="dialogParams.processInstanceInitiator"
    :taskId="dialogParams.taskId"
    :taskName="dialogParams.taskName"
    :operationList="dialogParams.operationList"
    :isRuntime="isRuntime"
    :isDraft="isDraft"
    @close="handlerClose"
    @start="handlerStart"
    @submit="handlerOperation"
    @draft="handlerDraft"
  >
    <!-- Online Form Page -->
    <template v-slot:formInfo>
      <div v-for="(value, key) in formInfo" :key="key" class="form-info">
        <div class="label">{{ FormInfo[key] }}</div>
        <div class="value">{{ value }}</div>
      </div>
    </template>
    <!-- <WorkflowForm
      v-if="dialogParams.routerName == null && dialogParams.formId != null"
      ref="workflowFormRef"
      style="width: 100%"
      :style="{ height: mainContextHeight - 188 + 'px' }"
      :formId="dialogParams.formId"
      :readOnly="readOnly"
      :flowInfo="getFlowInfo"
    /> -->
    <!-- Route Page -->
    <router-view v-slot:="{ Component, route }">
      <component
        :is="Component"
        ref="routerFlowForm"
        :key="route.path"
        style="width: 100%"
        :isRuntimeTask="isRuntime"
        :isDraft="isDraft"
        :readOnly="readOnly"
        :processInstanceId="dialogParams.processInstanceId"
        :taskId="dialogParams.taskId"
        :taskVariableList="variableList"
      />
    </router-view>
  </HandlerFlowTask>
</template>

<script setup lang="ts">
import { ElMessageBox, ElMessage } from 'element-plus';
import axios from 'axios';
import _ from 'lodash';
import WorkflowForm from '@/pages/online/OnlinePageRender/index.vue';
import { serverDefaultCfg } from '@/common/http/config';
import { FlowOperationController } from '@/api/flow';
import { ANY_OBJECT } from '@/types/generic';
import { SysFlowTaskOperationType } from '@/common/staticDict/flow';
import { useMessage } from '@/store';
import { FormInfo } from '@/common/enum/useForm';
import HandlerFlowTask from '../components/HandlerFlowTask.vue';
import { useFlowAction } from './hook';
import { IProp } from './types';

const props: IProp = defineProps<IProp>();

const mainContextHeight = inject('mainContextHeight', 200);

const workflowFormRef = ref();
const routerFlowForm = ref();
const formInfo = ref({});
const isStart = ref(false);
// Save taskId after drafting process
const draftTaskId = ref<string>();
// Save process instance ID after drafting
const draftProcessInstanceId = ref<string>();
// Online form page data
const formData = ref<ANY_OBJECT>();
// Online form page one-to-many data
const oneToManyRelationData = ref<ANY_OBJECT>();

const { handlerFlowTaskRef, preHandlerOperation, submitConsign, handlerClose, dialogParams } =
  useFlowAction(props);

const isOnlineForm = computed(() => {
  return !!dialogParams.value.formId;
});

const isDraft = computed(() => {
  return typeof dialogParams.value.isDraft === 'string'
    ? dialogParams.value.isDraft === 'true'
    : dialogParams.value.isDraft;
});
const isPreview = computed(() => {
  return typeof dialogParams.value.isPreview === 'string'
    ? dialogParams.value.isPreview === 'true'
    : dialogParams.value.isPreview;
});
const isRuntime = computed(() => {
  return typeof dialogParams.value.isRuntime === 'string'
    ? dialogParams.value.isRuntime === 'true'
    : dialogParams.value.isRuntime;
});
const readOnly = computed(() => {
  return typeof dialogParams.value.readOnly === 'string'
    ? dialogParams.value.readOnly === 'true'
    : dialogParams.value.readOnly;
});

const getFlowInfo = computed(() => {
  return {
    processInstanceId: dialogParams.value.processInstanceId,
    taskId: dialogParams.value.taskId,
    processDefinitionKey: dialogParams.value.processDefinitionKey,
    processInstanceInitiator: dialogParams.value.processInstanceInitiator,
    messageId: dialogParams.value.messageId,
    isRuntime: isRuntime.value,
    isDraft: isDraft.value,
  };
});
const variableList = computed(() => {
  let temp = dialogParams.value.variableList;
  const variableList = Array.isArray(temp) ? temp : JSON.parse(temp || '[]');
  return variableList;
});

const messageStore = useMessage();

/**
 * Get function from routed component
 * @param {string} functionName Function name
 * @returns {function}
 */
const getRouterCompomentFunction = (functionName: string) => {
  return routerFlowForm.value && typeof routerFlowForm.value[functionName] === 'function'
    ? routerFlowForm.value[functionName]
    : undefined;
};
/**
 * Get form data
 */
const getMasterData = (
  operationType: string,
  assignee: string | Array<string> | undefined,
): Promise<ANY_OBJECT> => {
  return new Promise((resolve, reject) => {
    // TODO workflowFormRef.value.getFormData does not need to be checked
    if (isOnlineForm.value && workflowFormRef.value.getFormData) {
      workflowFormRef.value
        .getFormData(false, variableList)
        .then((formData: ANY_OBJECT | null) => {
          console.log('handleerFlowTask.getMasterData Form Data', formData);
          if (formData == null) {
            reject();
            return;
          }
          const assigneeArr =
            assignee && assignee !== '' ? (assignee as string).split(',') : undefined;
          if (operationType === SysFlowTaskOperationType.MULTI_SIGN) {
            // Co-sign operation sets multi-instance assignee collection
            if (formData.taskVariableData == null) formData.taskVariableData = {};
            formData.taskVariableData.assigneeList = assigneeArr;
          } else if (operationType === SysFlowTaskOperationType.SET_ASSIGNEE) {
            // Set next task node assignee
            if (formData.taskVariableData == null) formData.taskVariableData = {};
            formData.taskVariableData.appointedAssignee = assigneeArr
              ? assigneeArr.join(',')
              : undefined;
          }
          resolve(formData);
        })
        .catch((e: Error) => {
          reject(e);
        });
    } else {
      // Get static form page's getMasterData function
      let funGetMasterData = getRouterCompomentFunction('getMasterData');
      return funGetMasterData ? funGetMasterData(variableList) : reject();
    }
  });
};
const preHandlerOperationThen = (
  operation: ANY_OBJECT,
  copyItemList: Array<ANY_OBJECT>,
  res: ANY_OBJECT | null,
) => {
  // Add or reduce operations
  // if (
  //   [
  //     SysFlowTaskOperationType.CO_SIGN,
  //     SysFlowTaskOperationType.SIGN_REDUCTION,
  //     SysFlowTaskOperationType.BFORE_CONSIGN,
  //     SysFlowTaskOperationType.AFTER_CONSIGN,
  //   ].includes(operation.type)
  // ) {
  //   // Serial co-sign before and after add parameters
  //   let before;
  //   if (
  //     operation.type === SysFlowTaskOperationType.BFORE_CONSIGN ||
  //     operation.type === SysFlowTaskOperationType.AFTER_CONSIGN
  //   ) {
  //     before = operation.type === SysFlowTaskOperationType.BFORE_CONSIGN;
  //   }
  //   submitConsign((res || {}).assignee, operation.type === SysFlowTaskOperationType.CO_SIGN, before)
  //     .then(() => {
  //       handlerClose();
  //     })
  //     .catch(e => {
  //       console.warn(e);
  //     });
  //   return;
  // }
  // // Reject operation
  // if (
  //   operation.type === SysFlowTaskOperationType.REJECT ||
  //   operation.type === SysFlowTaskOperationType.REJECT_TO_TASK
  // ) {
  //   FlowOperationController.rejectRuntimeTask({
  //     processInstanceId: dialogParams.value.processInstanceId,
  //     taskId: dialogParams.value.taskId,
  //     targetTaskKey: (res || {}).targetTaskKey,
  //     taskComment: (res || {}).message,
  //     taskVariableData: {
  //       latestApprovalStatus: operation.latestApprovalStatus,
  //     },
  //   })
  //     .then(() => {
  //       handlerClose();
  //     })
  //     .catch(e => {
  //       console.warn(e);
  //     });
  //   return;
  // }
  // Reject to start
  if (operation.type === SysFlowTaskOperationType.REJECT_TO_START) {
    FlowOperationController.rejectToStartUserTask({
      processInstanceId: dialogParams.value.processInstanceId,
      taskId: dialogParams.value.taskId,
      taskComment: (res || {}).message,
    })
      .then(() => {
        handlerClose();
      })
      .catch(e => {
        console.warn(e);
      });
    return;
  }
  // // Revoke operation
  // if (operation.type === SysFlowTaskOperationType.REVOKE) {
  //   ElMessageBox.confirm('Are you sure you want to revoke this task?', '', {
  //     confirmButtonText: 'Confirm',
  //     cancelButtonText: 'Cancel',
  //     type: 'warning',
  //   })
  //     .then(() => {
  //       FlowOperationController.revokeHistoricTask({
  //         processInstanceId: dialogParams.value.processInstanceId,
  //         taskId: dialogParams.value.taskId,
  //         taskComment: 'Task assignee revoked the task',
  //         taskVariableData: {
  //           latestApprovalStatus: operation.latestApprovalStatus,
  //         },
  //       })
  //         .then(() => {
  //           handlerClose();
  //         })
  //         .catch(e => {
  //           console.warn(e);
  //         });
  //     })
  //     .catch(e => {
  //       console.warn(e);
  //     });
  //   return;
  // }
  let params = {
    taskId: dialogParams.value.taskId || draftTaskId.value,
    processInstanceId: dialogParams.value.processInstanceId || draftProcessInstanceId.value,
    flowTaskCommentDto: {
      taskComment: (res || {}).message,
      approvalType: operation.type,
    },
  };

  FlowOperationController.submitUserTask(params)
    .then(() => {
      handlerClose();
      messageStore.reloadMessage();
      ElMessage.success('Submit successful!');
    })
    .catch(e => {
      console.warn(e);
    });
};
/**
 * Process operation
 * @param {Object} operation Process operation
 */
const handlerOperation = (
  operation: ANY_OBJECT,
  copyItemList: ANY_OBJECT[],
  xml?: string | undefined,
) => {
  preHandlerOperation(operation, isStart.value || isDraft.value, xml, copyItemList)
    .then(res => {
      preHandlerOperationThen(operation, copyItemList, res);
    })
    .catch(e => {
      console.warn(e);
    });
};
/**
 * Start process
 */
const handlerStart = (
  operation: ANY_OBJECT,
  copyItemList: ANY_OBJECT[],
  xml?: string | undefined,
) => {
  // Start and save draft then submit again
  if (draftProcessInstanceId.value != null && draftTaskId.value != null) {
    handlerOperation(operation, copyItemList, xml);
    return;
  }
  if (!isOnlineForm.value) {
    let funHandlerStart = getRouterCompomentFunction('handlerStart');
    if (funHandlerStart != null) {
      funHandlerStart(operation, copyItemList, xml)
        .then(() => {
          handlerClose();
        })
        .catch((e: Error) => {
          console.warn(e);
        });
    } else {
      ElMessage.error(
        'The current process has not implemented the start function, please contact the administrator!',
      );
    }
  } else {
    preHandlerOperation(operation, true, xml)
      .then(res => {
        getMasterData(operation.type, (res || {}).assignee)
          .then(formData => {
            FlowOperationController.startAndTakeUserTask(
              {
                processDefinitionKey: dialogParams.value.processDefinitionKey,
                masterData: formData.masterData || {},
                slaveData: formData.slaveData,
                taskVariableData: {
                  ...formData.taskVariableData,
                  latestApprovalStatus: operation.latestApprovalStatus,
                },
                flowTaskCommentDto: {
                  approvalType: operation.type,
                },
                copyData: (copyItemList || []).reduce((retObj, item) => {
                  retObj[item.type] = item.id;
                  return retObj;
                }, {}),
              },
              {
                // Check if it is started from process design
                processDefinitionKey: isPreview.value
                  ? undefined
                  : dialogParams.value.processDefinitionKey,
              },
            )
              .then(() => {
                handlerClose();
                ElMessage.success('Start successful!');
              })
              .catch(e => {
                console.warn(e);
              });
          })
          .catch(e => {
            console.warn(e);
          });
      })
      .catch(e => {
        console.warn(e);
      });
  }
};

/**
 * Get draft data
 */
const getDraftData = () => {
  return new Promise<ANY_OBJECT>((resolve, reject) => {
    workflowFormRef.value
      .getFormData(true)
      .then((res: ANY_OBJECT) => {
        resolve(res);
      })
      .catch((e: Error) => {
        reject(e);
      });
  });
};

/**
 * Save draft
 */
const handlerDraft = () => {
  if (isOnlineForm.value) {
    getDraftData()
      .then(formData => {
        if (formData == null) {
          return;
        }
        let params = {
          processDefinitionKey: dialogParams.value.processDefinitionKey,
          processInstanceId: dialogParams.value.processInstanceId || draftProcessInstanceId.value,
          masterData: formData.masterData,
          slaveData: formData.slaveData,
        };
        FlowOperationController.startAndSaveDraft(params, {
          processDefinitionKey: dialogParams.value.processDefinitionKey,
        })
          .then(res => {
            ElMessage.success('Draft save success');
            draftProcessInstanceId.value = res.data.processInstanceId;
            draftTaskId.value = res.data.taskId;
          })
          .catch(e => {
            console.warn(e);
          });
      })
      .catch(e => {
        console.warn(e);
      });
  } else {
    let funHandlerOperation = getRouterCompomentFunction('handlerDraft');
    if (funHandlerOperation) {
      funHandlerOperation().catch((e: Error) => {
        console.warn(e);
      });
    } else {
      ElMessage.error(
        'The current process has not implemented saving drafts, please contact the administrator!',
      );
    }
  }
};

/**
 * Initialize process form data
 */
const initFormData = async () => {
  try {
    const response = await axios.get(`${serverDefaultCfg.baseURL}order/orderPlacementInfo`, {
      params: {
        processInstanceId: dialogParams.value.processInstanceId,
      },
    });
    formInfo.value = _.omit(response.data[0], 'orderDataType', 'orderId', 'processInstanceId');
  } catch (error) {
    console.error('init form data:', error);
  }
};

onMounted(() => {
  initFormData();
});
</script>

<style lang="scss" scoped>
.form-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  height: 36px;
  line-height: 36px;
  color: $color-text-secondary;
  font-weight: 400;
  margin: 0px 50px 10px 20px;
  border-bottom: 1px solid #e8eaec;
}
.task-title {
  display: flex;
  justify-content: space-between;
  padding-bottom: 5px;
  margin-bottom: 10px;
  border-bottom: 3px solid #409eff;
}
.task-title .text {
  height: 28px;
  line-height: 28px;
  font-weight: 600;
  font-size: 16px;
  color: #383838;
}
.task-title .el-tag {
  margin-left: 10px;
}
</style>
