<template>
  <div class="panel-tab__content" style="padding-top: 3px">
    <vxe-table
      empty-text="No data"
      :data="elementPropertyList"
      :size="(layoutStore.defaultFormItemSize as SizeType)"
      :row-config="{ isHover: true }"
      max-height="240"
      fit
      header-cell-class-name="table-header-gray"
    >
      <vxe-column title="No." width="50px" type="seq" />
      <vxe-column title="Property Name" field="name" min-width="100px" show-overflow-tooltip />
      <vxe-column title="Property Value" field="value" min-width="90px" show-overflow-tooltip />
      <vxe-column title="Operation" width="110px">
        <template v-slot="{ row, $rowIndex }">
          <el-button
            type="primary"
            :size="layoutStore.defaultFormItemSize"
            link
            @click="openAttributesForm(row, $rowIndex)"
            >Edit</el-button
          >
          <!-- <el-divider direction="vertical" /> -->
          <el-button
            :size="layoutStore.defaultFormItemSize"
            link
            type="danger"
            @click="removeAttributes(row, $rowIndex)"
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
    <el-dialog
      v-model="propertyFormModelVisible"
      title="Attribute Configuration"
      width="600px"
      append-to-body
      destroy-on-close
    >
      <el-form
        :model="propertyForm"
        label-width="120px"
        :size="layoutStore.defaultFormItemSize"
        ref="attributeFormRef"
        @submit.prevent
      >
        <el-form-item label="Property Name:" prop="name">
          <el-input v-model="propertyForm.name" clearable />
        </el-form-item>
        <el-form-item label="Property Value:" prop="value">
          <el-input v-model="propertyForm.value" clearable />
        </el-form-item>
      </el-form>
      <template v-slot:footer>
        <el-button :size="layoutStore.defaultFormItemSize" @click="propertyFormModelVisible = false"
          >Cancel</el-button
        >
        <el-button :size="layoutStore.defaultFormItemSize" type="primary" @click="saveAttribute"
          >OK</el-button
        >
      </template>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus';
import { VxeTable, VxeColumn, SizeType } from 'vxe-table';
import { ANY_OBJECT } from '@/types/generic';

import { useLayoutStore } from '@/store';
const layoutStore = useLayoutStore();

const props = defineProps<{ id: string; type: string }>();
const prefix = inject('prefix');

console.log('prefix', prefix);

const attributeFormRef = ref();

const elementPropertyList = ref([]);
const propertyForm = ref<ANY_OBJECT>({});
const editingPropertyIndex = ref(-1);
const propertyFormModelVisible = ref(false);

let bpmnElement: ANY_OBJECT = {};
let otherExtensionList: ANY_OBJECT[] = [];
let bpmnElementPropertyList: ANY_OBJECT[] = [];

const win: ANY_OBJECT = window;

const resetAttributesList = () => {
  bpmnElement = win.bpmnInstances.bpmnElement;
  otherExtensionList = []; // Other extension configurations
  const bpmnElementProperties =
    bpmnElement.businessObject?.extensionElements?.values?.filter(ex => {
      if (ex.$type !== `${prefix}:Properties`) {
        otherExtensionList.push(ex);
      }
      return ex.$type === `${prefix}:Properties`;
    }) ?? [];

  // Save all the extended attribute fields
  bpmnElementPropertyList = bpmnElementProperties.reduce(
    (pre: ANY_OBJECT[], current: ANY_OBJECT) => {
      if (current.values != null) pre = pre.concat(current.values);
      return pre;
    },
    [],
  );
  // Copy for display
  elementPropertyList.value = JSON.parse(JSON.stringify(bpmnElementPropertyList ?? []));
};
const openAttributesForm = (attr: ANY_OBJECT, index: number) => {
  editingPropertyIndex.value = index;
  propertyForm.value = index === -1 ? {} : JSON.parse(JSON.stringify(attr));
  propertyFormModelVisible.value = true;
  nextTick(() => {
    if (attributeFormRef.value) attributeFormRef.value.clearValidate();
  });
};
const removeAttributes = (attr: ANY_OBJECT, index: number) => {
  ElMessageBox.confirm('Are you sure you want to remove this attribute?', 'Tip', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      elementPropertyList.value.splice(index, 1);
      bpmnElementPropertyList.splice(index, 1);
      // Create a new attributes field save list
      const propertiesObject = win.bpmnInstances.moddle.create(`${prefix}:Properties`, {
        values: bpmnElementPropertyList,
      });
      updateElementExtensions(propertiesObject);
      resetAttributesList();
    })
    .catch(() => console.info('Operation canceled'));
};
const saveAttribute = () => {
  let propertiesObject = [];
  if (editingPropertyIndex.value === -1) {
    // New attribute field
    const newPropertyObject = win.bpmnInstances.moddle.create(
      `${prefix}:Property`,
      propertyForm.value,
    );
    // Create a new attributes field save list
    propertiesObject = win.bpmnInstances.moddle.create(`${prefix}:Properties`, {
      values: bpmnElementPropertyList.concat([newPropertyObject]),
    });
  } else {
    // Modify attribute field
    propertiesObject = win.bpmnInstances.moddle.create(`${prefix}:Properties`, {
      values: elementPropertyList.value.map((item, index) => {
        if (index !== editingPropertyIndex.value) {
          return win.bpmnInstances.moddle.create(`${prefix}:Property`, {
            name: item.name,
            value: item.value,
          });
        } else {
          return win.bpmnInstances.moddle.create(`${prefix}:Property`, {
            name: propertyForm.value.name,
            value: propertyForm.value.value,
          });
        }
      }),
    });
  }
  updateElementExtensions(propertiesObject);
  propertyFormModelVisible.value = false;
  resetAttributesList();
};
const updateElementExtensions = (properties: ANY_OBJECT) => {
  const extensions = win.bpmnInstances.moddle.create('bpmn:ExtensionElements', {
    values: otherExtensionList.concat([properties]),
  });
  win.bpmnInstances.modeling.updateProperties(bpmnElement, {
    extensionElements: extensions,
  });
};

watch(
  () => props.id,
  val => {
    val && val.length && resetAttributesList();
  },
  {
    immediate: true,
  },
);

defineExpose({ openAttributesForm });
</script>
