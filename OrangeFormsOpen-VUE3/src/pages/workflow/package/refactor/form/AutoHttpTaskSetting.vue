<template>
  <div class="autotask-http-setting">
    <el-form-item label="API Url">
      <el-input
        placeholder="Please enter the request URL"
        v-model="formData.httpRequestInfo.url"
        @change="onChange"
      >
        <template #prepend>
          <el-select
            class="url-select"
            style="width: 100px"
            v-model="formData.httpRequestInfo.httpMethod"
            placeholder="Please select"
            @change="onChange"
          >
            <el-option label="GET" value="GET" />
            <el-option label="POST" value="POST" />
            <el-option label="PUT" value="PUT" />
            <el-option label="DELETE" value="DELETE" />
          </el-select>
        </template>
      </el-input>
    </el-form-item>
    <MultiItemList
      label="Request Header Settings"
      :data="formData.httpRequestInfo.headerList"
      addText="Add"
      @edit="onEditHttpHeader"
      @delete="onDeleteHttpHeader"
      :prop="{
        label: 'key',
        value: 'key',
      }"
    >
      <template v-slot="scope">
        <span>{{ (scope.data || {}).key }}</span>
        <span style="margin: 0px 10px">equals</span>
        <span style="margin: 0px 10px 0px 0px">{{
          AutoTaskValueType.getValue(scope.data.type)
        }}</span>
        <span>
          {{ (scope.data || {}).srcValue }}
        </span>
      </template>
    </MultiItemList>
    <MultiItemList
      label="URL Parameter Settings"
      :data="formData.httpRequestInfo.urlParamList"
      addText="Add"
      @edit="onEditUrlParam"
      @delete="onDeleteUrlParam"
      :prop="{
        label: 'name',
        value: 'id',
      }"
    >
      <template v-slot="scope">
        <span>{{ (scope.data || {}).key }}</span>
        <span style="margin: 0px 10px">equals</span>
        <span style="margin: 0px 10px 0px 0px">{{
          AutoTaskValueType.getValue(scope.data.type)
        }}</span>
        <span>
          {{ (scope.data || {}).srcValue }}
        </span>
      </template>
    </MultiItemList>
    <el-form-item label="Request Body Type">
      <el-radio-group v-model="formData.httpRequestInfo.bodyType" @change="onChange">
        <el-radio v-for="item in BodyType.getList()" :key="item.id" :label="item.id">
          <el-row type="flex" align="middle">
            <span>{{ item.name }}</span>
            <el-dropdown
              v-if="formData.httpRequestInfo.bodyType === BodyType.RAW && item.id === BodyType.RAW"
              style="margin-left: 10px"
              @command="cmd => (formData.httpRequestInfo.rawType = cmd)"
            >
              <el-link>{{ RawType.getValue(formData.httpRequestInfo.rawType) }}</el-link>
              <template #dropdown>
                <el-dropdown-menu>
                  <el-dropdown-item
                    v-for="item in RawType.getList()"
                    :key="item.id"
                    :command="item.id"
                  >
                    {{ item.name }}
                  </el-dropdown-item>
                </el-dropdown-menu>
              </template>
            </el-dropdown>
          </el-row>
        </el-radio>
      </el-radio-group>
    </el-form-item>
    <MultiItemList
      v-if="formData.httpRequestInfo.bodyType === BodyType.FORM_DATA"
      label="Body Parameter Settings"
      :data="formData.httpRequestInfo.formDataList"
      addText="Add"
      @edit="onEditBodyParam"
      @delete="onDeleteBodyParam"
      :prop="{
        label: 'name',
        value: 'id',
      }"
    >
      <template v-slot="scope">
        <span>{{ (scope.data || {}).key }}</span>
        <span style="margin: 0px 10px">equals</span>
        <span style="margin: 0px 10px 0px 0px">{{
          AutoTaskValueType.getValue(scope.data.type)
        }}</span>
        <span>
          {{ (scope.data || {}).srcValue }}
        </span>
      </template>
    </MultiItemList>
    <el-form-item
      label="Body Parameter Settings"
      v-if="formData.httpRequestInfo.bodyType === BodyType.RAW"
    >
      <el-input
        v-model="formData.httpRequestInfo.bodyData"
        type="textarea"
        rows="5"
        clearable
        placeholder="Please enter the request body"
        @change="onChange"
      />
    </el-form-item>
    <el-form-item label="Successful HTTP Status Codes">
      <el-input
        v-model="formData.httpResponnseData.successStatusCode"
        clearable
        placeholder="Example: 200,201 (Multiple status codes separated by commas)"
        @change="onChange"
      />
    </el-form-item>
    <el-form-item label="Success Field">
      <el-input
        v-model="formData.httpResponnseData.successBodyField"
        clearable
        placeholder="Example: data.isSuccess, a field to further judge success in case of success"
        @change="onChange"
      />
    </el-form-item>
    <el-form-item label="Failure Information Field">
      <el-input
        v-model="formData.httpResponnseData.errorMessageBodyField"
        clearable
        placeholder="Example: data.message, the returned failure message field in case of success"
        @change="onChange"
      />
    </el-form-item>
    <el-form-item label="Failure Handling Method">
      <el-radio-group v-model="formData.httpResponnseData.failHandleType" @change="onChange">
        <el-radio v-for="item in FailHandleType.getList()" :key="item.id" :label="item.id">
          {{ item.name }}
        </el-radio>
      </el-radio-group>
    </el-form-item>
    <el-form-item v-if="false" label="Response Data">
      <el-table
        :show-header="false"
        :data="responseDataTree"
        size="mini"
        row-key="id"
        :tree-props="{ children: 'children' }"
      >
        <el-table-column prop="fieldName" label="Field Name" />
        <el-table-column prop="fieldType" label="Field Type" width="100px" />
        <el-table-column label="Action" width="120px">
          <template v-slot="scope">
            <el-button type="primary" link size="small" @click="onEditResponseData(scope.row)">
              Edit
            </el-button>
            <el-button type="primary" link size="small" @click="onDeleteResponseData(scope.row)">
              Delete
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-button
        type="primary"
        plain
        size="small"
        icon="el-icon-plus"
        style="width: 100%"
        @click="onEditResponseData()"
      >
        Add Response Field
      </el-button>
    </el-form-item>
  </div>
</template>

<script setup lang="ts">
import { ElMessageBox } from 'element-plus';
import { defineProps, defineEmits } from 'vue';
import EditHttpHeader from './editHttpHeader.vue';
import EditHttpParameter from './editHttpParameter.vue';
import EditResponseData from './editResponseData.vue';
import { treeDataTranslate } from '@/common/utils/index';
import { DictionaryBase } from '@/common/staticDict/types';
import MultiItemList from '@/components/MultiItemList/index.vue';
import { Dialog } from '@/components/Dialog';
import { AutoTaskActionType, AutoTaskValueType } from '@/common/staticDict/flow';
import { ANY_OBJECT } from '@/types/generic';

const emit = defineEmits(['update:modelValue', 'change']);
const props = defineProps<{
  modelValue: string;
}>();
const flowEntry = inject('flowEntry', () => {
  return {} as ANY_OBJECT;
});
const formList = inject('formList', () => {
  return {} as ANY_OBJECT;
});
const prefix = inject('prefix');
const getAllAutoVariableList = inject('getAllAutoVariableList', () => {
  return [];
});
type FormDataType = {
  actionType?: number;
  httpRequestInfo: {
    url: string;
    httpMethod: string;
    headerList: Array<ANY_OBJECT>;
    bodyType: string;
    urlParamList: Array<ANY_OBJECT>;
    formDataList: Array<ANY_OBJECT>;
    rawType: string;
    bodyData: string;
  };
  httpResponnseData: {
    successStatusCode: string;
    successBodyField: string;
    errorMessageBodyField: string;
    failHandleType: string;
    httpResponseBody: Array<ANY_OBJECT>;
  };
};
const formData = ref<FormDataType>({
  actionType: AutoTaskActionType.HTTP,
  httpRequestInfo: {
    url: '',
    httpMethod: 'GET',
    headerList: [],
    bodyType: 'formData',
    urlParamList: [],
    formDataList: [],
    rawType: 'json',
    bodyData: '',
  },
  httpResponnseData: {
    successStatusCode: '',
    successBodyField: '',
    errorMessageBodyField: '',
    failHandleType: 'stop',
    httpResponseBody: [],
  },
});

const BodyType = new DictionaryBase('Body Type', [
  {
    id: 'formData',
    name: 'form-data',
    symbol: 'FORM_DATA',
  },
  {
    id: 'raw',
    name: 'raw',
    symbol: 'RAW',
  },
]);

const RawType = new DictionaryBase('Raw Type', [
  {
    id: 'text',
    name: 'text',
    symbol: 'TEXT',
  },
  {
    id: 'json',
    name: 'json',
    symbol: 'JSON',
  },
]);

const FailHandleType = new DictionaryBase('Failure Handling Method', [
  {
    id: 'stop',
    name: 'Stop Process Execution',
    symbol: 'EXIT',
  },
  {
    id: 'continue',
    name: 'Continue Execution',
    symbol: 'CONTINUE',
  },
]);

const responseDataTree = computed(() => {
  let dataList = formData.value.httpResponnseData.httpResponseBody.map(item => {
    return {
      ...item,
      disabled: item.fieldType !== 'Object' && item.fieldType !== 'Array',
    };
  });
  return treeDataTranslate(dataList, 'id', 'parentId');
});

const onChange = () => {
  let tempData = {
    ...formData.value,
  };
  emit('update:modelValue', JSON.stringify(tempData));
  emit('change', JSON.stringify(tempData));
};

const updateHttpHeader = (row, data) => {
  if (row == null) {
    // Create New
    if (formData.value.httpRequestInfo.headerList == null) {
      formData.value.httpRequestInfo.headerList = [];
    }
    formData.value.httpRequestInfo.headerList.push(data);
  } else {
    // Edit
    formData.value.httpRequestInfo.headerList = formData.value.httpRequestInfo.headerList.map(
      item => {
        return item.key === data.key ? data : item;
      },
    );
  }
  onChange();
};

const onEditHttpHeader = (row?: ANY_OBJECT) => {
  Dialog.show(
    'Edit Request Header',
    EditHttpHeader,
    {
      area: ['500px', '300px'],
    },
    {
      entryId: flowEntry().entryId,
      data: row,
      usedKeyList: formData.value.httpRequestInfo.headerList.map(item => item.key),
      flowVariableList: getAllAutoVariableList(),
      path: 'thirdEditHttpHeader',
    },
    {
      width: '500px',
      height: '300px',
      pathName: '/thirdParty/thirdEditHttpHeader',
    },
  )
    .then(res => {
      updateHttpHeader(row, res);
    })
    .catch(e => {
      console.log(e);
    });
};

const onDeleteHttpHeader = row => {
  ElMessageBox.confirm('Do you want to delete this request header?', 'Prompt', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(() => {
      formData.value.httpRequestInfo.headerList = formData.value.httpRequestInfo.headerList.filter(
        item => item.key !== row.key,
      );
      onChange();
    })
    .catch(e => {
      console.log(e);
    });
};

const updateUrlParam = (row, data) => {
  if (row == null) {
    // Create New
    if (formData.value.httpRequestInfo.urlParamList == null) {
      formData.value.httpRequestInfo.urlParamList = [];
    }
    formData.value.httpRequestInfo.urlParamList.push(data);
  } else {
    // Edit
    formData.value.httpRequestInfo.urlParamList = formData.value.httpRequestInfo.urlParamList.map(
      item => {
        return item.key === data.key ? data : item;
      },
    );
  }
  onChange();
};

const onEditUrlParam = (row?: ANY_OBJECT) => {
  Dialog.show(
    'Edit URL Parameter',
    EditHttpParameter,
    {
      area: ['500px', '400px'],
    },
    {
      entryId: flowEntry().entryId,
      data: row,
      usedParamList: formData.value.httpRequestInfo.urlParamList.map(item => item.key),
      flowVariableList: getAllAutoVariableList(),
      path: 'thirdEditHttpUrlParam',
    },
    {
      width: '500px',
      height: '400px',
      pathName: '/thirdParty/thirdEditHttpRequestParam',
    },
  )
    .then(res => {
      updateUrlParam(row, res);
    })
    .catch(e => {
      console.log(e);
    });
};

const onDeleteUrlParam = row => {
  ElMessageBox.confirm('Do you want to delete this URL parameter?', 'Prompt', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(res => {
      formData.value.httpRequestInfo.urlParamList =
        formData.value.httpRequestInfo.urlParamList.filter(item => item.key !== row.key);
      onChange();
    })
    .catch(e => {
      console.log(e);
    });
};

const updateBodyParam = (row, res) => {
  if (formData.value.httpRequestInfo.formDataList == null) {
    formData.value.httpRequestInfo.formDataList = [];
  }
  if (row == null) {
    // Create New
    formData.value.httpRequestInfo.formDataList.push(res);
  } else {
    // Edit
    formData.value.httpRequestInfo.formDataList = formData.value.httpRequestInfo.formDataList.map(
      item => {
        return item.key === res.key ? res : item;
      },
    );
  }
  onChange();
};

const onEditBodyParam = (row?: ANY_OBJECT) => {
  Dialog.show(
    'Edit Body Parameter',
    EditHttpParameter,
    {
      area: ['500px', '400px'],
    },
    {
      entryId: flowEntry().entryId,
      data: row,
      usedParamList: formData.value.httpRequestInfo.formDataList.map(item => item.key),
      flowVariableList: getAllAutoVariableList(),
      path: 'thirdEditHttpBodyParam',
    },
    {
      width: '500px',
      height: '400px',
      pathName: '/thirdParty/thirdEditHttpRequestParam',
    },
  )
    .then(res => {
      updateBodyParam(row, res);
    })
    .catch(e => {
      console.log(e);
    });
};

const onDeleteBodyParam = row => {
  ElMessageBox.confirm('Do you want to delete this Body parameter?', 'Prompt', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(res => {
      formData.value.httpRequestInfo.formDataList =
        formData.value.httpRequestInfo.formDataList.filter(item => item.key !== row.key);
      onChange();
    })
    .catch(e => {
      console.log(e);
    });
};

const updateResponseData = (row, res) => {
  function buildChildId(node, parentId) {
    if (parentId == null || parentId === '') {
      node.id = parentId + '.' + node.fieldName;
    } else {
      node.id = node.fieldName;
    }
    if (Array.isArray(node.children)) {
      node.children.forEach(child => {
        buildChildId(child, node.id);
      });
    }
  }
  if (formData.value.httpResponnseData.httpResponseBody == null) {
    formData.value.httpResponnseData.httpResponseBody = [];
  }
  if (row == null) {
    // Create New
    formData.value.httpResponnseData.httpResponseBody.push(res);
  } else {
    // Edit
    formData.value.httpResponnseData.httpResponseBody =
      formData.value.httpResponnseData.httpResponseBody.map(item => {
        if (item.id === res.oldId) {
          buildChildId(res, res.parentId);
          return res;
        } else {
          return item;
        }
      });
  }
  onChange();
};

const onEditResponseData = (row?: ANY_OBJECT) => {
  Dialog.show(
    'Edit Response Field',
    EditResponseData,
    {
      area: ['500px', '400px'],
    },
    {
      entryId: flowEntry().entryId,
      data: row,
      usedFieldList: responseDataTree.value,
      path: 'thirdEditResponseData',
    },
    {
      width: '500px',
      height: '400px',
      pathName: '/thirdParty/thirdEditResponseData',
    },
  )
    .then(res => {
      updateResponseData(row, res);
    })
    .catch(e => {
      console.log(e);
    });
};

const onDeleteResponseData = row => {
  ElMessageBox.confirm('Do you want to delete this response field?', 'Prompt', {
    confirmButtonText: 'Confirm',
    cancelButtonText: 'Cancel',
    type: 'warning',
  })
    .then(res => {
      formData.value.httpResponnseData.httpResponseBody =
        formData.value.httpResponnseData.httpResponseBody.filter(item => item.id !== row.id);
      onChange();
    })
    .catch(e => {
      console.log(e);
    });
};

watch(
  () => props.modelValue,
  val => {
    let taskInfo = val && val !== '' ? JSON.parse(val) : {};
    if (taskInfo.httpRequestInfo == null) {
      taskInfo.httpRequestInfo = {};
    }
    if (taskInfo.httpResponnseData == null) {
      taskInfo.httpResponnseData = {};
    }
    formData.value = {
      actionType: AutoTaskActionType.HTTP,
      httpRequestInfo: {
        url: taskInfo.httpRequestInfo.url || '',
        httpMethod: taskInfo.httpRequestInfo.httpMethod || 'GET',
        headerList: taskInfo.httpRequestInfo.headerList || [],
        bodyType: taskInfo.httpRequestInfo.bodyType || 'formData',
        urlParamList: taskInfo.httpRequestInfo.urlParamList || [],
        formDataList: taskInfo.httpRequestInfo.formDataList || [],
        rawType: taskInfo.httpRequestInfo.rawType || 'json',
        bodyData: taskInfo.httpRequestInfo.bodyData || '',
      },
      httpResponnseData: {
        successStatusCode: taskInfo.httpResponnseData.successStatusCode || '',
        successBodyField: taskInfo.httpResponnseData.successBodyField || '',
        errorMessageBodyField: taskInfo.httpResponnseData.errorMessageBodyField || '',
        failHandleType: taskInfo.httpResponnseData.failHandleType || 'stop',
        httpResponseBody: taskInfo.httpResponnseData.httpResponseBody || [],
      },
    };
  },
  { immediate: true },
);
</script>

<style scoped>
.url-select {
  width: 100px;
}
.url-select /deep/ .el-input--suffix {
  width: 100px;
}
.url-select /deep/ .el-input__inner {
  padding: 0px 15px;
}
</style>
