<template>
  <div>
    <h2 style="margin: 10px 0">Remarks to Warehouse</h2>
    <el-radio-group v-model="remarksToWarehouse" @change="handleDeliveryChange" class="radio-group">
      <el-radio label="standard">Standard delivery</el-radio>
      <el-radio label="urgent">Urgent non-standard delivery</el-radio>
    </el-radio-group>
    <div v-if="remarksToWarehouse === 'urgent'" class="urgent-form">
      <el-form ref="form" :model="formData" label-width="180px" :rules="rules" label-position="top">
        <el-row :gutter="24">
          <el-col :span="8">
            <el-form-item
              span="12"
              label="Request Delivery Date"
              prop="requestDeliveryDate"
              required
            >
              <el-date-picker
                v-model="formData.requestDeliveryDate"
                type="date"
                placeholder="Request Delivery Date"
              />
            </el-form-item>
          </el-col>
          <el-col :span="8">
            <el-form-item label="Paying" prop="paying">
              <el-select v-model="formData.paying" placeholder="Paying">
                <el-option label="BSC paying" value="BSC paying"></el-option>
                <el-option label="Customer paying" value="Customer paying"></el-option>
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ANY_OBJECT } from '@/types/generic';
import { WidgetProps } from '@/online/components/types/widget';
import moment from 'moment';

const form = ref();
const emit = defineEmits<{
  'update:modelValue': [string | number | ANY_OBJECT[]];
  change: [string | number | ANY_OBJECT[], string | number | ANY_OBJECT[]];
}>();
const rules = {
  requestDeliveryDate: [
    { required: true, message: 'The Request Delivery Date Cannot Be Empty', trigger: 'blur' },
  ],
};
const pps = withDefaults(
  defineProps<{
    modelValue: string | number | Array<ANY_OBJECT> | undefined;
    widget: WidgetProps;
  }>(),
  {},
);

const formData = ref<ANY_OBJECT>({
  requestDeliveryDate: '',
  paying: '',
});

const remarksToWarehouse = ref<string>();

const handleDeliveryChange = val => {
  if (val === 'standard') {
    formData.value = {
      requestDeliveryDate: '',
      paying: '',
    };
  }
};

const getValue = () => {
  return {
    remarksToWarehouse: remarksToWarehouse.value,
    requestDeliveryDate: formData.value.requestDeliveryDate
      ? moment(formData.value.requestDeliveryDate).format('YYYY-MM-DD')
      : '',
    paying: formData.value.paying,
  };
};
const validateForm = () => {
  return new Promise(resolve => {
    if (form.value) {
      form.value.validate(valid => {
        resolve(valid);
      });
    } else {
      resolve(true);
    }
  });
};
const setValue = (val: string) => {
  remarksToWarehouse.value = val;
  formData.value.requestDeliveryDate = val;
  formData.value.paying = val;
};
defineExpose({ getValue, setValue, validateForm });
</script>

<style scoped>
.radio-group {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}
.urgent-form {
  position: relative;
  left: 60px;
}
</style>
