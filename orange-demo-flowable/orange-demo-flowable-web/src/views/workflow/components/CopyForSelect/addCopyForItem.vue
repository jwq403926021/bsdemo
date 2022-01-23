<template>
  <div class="form-single-fragment" style="position: relative;">
    <el-form ref="form" :model="formData" class="full-width-input" style="width: 100%;"
      label-width="130px" :size="defaultFormItemSize" label-position="right" @submit.native.prevent>
      <el-row :gutter="20">
        <el-col :span="24">
          <el-form-item label="抄送类型" prop="type">
            <el-select class="input-item" v-model="formData.type"
              placeholder="抄送类型" @change="onCopyForTypeChange">
              <el-option v-for="item in SysFlowCopyForType.getList()"
                :key="item.id" :label="item.name" :value="item.id" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-form-item v-if="formData.type === SysFlowCopyForType.USER" label="抄送人" prop="id">
            <TagSelect v-model="formData.id">
              <el-button slot="append" class="append-add" type="default" icon="el-icon-plus" @click="onSelectUser" />
            </TagSelect>
          </el-form-item>
          <el-form-item v-if="formData.type === SysFlowCopyForType.ROLE" label="抄送角色" prop="id">
            <el-select v-model="formData.id" placeholder="" :multiple="true">
              <el-option v-for="role in roleList" :key="role.id" :label="role.name" :value="role.id" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="formData.type === SysFlowCopyForType.DEPT" label="抄送部门" prop="id">
            <TagSelect v-model="selectDeptList">
              <el-button slot="append" class="append-add" type="default" icon="el-icon-plus" @click="onSelectDept" />
            </TagSelect>
          </el-form-item>
          <el-form-item v-if="[SysFlowCopyForType.POST, SysFlowCopyForType.SELF_DEPT_POST, SysFlowCopyForType.UP_DEPT_POST].indexOf(formData.type) !== -1"
            :label="SysFlowCopyForType.getValue(formData.type)" prop="id">
            <el-select v-model="formData.id" placeholder="" :multiple="true">
              <el-option v-for="post in postList" :key="post.postId" :label="post.postName" :value="post.postId" />
            </el-select>
          </el-form-item>
          <el-form-item v-if="formData.type === SysFlowCopyForType.DEPT_POST" label="抄送部门" prop="deptId">
            <el-cascader v-model="deptId" :clearable="true"
              :size="defaultFormItemSize" placeholder=""
              :props="{value: 'id', label: 'name', checkStrictly: true}"
              :options="deptList" @change="onDeptChange"
            >
            </el-cascader>
          </el-form-item>
          <el-form-item v-if="formData.type === SysFlowCopyForType.DEPT_POST" label="抄送岗位" prop="id">
            <el-select v-model="formData.id" placeholder="" :multiple="true">
              <el-option v-for="post in validDeptPostList" :key="post.deptPostId" :label="post.postShowName" :value="post.deptPostId" />
            </el-select>
          </el-form-item>
        </el-col>
        <el-col :span="24">
          <el-row type="flex" justify="end">
            <el-button type="primary" :size="defaultFormItemSize" :plain="true"
              @click="onCancel(false)">
              取消
            </el-button>
            <el-button type="primary" :size="defaultFormItemSize" @click="onSubmit()">
              保存
            </el-button>
          </el-row>
        </el-col>
      </el-row>
    </el-form>
  </div>
</template>

<script>
import { findItemFromList } from '@/utils';
import TagSelect from '@/views/workflow/components/TagSelect.vue';
import TaskUserSelect from '@/views/workflow/components/TaskUserSelect.vue';
import TaskGroupSelect from '@/views/workflow/components/TaskGroupSelect.vue';

export default {
  props: {
    deptList: {
      type: Array,
      required: true
    },
    postList: {
      type: Array,
      required: true
    },
    deptPostList: {
      type: Array,
      required: true
    },
    roleList: {
      type: Array,
      required: true
    }
  },
  components: {
    TagSelect
  },
  data () {
    return {
      deptId: undefined,
      selectDeptList: [],
      formData: {
        type: this.SysFlowCopyForType.USER,
        id: undefined
      }
    }
  },
  methods: {
    onCancel (isSuccess, data) {
      if (this.observer != null) {
        this.observer.cancel(isSuccess, data);
      }
    },
    onSubmit () {
      if (this.formData.type === this.SysFlowCopyForType.DEPT_POST && (this.deptId == null || this.deptId === '')) {
        this.$message.warning('请选择抄送部门');
        return;
      }
      if ((this.formData.id == null || this.formData.id === '') &&
        this.formData.type !== this.SysFlowCopyForType.SELF_DEPT_LEADER &&
        this.formData.type !== this.SysFlowCopyForType.UP_DEPT_LEADER) {
        this.$message.warning('请选择' + this.SysFlowCopyForType.getValue(this.formData.type));
        return;
      }
      let tempId = null;
      switch (this.formData.type) {
        case this.SysFlowCopyForType.ROLE:
          tempId = this.formData.id.join(',');
          break;
        case this.SysFlowCopyForType.USER:
        case this.SysFlowCopyForType.DEPT:
          tempId = this.formData.id;
          break;
        case this.SysFlowCopyForType.POST:
        case this.SysFlowCopyForType.SELF_DEPT_POST:
        case this.SysFlowCopyForType.UP_DEPT_POST:
        case this.SysFlowCopyForType.DEPT_POST:
          tempId = this.formData.id.join(',');
          break;
        default:
          tempId = '';
          break;
      }
      this.onCancel(true, {
        type: this.formData.type,
        id: tempId
      });
    },
    onDeptChange (val) {
      this.formData.id = undefined;
    },
    onCopyForTypeChange (val) {
      this.formData.id = undefined;
      this.deptId = undefined;
      this.selectDeptList = [];
    },
    onSelectUser () {
      let usedUserIdList = (this.formData.id == null || this.formData.id === '') ? [] : this.formData.id.split(',');
      this.$dialog.show('添加抄送人', TaskUserSelect, {
        area: ['1000px', '600px']
      }, {
        multiple: true,
        usedUserIdList: usedUserIdList
      }).then(res => {
        let userList = [];
        if (Array.isArray(res)) {
          userList = res.map(item => item.loginName);
        } else {
          if (res && res.loginName !== '') {
            userList = [res.loginName];
          }
        }
        // 跟老的候选人合并
        if (Array.isArray(usedUserIdList) && usedUserIdList.length > 0) {
          userList.forEach(item => {
            if (usedUserIdList.indexOf(item) === -1) {
              usedUserIdList.push(item);
            }
          });
        } else {
          usedUserIdList = userList;
        }

        this.formData.id = usedUserIdList.join(',');
      }).catch(e => {});
    },
    onSelectDept () {
      let usedIdList = (this.formData.id == null || this.formData.id === '') ? [] : this.formData.id.split(',');
      this.$dialog.show('选择部门', TaskGroupSelect, {
        area: ['600px', '600px']
      }, {
        allGroupList: this.deptList,
        usedIdList: usedIdList
      }).then(res => {
        if (Array.isArray(res)) {
          if (!Array.isArray(this.selectDeptList)) this.selectDeptList = [];
          res.forEach(item => {
            if (findItemFromList(this.selectDeptList, item.id, 'id') == null) {
              this.selectDeptList.push(item);
            }
          });
        }
        this.formData.id = Array.isArray(this.selectDeptList) ? this.selectDeptList.map(item => item.id).join(',') : '';
      }).catch(e => {});
    }
  },
  computed: {
    validDeptPostList () {
      let id = Array.isArray(this.deptId) ? this.deptId[this.deptId.length - 1] : undefined;
      if (id == null) return [];
      return this.deptPostList.filter(item => {
        return item.deptId === id;
      });
    }
  }
}
</script>

<style scoped>
  .append-add {
    border: none;
    border-left: 1px solid #DCDFE6;
    border-radius: 0px;
    background: #F5F7FA;
  }
</style>
