<template>
  <div class="panel-tab__content" style="padding-top: 0">
    <div
      class="panel-tab__content--title"
      style="height: 32px; padding-bottom: 3px; box-sizing: content-box"
    >
      <span class="panel-title">Message List</span>
      <right-add-btn @click.prevent.stop="openModel('message')">Add</right-add-btn>
    </div>
    <vxe-table
      empty-text="No data"
      :data="messageList"
      :size="layoutStore.defaultFormItemSize"
      :row-config="{ isHover: true }"
      header-cell-class-name="table-header-gray"
    >
      <vxe-column type="seq" title="No." width="50px" />
      <vxe-column title="Message ID" field="id" max-width="300px" show-overflow-tooltip />
      <vxe-column title="Message Name" field="name" max-width="300px" show-overflow-tooltip />
      <template v-slot:empty>
        <div class="table-empty unified-font">
          <img src="@/assets/img/empty.png" />
          <span>No Data</span>
        </div>
      </template>
    </vxe-table>
    <div class="panel-tab__content--title" style="padding-top: 10px; padding-bottom: 3px">
      <span class="panel-title">Signal List</span>
      <right-add-btn @click.prevent.stop="openModel('signal')">Add</right-add-btn>
    </div>
    <vxe-table
      empty-text="No data"
      :data="signalList"
      :size="layoutStore.defaultFormItemSize"
      :row-config="{ isHover: true }"
      style="margin-bottom: 10px"
      header-cell-class-name="table-header-gray"
    >
      <vxe-column type="seq" title="No." width="50px" />
      <vxe-column title="Signal ID" field="id" max-width="300px" show-overflow-tooltip />
      <vxe-column title="Signal Name" field="name" max-width="300px" show-overflow-tooltip />
      <template v-slot:empty>
        <div class="table-empty unified-font">
          <img src="@/assets/img/empty.png" />
          <span>No Data</span>
        </div>
      </template>
    </vxe-table>

    <el-dialog
      v-model="modelVisible"
      :title="modelConfig.title"
      :close-on-click-modal="false"
      width="400px"
      append-to-body
      destroy-on-close
    >
      <el-form
        :model="modelObjectForm"
        :size="layoutStore.defaultFormItemSize"
        label-width="120px"
        @submit.prevent
      >
        <el-form-item :label="modelConfig.idLabel">
          <el-input v-model="modelObjectForm.id" clearable />
        </el-form-item>
        <el-form-item :label="modelConfig.nameLabel">
          <el-input v-model="modelObjectForm.name" clearable />
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <el-button :size="layoutStore.defaultFormItemSize" @click="modelVisible = false"
          >Cancel</el-button
        >
        <el-button :size="layoutStore.defaultFormItemSize" type="primary" @click="addNewObject"
          >Save</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessage } from 'element-plus';
import { VxeTable, VxeColumn } from 'vxe-table';
import RightAddBtn from '@/components/Btns/RightAddBtn.vue';
import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();

const signalList = ref([]);
const messageList = ref([]);
const modelVisible = ref(false);
const modelType = ref('');
const modelObjectForm = ref({});

let rootElements = [];
let messageIdMap = {};
let signalIdMap = {};

const modelConfig = computed(() => {
  if (modelType.value === 'message') {
    return { title: 'Create Message', idLabel: 'Message ID', nameLabel: 'Message Name' };
  } else {
    return { title: 'Create Signal', idLabel: 'Signal ID', nameLabel: 'Signal Name' };
  }
});

const openModel = (type: string) => {
  modelType.value = type;
  modelObjectForm.value = {};
  modelVisible.value = true;
};

const initDataList = () => {
  rootElements = window.bpmnInstances.modeler.getDefinitions().rootElements;
  messageIdMap = {};
  signalIdMap = {};
  messageList.value = [];
  signalList.value = [];
  rootElements.forEach(el => {
    if (el.$type === 'bpmn:Message') {
      messageIdMap[el.id] = true;
      messageList.value.push({ ...el });
    }
    if (el.$type === 'bpmn:Signal') {
      signalIdMap[el.id] = true;
      signalList.value.push({ ...el });
    }
  });
};

const addNewObject = () => {
  if (modelType.value === 'message') {
    if (messageIdMap[modelObjectForm.value.id]) {
      return ElMessage.error('This message already exists. Please modify the ID and save again.');
    }
    const messageRef = window.bpmnInstances.moddle.create('bpmn:Message', modelObjectForm.value);
    rootElements.push(messageRef);
  } else {
    if (signalIdMap[modelObjectForm.value.id]) {
      return ElMessage.error('This signal already exists. Please modify the ID and save again.');
    }
    const signalRef = window.bpmnInstances.moddle.create('bpmn:Signal', modelObjectForm.value);
    rootElements.push(signalRef);
  }
  modelVisible.value = false;
  initDataList();
};

onMounted(() => {
  initDataList();
});
</script>

<style lang="scss" scoped>
.panel-tab__content--title {
  align-items: center;
  padding-bottom: 0;
  .el-button {
    display: flex;
    align-items: center;
    :deep(.el-icon-circle-plus) {
      font-size: 16px;
    }
    :deep(span) {
      margin-left: 2px !important;
    }
  }
}
</style>
