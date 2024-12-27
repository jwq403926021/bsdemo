import { useRoute, useRouter } from 'vue-router';
import { ElMessage } from 'element-plus';
import { ANY_OBJECT } from '@/types/generic';
import { FlowOperationController } from '@/api/flow';
import { Dialog } from '@/components/Dialog';
import { SysFlowTaskOperationType } from '@/common/staticDict/flow';
import { useThirdParty } from '@/components/thirdParty/hooks';
import { useLayoutStore } from '@/store';
import TaskCommit from '../components/TaskCommit.vue';
import { IProp } from './types';

export const useFlowAction = (props: IProp) => {
  const layoutStore = useLayoutStore();
  const router = useRouter();
  const route = useRoute();
  const handlerFlowTaskRef = ref();
  const { thirdParams } = useThirdParty(props);

  const dialogParams = computed(() => {
    console.log('dialogParams props', props);
    let operationList =
      props.operationList == null || props.operationList === ''
        ? thirdParams.value.operationList
        : props.operationList;
    operationList = typeof operationList == 'string' ? JSON.parse(operationList) : operationList;
    return {
      isRuntime: props.isRuntime === 'true' || thirdParams.value.isRuntime || false,
      isDraft: props.isDraft === 'true' || thirdParams.value.isDraft || false,
      isPreview: props.isPreview === 'true' || thirdParams.value.isPreview || false,
      processDefinitionKey: props.processDefinitionKey || thirdParams.value.processDefinitionKey,
      formId: props.formId || thirdParams.value.formId,
      routerName: props.routerName || thirdParams.value.routerName,
      readOnly: props.readOnly === 'true' || thirdParams.value.readOnly,
      messageId: props.messageId || thirdParams.value.messageId,
      processInstanceId: props.processInstanceId || thirdParams.value.processInstanceId,
      processDefinitionId: props.processDefinitionId || thirdParams.value.processDefinitionId,
      taskId: props.taskId || thirdParams.value.taskId,
      flowEntryName: props.flowEntryName || thirdParams.value.flowEntryName,
      processInstanceInitiator:
        props.processInstanceInitiator || thirdParams.value.processInstanceInitiator,
      taskName: props.taskName || thirdParams.value.taskName,
      operationList: operationList,
      variableList:
        typeof props.variableList == 'string'
          ? JSON.parse(props.variableList)
          : props.variableList || thirdParams.value.variableList,
    };
  });
  // Add Sign
  const submitConsign = (
    assignee: Array<string> | string,
    isAdd = true,
    before: boolean | undefined = undefined,
  ) => {
    return new Promise((resolve, reject) => {
      const params = {
        taskId: dialogParams.value.taskId,
        processInstanceId: dialogParams.value.processInstanceId,
        newAssignees: Array.isArray(assignee) ? assignee : assignee.split(','),
        isAdd,
        before,
      };

      FlowOperationController.submitConsign(params)
        .then(() => {
          ElMessage.success(before != null || isAdd ? 'Add Sign Success!' : 'Reduce Sign Success!');
          resolve(true);
        })
        .catch(e => {
          console.warn('Add Sign Exception', e);
          reject();
        });
    });
  };
  // Close Flow Handling
  const handlerClose = () => {
    if (props.dialog) {
      props.dialog.cancel();
    } else {
      route.meta.refreshParentCachedPage = true;
      router.go(-1);
    }
  };
  // Preprocess Workflow Operation
  const preHandlerOperation = (
    operation: ANY_OBJECT | null,
    isStart: boolean,
    xml: string | undefined,
    copyItemList: Array<ANY_OBJECT> | null = null,
  ) => {
    return new Promise<ANY_OBJECT | null>((resolve, reject) => {
      if (operation == null) {
        isStart ? resolve(null) : reject();
        return;
      }
      // Revoke operation does not pop up selection window
      let showCommitDig =
        (!isStart && operation.type !== SysFlowTaskOperationType.REVOKE) ||
        operation.type === SysFlowTaskOperationType.SET_ASSIGNEE;
      if (operation.type === SysFlowTaskOperationType.MULTI_SIGN) {
        showCommitDig =
          !operation.multiSignAssignee ||
          !Array.isArray(operation.multiSignAssignee.assigneeList) ||
          operation.multiSignAssignee.assigneeList.length <= 0;
      }
      if (showCommitDig) {
        let title = 'Submit';
        if (!isStart) {
          switch (operation.type) {
            case SysFlowTaskOperationType.CO_SIGN:
            case SysFlowTaskOperationType.SIGN_REDUCTION:
            case SysFlowTaskOperationType.BFORE_CONSIGN:
            case SysFlowTaskOperationType.AFTER_CONSIGN:
              title = SysFlowTaskOperationType.getValue(operation.type);
              break;
            default:
              title = 'Approval';
              break;
          }
        }
        Dialog.show<ANY_OBJECT>(
          title,
          TaskCommit,
          {
            area: '500px',
          },
          {
            operation,
            rowData: {
              copyItemList,
              operation,
            },
            copyItemList,
            processInstanceId: dialogParams.value.processInstanceId,
            taskId: dialogParams.value.taskId,
            xml: xml,
            finishedInfo: (handlerFlowTaskRef.value || {}).finishedInfo,
            path: 'thirdTaskCommit',
          },
          {
            width: '500px',
            height: '380px',
            pathName: '/thirdParty/thirdTaskCommit',
          },
        )
          .then(res => {
            resolve(res);
          })
          .catch(e => {
            reject(e);
          });
      } else {
        resolve(null);
      }
    });
  };

  return {
    handlerFlowTaskRef,
    dialogParams,
    submitConsign,
    handlerClose,
    preHandlerOperation,
  };
};
