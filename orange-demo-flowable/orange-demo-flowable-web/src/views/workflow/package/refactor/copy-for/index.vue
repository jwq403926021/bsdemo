<template>
  <div class="panel-tab__content">
    <ConyForSelect v-model="copyForItemList" />
  </div>
</template>

<script>
import ConyForSelect from '@/views/workflow/components/CopyForSelect/index.vue';

export default {
  props: {
    id: String,
    type: String
  },
  components: {
    ConyForSelect
  },
  inject: ['prefix'],
  data () {
    return {
      copyForItemList: [],
      copyItemElementList: undefined
    }
  },
  methods: {
    resetFormVariable () {
      this.bpmnELement = window.bpmnInstances.bpmnElement;
      let elExtensionElements = this.bpmnELement.businessObject.get("extensionElements") || window.bpmnInstances.moddle.create("bpmn:ExtensionElements", { values: [] });
      this.copyItemElementList = elExtensionElements.values.filter(ex => ex.$type === `${this.prefix}:CopyItemList`)?.[0] ||
        window.bpmnInstances.moddle.create(`${this.prefix}:CopyItemList`, { copyItemList: [] });
      this.copyForItemList = this.copyItemElementList.copyItemList;
      this.updateElementExtensions();
    },
    updateElementExtensions () {
      // 更新回扩展元素
      let elExtensionElements = this.bpmnELement.businessObject.get("extensionElements")  || window.bpmnInstances.moddle.create("bpmn:ExtensionElements", { values: [] });
      let otherExtensions = elExtensionElements.values.filter(ex => ex.$type !== `${this.prefix}:CopyItemList`);
      this.copyItemElementList.copyItemList = (this.copyForItemList || []).map(item => {
        return window.bpmnInstances.moddle.create(`${this.prefix}:CopyItem`, {
          id: item.id,
          type: item.type
        });
      });
      const newElExtensionElements = window.bpmnInstances.moddle.create(`bpmn:ExtensionElements`, {
        values: otherExtensions.concat(this.copyItemElementList)
      });
      // 更新到元素上
      window.bpmnInstances.modeling.updateProperties(this.bpmnELement, {
        extensionElements: newElExtensionElements
      });
    }
  },
  watch: {
    'copyForItemList': {
      handler (newValue) {
        this.updateElementExtensions();
      }
    },
    id: {
      immediate: true,
      handler (val) {
        if (val && val.length) {
          this.$nextTick(() => {
            this.resetFormVariable();
          });
        }
      }
    }
  }
}
</script>

<style>
</style>
