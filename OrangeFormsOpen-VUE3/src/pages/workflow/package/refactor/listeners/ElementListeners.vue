<template>
  <div class="panel-tab__content" style="padding-top: 3px">
    <vxe-table
      empty-text="No data"
      :data="elementListenersList"
      :size="(layoutStore.defaultFormItemSize as SizeType)"
      :row-config="{ isHover: true }"
      header-cell-class-name="table-header-gray"
    >
      <vxe-column title="No." width="50px" type="seq" />
      <vxe-column title="Event Type" min-width="100px" field="event" />
      <vxe-column title="Listener Type" min-width="100px" show-overflow-tooltip>
        <template v-slot="scope">
          {{ listenerTypeObject[scope.row.listenerType] }}
        </template>
      </vxe-column>
      <vxe-column title="Operation" width="110px">
        <template v-slot="{ row, $rowIndex }">
          <el-button
            :size="layoutStore.defaultFormItemSize"
            link
            type="primary"
            @click="openListenerForm(row, $rowIndex)"
            >Edit</el-button
          >
          <el-button
            :size="layoutStore.defaultFormItemSize"
            link
            type="danger"
            @click="removeListener(row, $rowIndex)"
            >Delete</el-button
          >
        </template>
      </vxe-column>
      <template v-slot:empty>
        <div class="table-empty unified-font">
          <img src="@/assets/img/empty.png" />
          <span>No Data</span>
        </div>
      </template>
    </vxe-table>
    <!-- Listener Edit/Create Section -->
    <el-drawer
      v-model="listenerFormModelVisible"
      title="Execute Listener"
      :size="`${width}px`"
      class="process-drawer"
      append-to-body
      destroy-on-close
    >
      <el-form
        :size="layoutStore.defaultFormItemSize"
        :model="listenerForm"
        label-width="120px"
        ref="listenerFormRef"
        @submit.prevent
      >
        <el-form-item
          label="Event Type"
          prop="event"
          :rules="{ required: true, message: 'Please select an event type', trigger: ['blur', 'change'] }"
        >
          <el-select v-model="listenerForm.event">
            <el-option label="start" value="start" />
            <el-option label="end" value="end" />
          </el-select>
        </el-form-item>
        <el-form-item
          label="Listener Type"
          prop="listenerType"
          :rules="{ required: true, message: 'Please select a listener type', trigger: ['blur', 'change'] }"
        >
          <el-select v-model="listenerForm.listenerType">
            <el-option
              v-for="i in Object.keys(listenerTypeObject)"
              :key="i"
              :label="listenerTypeObject[i]"
              :value="i"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="listenerForm.listenerType === 'classListener'"
          label="Java Class"
          prop="class"
          key="listener-class"
          :rules="{ required: true, message: 'Please enter Java class', trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerForm.class" clearable />
        </el-form-item>
        <el-form-item
          v-if="listenerForm.listenerType === 'expressionListener'"
          label="Expression"
          prop="expression"
          key="listener-expression"
          :rules="{ required: true, message: 'Please enter expression', trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerForm.expression" clearable />
        </el-form-item>
        <el-form-item
          v-if="listenerForm.listenerType === 'delegateExpressionListener'"
          label="Delegate Expression"
          prop="delegateExpression"
          key="listener-delegate"
          :rules="{ required: true, message: 'Please enter delegate expression', trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerForm.delegateExpression" clearable />
        </el-form-item>
        <template v-if="listenerForm.listenerType === 'scriptListener'">
          <el-form-item
            label="Script Format"
            prop="scriptFormat"
            key="listener-script-format"
            :rules="{ required: true, trigger: ['blur', 'change'], message: 'Please enter script format' }"
          >
            <el-input v-model="listenerForm.scriptFormat" clearable />
          </el-form-item>
          <el-form-item
            label="Script Type"
            prop="scriptType"
            key="listener-script-type"
            :rules="{ required: true, trigger: ['blur', 'change'], message: 'Please select script type' }"
          >
            <el-select v-model="listenerForm.scriptType">
              <el-option label="Inline Script" value="inlineScript" />
              <el-option label="External Script" value="externalScript" />
            </el-select>
          </el-form-item>
          <el-form-item
            v-if="listenerForm.scriptType === 'inlineScript'"
            label="Script Content"
            prop="value"
            key="listener-script"
            :rules="{ required: true, trigger: ['blur', 'change'], message: 'Please enter script content' }"
          >
            <el-input v-model="listenerForm.value" clearable />
          </el-form-item>
          <el-form-item
            v-if="listenerForm.scriptType === 'externalScript'"
            label="Resource URL"
            prop="resource"
            key="listener-resource"
            :rules="{ required: true, trigger: ['blur', 'change'], message: 'Please enter resource URL' }"
          >
            <el-input v-model="listenerForm.resource" clearable />
          </el-form-item>
        </template>
      </el-form>
      <el-divider />
      <p class="listener-filed__title" style="margin-bottom: 12px">
        <span
          ><el-icon><Menu /></el-icon>Injected Fields:</span
        >
        <el-button
          :size="layoutStore.defaultFormItemSize"
          type="primary"
          @click="openListenerFieldForm(null)"
          >Add Field</el-button
        >
      </p>
      <vxe-table
        empty-text="No data"
        :data="fieldsListOfListener"
        :size="(layoutStore.defaultFormItemSize as SizeType)"
        :row-config="{ isHover: true }"
        max-height="240"
        border
        fit
        style="flex: none"
        header-cell-class-name="table-header-gray"
      >
        <vxe-column title="No." width="50px" type="seq" />
        <vxe-column title="Field Name" min-width="100px" field="name" />
        <vxe-column title="Field Type" min-width="80px" show-overflow-tooltip>
          <template v-slot="scope">
            {{ fieldTypeObject[scope.row.fieldType] }}
          </template>
        </vxe-column>
        <vxe-column title="Field Value/Expression" min-width="100px" show-overflow-tooltip
          :formatter="(row:ANY_OBJECT) => row.string || row.expression"
        >
          <template v-slot="scope">
            {{ scope.row.string || scope.row.expression }}
          </template>
        </vxe-column>
        <vxe-column title="Operation" width="100px">
          <template v-slot="{ row, $rowIndex }">
            <el-button
              :size="layoutStore.defaultFormItemSize"
              link
              type="primary"
              @click="openListenerFieldForm(row, $rowIndex)"
              >Edit</el-button
            >
            <el-button
              :size="layoutStore.defaultFormItemSize"
              link
              type="danger"
              @click="removeListenerField(row, $rowIndex)"
              >Delete</el-button
            >
          </template>
        </vxe-column>
        <template v-slot:empty>
          <div class="table-empty unified-font">
            <img src="@/assets/img/empty.png" />
            <span>No Data</span>
          </div>
        </template>
      </vxe-table>

      <div class="element-drawer__button" style="margin-top: 8px">
        <el-button :size="layoutStore.defaultFormItemSize" @click="listenerFormModelVisible = false"
          >Cancel</el-button
        >
        <el-button
          :size="layoutStore.defaultFormItemSize"
          type="primary"
          @click="saveListenerConfig"
          >Save</el-button
        >
      </div>
    </el-drawer>

    <!-- Injected Field Edit/Create Section -->
    <el-dialog
      title="Field Configuration"
      v-model="listenerFieldFormModelVisible"
      width="600px"
      append-to-body
      destroy-on-close
    >
      <el-form
        :model="listenerFieldForm"
        :size="layoutStore.defaultFormItemSize"
        label-width="120px"
        ref="listenerFieldFormRef"
        @submit.prevent
      >
        <el-form-item
          label="Field Name:"
          prop="name"
          :rules="{ required: true, message: 'Please enter field name', trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerFieldForm.name" clearable />
        </el-form-item>
        <el-form-item
          label="Field Type:"
          prop="fieldType"
          :rules="{ required: true, message: 'Please select field type', trigger: ['blur', 'change'] }"
        >
          <el-select v-model="listenerFieldForm.fieldType">
            <el-option
              v-for="i in Object.keys(fieldTypeObject)"
              :key="i"
              :label="fieldTypeObject[i]"
              :value="i"
            />
          </el-select>
        </el-form-item>
        <el-form-item
          v-if="listenerFieldForm.fieldType === 'string'"
          label="Field Value:"
          prop="string"
          key="field-string"
          :rules="{ required: true, message: 'Please enter field value', trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerFieldForm.string" clearable />
        </el-form-item>
        <el-form-item
          v-if="listenerFieldForm.fieldType === 'expression'"
          label="Expression:"
          prop="expression"
          key="field-expression"
          :rules="{ required: true, message: 'Please enter expression', trigger: ['blur', 'change'] }"
        >
          <el-input v-model="listenerFieldForm.expression" clearable type="textarea" />
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <el-button
          :size="layoutStore.defaultFormItemSize"
          @click="listenerFieldFormModelVisible = false"
          >Cancel</el-button
        >
        <el-button :size="layoutStore.defaultFormItemSize" type="primary" @click="saveListenerFiled"
          >Confirm</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus';
import { Menu } from '@element-plus/icons-vue';
import { VxeTable, VxeColumn, SizeType } from 'vxe-table';
import { ANY_OBJECT } from '@/types/generic';
import { useLayoutStore } from '@/store';
import { createListenerObject, updateElementExtensions } from '../../utils';
import { initListenerType, initListenerForm, listenerType, fieldType } from './utilSelf';

const layoutStore = useLayoutStore();

const props = defineProps<{ id: string; type: string }>();
const prefix = inject('prefix', '');
const width = inject('width');

const listenerFormRef = ref();
const listenerFieldFormRef = ref();

const elementListenersList = ref<ANY_OBJECT[]>([]); // Listener list
const listenerForm = ref<ANY_OBJECT>({}); // Listener detail form
const listenerFormModelVisible = ref(false); // Listener edit sidebar visibility status
const fieldsListOfListener = ref<ANY_OBJECT[]>([]);
const listenerFieldForm = ref<ANY_OBJECT>({}); // Listener injected field detail form
const listenerFieldFormModelVisible = ref(false); // Listener injected field form modal visibility status
const editingListenerIndex = ref(-1); // Listener index, -1 for new
const editingListenerFieldIndex = ref(-1); // Field index, -1 for new
const listenerTypeObject = ref<ANY_OBJECT>(listenerType);
const fieldTypeObject = ref<ANY_OBJECT>(fieldType);

let bpmnElementListeners: ANY_OBJECT[] = [];
let bpmnElement: ANY_OBJECT = {};
let otherExtensionList: ANY_OBJECT[] = [];
const win: ANY_OBJECT = window;

const getOtherExtensionList = () => {
  otherExtensionList = [];
  let temp =
    bpmnElement.businessObject?.extensionElements?.values?.filter(ex => {
      if (ex.$type !== `${prefix}:ExecutionListener`) {
        otherExtensionList.push(ex);
      }
      return ex.$type === `${prefix}:ExecutionListener`;
    }) ?? [];
  return temp;
};
const resetListenersList = () => {
  bpmnElement = win.bpmnInstances.bpmnElement;
  otherExtensionList = [];
  bpmnElementListeners = getOtherExtensionList();
  elementListenersList.value = bpmnElementListeners.map(listener => initListenerType(listener));
};
// Open listener detail sidebar
const openListenerForm = (listener: ANY_OBJECT, index: number) => {
  if (listener) {
    listenerForm.value = initListenerForm(listener);
    editingListenerIndex.value = index;
  } else {
    listenerForm.value = {};
    editingListenerIndex.value = -1; // Mark as new
  }
  if (listener && listener.fields) {
    fieldsListOfListener.value = listener.fields.map((field: ANY_OBJECT) => ({
      ...field,
      fieldType: field.string ? 'string' : 'expression',
    }));
  } else {
    fieldsListOfListener.value = [];
    const val = { ...listenerForm.value };
    val.fields = [];
    listenerForm.value = val;
  }
  // Open sidebar and clear validation status
  listenerFormModelVisible.value = true;
  nextTick(() => {
    if (listenerFormRef.value) listenerFormRef.value.clearValidate();
  });
};
// Open listener field edit modal
const openListenerFieldForm = (field: ANY_OBJECT | null, index = 0) => {
  listenerFieldForm.value = field ? JSON.parse(JSON.stringify(field)) : {};
  editingListenerFieldIndex.value = field ? index : -1;
  listenerFieldFormModelVisible.value = true;
  nextTick(() => {
    if (listenerFieldFormRef.value) listenerFieldFormRef.value.clearValidate();
  });
};
// Save listener injected field
const saveListenerFiled = async () => {
  let validateStatus = await listenerFieldFormRef.value.validate();
  if (!validateStatus) return; // Directly return if validation fails
  if (editingListenerFieldIndex.value === -1) {
    fieldsListOfListener.value.push(listenerFieldForm.value);
    listenerForm.value.fields.push(listenerFieldForm.value);
  } else {
    fieldsListOfListener.value.splice(editingListenerFieldIndex.value, 1, listenerFieldForm.value);
    listenerForm.value.fields.splice(editingListenerFieldIndex.value, 1, listenerFieldForm.value);
  }
  listenerFieldFormModelVisible.value = false;
  nextTick(() => (listenerFieldForm.value = {}));
};
// Remove listener field
const removeListenerField = (field: ANY_OBJECT, index: number) => {
  ElMessageBox.confirm('Are you sure you want to remove this field?', 'Tip', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      fieldsListOfListener.value.splice(index, 1);
      listenerForm.value.fields.splice(index, 1);
    })
    .catch(() => console.info('Operation canceled'));
};
// Remove listener
const removeListener = (listener: ANY_OBJECT, index: number) => {
  ElMessageBox.confirm('Are you sure you want to remove this listener?', 'Tip', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      getOtherExtensionList();
      bpmnElementListeners.splice(index, 1);
      elementListenersList.value.splice(index, 1);
      updateElementExtensions(bpmnElement, otherExtensionList.concat(bpmnElementListeners));
    })
    .catch(() => console.info('Operation canceled'));
};
// Save listener configuration
const saveListenerConfig = async () => {
  let validateStatus = await listenerFormRef.value.validate();
  if (!validateStatus) return; // Directly return if validation fails
  const listenerObject = createListenerObject(listenerForm.value, false, prefix);
  if (editingListenerIndex.value === -1) {
    bpmnElementListeners.push(listenerObject);
    elementListenersList.value.push(listenerForm.value);
  } else {
    bpmnElementListeners.splice(editingListenerIndex.value, 1, listenerObject);
    elementListenersList.value.splice(editingListenerIndex.value, 1, listenerForm.value);
  }
  getOtherExtensionList();
  updateElementExtensions(bpmnElement, otherExtensionList.concat(bpmnElementListeners));
  // Hide sidebar
  listenerFormModelVisible.value = false;
  listenerForm.value = {};
};

watch(
  () => props.id,
  val => {
    val && val.length && nextTick(() => resetListenersList());
  },
  {
    immediate: true,
  },
);

defineExpose({ openListenerForm });
</script>
