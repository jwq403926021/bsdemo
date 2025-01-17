<template>
  <el-row class="online-custom-block" :gutter="0">
    <el-col>
      <el-row>
        <el-steps style="width: 500px; margin: 20px auto" :active="active" align-center>
          <el-step>
            <template v-slot:title>
              <div @click="changeActive(1)" style="cursor: pointer; font-size: 14px">
                {{ form().step1Name || 'Step 1' }}
              </div>
            </template>
          </el-step>
          <el-step>
            <template v-slot:title>
              <div @click="changeActive(2)" style="cursor: pointer; font-size: 14px">
                {{ form().step2Name || 'Step 2' }}
              </div>
            </template>
          </el-step>
          <el-step>
            <template v-slot:title>
              <div @click="changeActive(3)" style="cursor: pointer; font-size: 14px">
                {{ form().step3Name || 'Step 3' }}
              </div>
            </template>
          </el-step>
        </el-steps>
      </el-row>
    </el-col>
    <el-col :span="24">
      <el-row :gutter="form().gutter">
        <VueDraggable
          class="custom-block-draggable"
          :class="form().mode === 'pc' ? 'el-row' : ''"
          draggable=".custom-widget-item"
          v-model="values"
          group="componentsGroup"
          ghostClass="ghost"
          chosenClass="chosen"
          :style="getDrableBoxStyle"
          style="position: relative; overflow: hidden; width: 100%"
          :disabled="!isEdit"
          :move="onDragMove"
          @add="onDragAdd"
        >
          <template v-if="Array.isArray(value) && value.length > 0">
            <el-col
              class="custom-widget-item"
              :class="{ active: isEdit && form().isActive(subWidget) }"
              v-for="subWidget in value"
              :key="subWidget.variableName"
              :span="
                selectedMode === 'pc'
                  ? subWidget.props.span || (subWidget.props.basicInfo || {}).span
                  : 24
              "
            >
              <div
                class="widget-item"
                :class="{ active: isEdit && form().isActive(subWidget) }"
                v-if="form().getWidgetVisible(subWidget)"
                @click.stop="onWidgetClick(subWidget)"
              >
                <div
                  v-if="subWidget.widgetType === SysCustomWidgetType.Table"
                  :style="getTableStyle(subWidget)"
                >
                  <OnlineCardTable
                    :widget="subWidget"
                    :ref="subWidget.variableName"
                    :value="form().getTableData(subWidget)"
                    @input="(dataList:ANY_OBJECT[]) => form().setTableData(subWidget, dataList)"
                    @click.stop="onWidgetClick(subWidget)"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.Block"
                  :style="getBlockStyle(subWidget)"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomBlock
                    :ref="subWidget.variableName"
                    v-model:value="subWidget.childWidgetList"
                    :isEdit="isEdit"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.Card"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineBaseCard
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <div
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.Tabs"
                  @click.stop="onWidgetClick(subWidget)"
                >
                  <OnlineCustomTabs
                    :ref="subWidget.variableName"
                    :widget="subWidget"
                    :isEdit="isEdit"
                    @widgetClick="onWidgetClick"
                  />
                </div>
                <!-- 使用OnlineCustomWidget将不会展示默认label名称 -->
                <OnlineCustomWidget
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.Text"
                  :ref="subWidget.variableName"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  :value="getWidgetValue(subWidget) || subWidget.props.text"
                  :style="{
                    'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px',
                  }"
                  @widgetClick="onWidgetClick"
                />
                <OnlineCustomWidget
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.Image"
                  :ref="subWidget.variableName"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  :value="getWidgetValue(subWidget)"
                  :src="subWidget.props.src"
                  :style="{
                    'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px',
                  }"
                  @widgetClick="onWidgetClick"
                />
                <OnlineCustomWidget
                  v-show="subWidget.props.activeStep === active"
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.BsProduct"
                  :ref="subWidget.variableName"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  :value="getWidgetValue(subWidget)"
                  :style="{
                    'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px',
                  }"
                  @widgetClick="onWidgetClick"
                />
                <OnlineCustomWidget
                  v-show="subWidget.props.activeStep === active"
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.BsProductConfirm"
                  :ref="subWidget.variableName"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  :value="getWidgetValue(subWidget)"
                  :style="{
                    'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px',
                  }"
                  @widgetClick="onWidgetClick"
                />
                <OnlineCustomWidget
                  v-show="subWidget.props.activeStep === active"
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.BsShippingOrderInfo"
                  :ref="subWidget.variableName"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  :value="getWidgetValue(subWidget)"
                  :style="{
                    'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px',
                  }"
                  @widgetClick="onWidgetClick"
                />
                <OnlineCustomWidget
                  v-show="subWidget.props.activeStep === active"
                  v-else-if="subWidget.widgetType === SysCustomWidgetType.BsRemarksToWarehouse"
                  :ref="subWidget.variableName"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  :value="getWidgetValue(subWidget)"
                  :style="{
                    'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px',
                  }"
                  @widgetClick="onWidgetClick"
                />
                <OnlineCustomWidget
                  v-show="subWidget.props.activeStep === active"
                  v-else-if="
                    subWidget.widgetType === SysCustomWidgetType.BsContactInfoForPackingList
                  "
                  :ref="subWidget.variableName"
                  :widget="subWidget"
                  :isEdit="isEdit"
                  :value="getWidgetValue(subWidget)"
                  :style="{
                    'margin-bottom': (subWidget.props.paddingBottom || 0) + 'px',
                  }"
                  @widgetClick="onWidgetClick"
                />
                <template v-else>
                  <component
                    v-show="subWidget.props.activeStep === active"
                    :is="form().mode === 'pc' ? ElFormItem : 'div'"
                    :label="subWidget.showName"
                    inset
                    :prop="subWidget.propString"
                    :class="{
                      'rich-input': subWidget.widgetType === SysCustomWidgetType.RichEditor,
                      'label-color-primary': true,
                    }"
                    :label-width="
                      subWidget.showName == null || subWidget.showName === ''
                        ? isEdit
                          ? '0px'
                          : '0px'
                        : undefined
                    "
                    @click.stop="onWidgetClick(subWidget)"
                  >
                    <OnlineCustomWidget
                      :widget="subWidget"
                      :ref="subWidget.variableName"
                      :value="getWidgetValue(subWidget)"
                      @input="val => onValueChange(subWidget, val)"
                      @change="(val: ANY_OBJECT|undefined, detail: ANY_OBJECT|null) => onWidgetValueChange(subWidget, val, detail)"
                    />
                  </component>
                </template>
                <ActiveWidgetMenu
                  v-if="isEdit && form().isActive(subWidget)"
                  :widget="subWidget"
                  :clone="form().cloneWidget"
                  @copy="onCopyWidget"
                  @delete="onDeleteWidget(subWidget)"
                />
              </div>
            </el-col>
          </template>
        </VueDraggable>
        <div v-if="isEmptyInStep && isEdit" class="info mover">
          <div style="width: 100px; height: 100px">
            <el-icon><UploadFilled /></el-icon>
          </div>
          <span>Please drag and drop components to edit</span>
        </div>
      </el-row>
    </el-col>
  </el-row>
</template>

<script setup lang="ts">
import { UploadFilled } from '@element-plus/icons-vue';
import { VueDraggable } from 'vue-draggable-plus';
import { ElMessageBox, ElFormItem } from 'element-plus';
import { ANY_OBJECT } from '@/types/generic';
import { SysCustomWidgetType } from '@/common/staticDict';
import { eventbus } from '@/common/utils/mitt';
import ActiveWidgetMenu from './ActiveWidgetMenu.vue';
import OnlineCustomWidget from './OnlineCustomWidget.vue';
import OnlineCustomTabs from './OnlineCustomTabs.vue';
import OnlineBaseCard from './OnlineBaseCard.vue';
import OnlineCardTable from './OnlineCardTable.vue';

interface IEmit {
  (event: 'widgetClick', value: ANY_OBJECT | null): void;
  (event: 'dragAdd', value: ANY_OBJECT): void;
  (event: 'update:value', value: ANY_OBJECT | ANY_OBJECT[]): void;
}
const emit = defineEmits<IEmit>();

interface IProps {
  // v-model:value 调用者必须使用这种方式赋值，才能被更新
  value: Array<ANY_OBJECT>;
  // 是否显示边框
  showBorder?: boolean;
  isEdit?: boolean;
  height?: string;
}
const props = withDefaults(defineProps<IProps>(), {
  value: () => [],
  isEdit: false,
  showBorder: true,
});

const form = inject('form', () => {
  console.error('OnlineCustomBlock: form not injected');
  return { isEdit: false } as ANY_OBJECT;
});
const active = inject('step');
const changeActive = num => {
  active.value = num;
  eventbus.emit('transferSelectedStep', num);
};
const selectedMode = ref<string>('pc');
const getDrableBoxStyle = computed(() => {
  let tempHeight = props.height;
  if (props.height == null || props.height === '') {
    tempHeight = isEmptyInStep.value ? '150px' : '0px';
  }
  return {
    'min-height': tempHeight,
    display: isEmptyInStep.value ? 'block' : 'contents',
  };
});
const getTableStyle = (widget: ANY_OBJECT) => {
  if (widget.widgetType !== SysCustomWidgetType.Table) return;
  return {
    'margin-bottom': (widget.props.paddingBottom || 0) + 'px',
  };
};
const getBlockStyle = (widget: ANY_OBJECT) => {
  return {
    'margin-bottom':
      (widget.props.paddingBottom || (widget.props.basicInfo || {}).paddingBottom || 0) + 'px',
    padding: props.isEdit ? '5px' : undefined,
    border: props.isEdit ? '1px solid #e8eaec' : undefined,
  };
};
const getChartStyle = (widget: ANY_OBJECT) => {
  return {
    'margin-bottom':
      (widget.props.paddingBottom || (widget.props.basicInfo || {}).paddingBottom || 0) + 'px',
  };
};

const values = computed({
  get() {
    return props.value;
  },
  set(val: Array<ANY_OBJECT>) {
    emit('update:value', val);
  },
});
const isEmptyInStep = computed(() => {
  return !(
    Array.isArray(props.value) &&
    props.value.filter(i => {
      return i.props.activeStep === active.value;
    }).length > 0
  );
});
const onWidgetClick = (widget: ANY_OBJECT | null = null) => {
  emit('widgetClick', widget);
};
const onDragAdd = (e: DragEvent) => {
  console.log('block onDragAdd', e, props.value, e.newIndex);
  const addItem = props.value[e.newIndex];
  const isExist = props.value.filter(i => i.widgetType === addItem.widgetType).length > 1;
  if (isExist) {
    emit(
      'update:value',
      props.value.filter((item, index) => index !== e.newIndex),
    );
    ElMessageBox('You cannot add an existing component');
    return;
  }
  addItem.props.activeStep = active.value;
  emit('dragAdd', { list: props.value, dragEvent: e });
  if (addItem.widgetType === 409) {
    addItem.variableName = 'product';
  }
  if (addItem.widgetType === 422) {
    addItem.variableName = 'remarksToWarehouse';
  }
  nextTick(() => {
    onWidgetClick(addItem);
  });
};
const onDragMove = (e: ANY_OBJECT) => {
  console.log('block dragMove', e);
  // 容器组件不能改变位置
  let widgetType = e.relatedContext.element ? e.relatedContext.element.widgetType : undefined;
  return widgetType !== SysCustomWidgetType.Block && widgetType !== SysCustomWidgetType.Card;
};
const onCopyWidget = (widget: ANY_OBJECT) => {
  emit('update:value', [...props.value, widget]);
};
const onDeleteWidget = (widget: ANY_OBJECT) => {
  ElMessageBox.confirm('Do you want to delete this component?', '', {
    confirmButtonText: 'Yes',
    cancelButtonText: 'No',
    type: 'warning',
  })
    .then(() => {
      emit(
        'update:value',
        props.value.filter(item => item !== widget),
      );
      onWidgetClick(null);
    })
    .catch(e => {
      console.warn(e);
    });
};

const getWidgetValue = (widget: ANY_OBJECT) => {
  return form().getWidgetValue(widget);
};
const onValueChange = (
  widget: ANY_OBJECT,
  value: string | boolean | Date | number | ANY_OBJECT | Array<ANY_OBJECT> | undefined | null,
) => {
  console.log('[[[ block value change ]]]', value, typeof value);

  if (value instanceof Event) {
    console.log('block onValueChange', value);
    if (value.target instanceof HTMLInputElement) {
      return form().onValueChange(widget, value.target.value);
    }
  }
  return form().onValueChange(widget, value);
};
const onWidgetValueChange = (
  widget: ANY_OBJECT,
  value: ANY_OBJECT | null | undefined,
  detail: ANY_OBJECT | null,
) => {
  console.log('block onWidgetValueChange>>>>>>>>>>>>>>', value, widget, form().onWidgetValueChange);
  return form().onWidgetValueChange(widget, value, detail);
};

onMounted(() => {
  eventbus.on('transferSelectedMode', d => {
    selectedMode.value = d as string;
  });
});
onUnmounted(() => {
  eventbus.off('transferSelectedMode');
});
</script>

<style lang="scss" scoped>
.ghost {
  height: 30px;
  border-radius: 3px;
}
.info {
  // position: absolute;
  display: flex;
  justify-content: center;
  align-items: center;
  width: 100%;
  height: 100%;
  margin-top: -400px;
  text-align: center;
  color: #999;
  flex-direction: column;
  vertical-align: middle;
}

.info div {
  width: 80px;
  height: 80px;
  font-size: 60px;
  text-align: center;
  border: 1px dashed #d9dbdd;
  border-radius: 6px;
  line-height: 100px;
}
.info span {
  margin-top: 10px;
  font-size: 16px;
}
:deep(.is-process) {
  border-color: #a8abb2;
  color: #a8abb2;
  font-weight: normal;
}
</style>
