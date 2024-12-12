<template>
  <div>
    <el-form
      ref="form"
      class="full-width-input"
      size="default"
      :model="formPageData"
      :rules="formRules"
      label-position="right"
      label-width="150px"
      @submit.prevent
    >
      <!--      <el-col :span="24">-->
      <!--        <el-form-item label="Page Type">-->
      <!--          <el-select v-model="formPageData.pageType" :disabled="isEdit">-->
      <!--            <el-option-->
      <!--              :value="SysOnlinePageType.BIZ"-->
      <!--              :label="SysOnlinePageType.getValue(SysOnlinePageType.BIZ)"-->
      <!--            />-->
      <!--            <el-option-->
      <!--              :value="SysOnlinePageType.FLOW"-->
      <!--              :label="SysOnlinePageType.getValue(SysOnlinePageType.FLOW)"-->
      <!--            />-->
      <!--          </el-select>-->
      <!--        </el-form-item>-->
      <!--      </el-col>-->
      <el-col :span="24">
        <el-form-item label="Page Code" prop="pageCode">
          <el-input v-model="formPageData.pageCode" :disabled="isEdit" @change="dirty = true" />
        </el-form-item>
      </el-col>
      <el-col :span="24">
        <el-form-item label="Page Name" prop="pageName">
          <el-input v-model="formPageData.pageName" @change="dirty = true" />
        </el-form-item>
      </el-col>
      <!--      <el-col :span="24">-->
      <!--        <el-form-item label="Background Extend">-->
      <!--          <el-input-->
      <!--            v-model="formPageData.extraJson.extendClass"-->
      <!--            placeholder="Please enter the full class name"-->
      <!--            @change="dirty = true"-->
      <!--          />-->
      <!--        </el-form-item>-->
      <!--      </el-col>-->
    </el-form>
  </div>
</template>

<script setup lang="ts">
import { FormItemRule } from 'element-plus/es/components/form';
import { Arrayable } from 'element-plus/es/utils';
import { ElMessage } from 'element-plus';
import { FormPage } from '@/types/online/page';
import { SysOnlinePageStatus, SysOnlinePageType } from '@/common/staticDict/online';
import { uuid } from '@/pages/workflow/package/utils';
import { OnlinePageController } from '@/api/online';
import { ANY_OBJECT } from '@/types/generic';
import { OnlineDatasourceController, OnlineDblinkController } from '@/api/online';

const props = defineProps<{
  modelValue: FormPage;
  status?: number;
  dblinkInfo: ANY_OBJECT;
  datasourceId: string;
  dataSource?: ANY_OBJECT;
}>();

const form = ref();
const formPageData = ref<FormPage>({
  pageId: undefined,
  pageCode: undefined,
  pageName: undefined,
  published: false,
  pageType: SysOnlinePageType.BIZ,
  status: SysOnlinePageStatus.BASIC,
  extraJson: {
    extendClass: undefined,
  },
});
const dirty = ref(false);
const formRules: Partial<Record<string, Arrayable<FormItemRule>>> = {
  pageCode: [
    { required: true, message: 'Page number cannot be empty!', trigger: 'blur' },
    {
      type: 'string',
      pattern: /^[A-Za-z0-9]+$/,
      message: 'Page code only allows letters and numbers',
      trigger: 'blur',
    },
  ],
  pageName: [{ required: true, message: 'Page name cannot be empty!', trigger: 'blur' }],
};
const isEdit = computed(() => {
  return !!formPageData.value.pageId;
});

const dialogParams = computed(() => {
  return {
    pageId: formPageData.value.pageId,
    datasourceId: props.datasourceId,
    dblinkInfo: props.dblinkInfo,
  };
});

watch(
  () => props.modelValue,
  (newVal, oldVal) => {
    if (newVal != oldVal) {
      formPageData.value = { ...formPageData.value, ...newVal };
    }
  },
  {
    deep: true,
  },
);

/**
 * 保存页面基础信息
 */
const savePageInfo = (status: number) => {
  let params = {
    onlinePageDto: {
      ...formPageData.value,
      extraJson: JSON.stringify(formPageData.value.extraJson || {}),
      status: status,
      published: false,
    },
  };
  return isEdit.value ? OnlinePageController.update(params) : OnlinePageController.add(params);
};

/**
 * 保存页面基础信息
 */
const save = (): Promise<FormPage | undefined> => {
  return new Promise((resolve, reject) => {
    form.value.validate((valid: boolean) => {
      if (!valid || !dirty.value) {
        valid ? resolve(formPageData.value) : reject();
        return;
      }
      savePageInfo(props.status ? props.status : SysOnlinePageStatus.DATASOURCE)
        .then(res => {
          ElMessage.success('The basic information saved successfully!');
          if (!isEdit.value) {
            formPageData.value.pageId = res.data;
          }
          dirty.value = false;
          let dblink: any = '';
          OnlineDblinkController.list({}).then(res => {
            dblink = res.data.dataList.map(item => {
              return {
                dblinkId: item.dblinkId,
                label: item.dblinkName,
                leaf: false,
              };
            });
            const randomName = uuid();
            let params = {
              pageId: dialogParams.value.pageId,
              onlineDatasourceDto: {
                datasourceId: '',
                datasourceName: randomName,
                variableName: randomName,
                dblinkId: dblink[0].dblinkId,
                masterTableName: 'zz_test_order_first',
              },
            };

            OnlineDatasourceController.add(params)
              .then(res => {
                ElMessage.success('The data model was saved successfully!');
                resolve(formPageData.value);
              })
              .catch(e => {
                console.warn(e);
              });
          });
        })
        .catch(e => {
          reject(e);
        });
    });
  });
};

defineExpose({
  save,
});

onMounted(() => {
  formPageData.value = { ...formPageData.value, ...props.modelValue };
});
</script>
