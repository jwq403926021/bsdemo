<template>
  <el-col class="copy-select" :span="24">
    <el-table :data="tableDataList" :show-header="false">
      <el-table-column label="操作" width="45px">
        <template slot-scope="scope">
          <el-button class="table-btn delete" type="text" icon="el-icon-remove-outline"
            @click="onDeleteCopyItem(scope.row)"
          />
        </template>
      </el-table-column>
      <el-table-column label="抄送类型" width="150px">
        <template slot-scope="scope">
          <span>{{SysFlowCopyForType.getValue(scope.row.type)}}</span>
        </template>
      </el-table-column>
      <el-table-column label="抄送对象">
        <template slot-scope="scope">
          <el-tag :size="defaultFormItemSize" type="primary" effect="dark"
            v-for="item in scope.row.showNameList" :key="item.id"
            closable @close="onCloseSubItem(scope.row, item)"
          >
            {{item.name}}
          </el-tag>
        </template>
      </el-table-column>
    </el-table>
    <el-button class="full-line-btn" icon="el-icon-plus" @click="onEditCopyForItem()">添加抄送人</el-button>
  </el-col>
</template>

<script>
import { treeDataTranslate } from '@/utils';
import { SysPostController, DictionaryController } from '@/api';
import addCopyForItem from './addCopyForItem.vue';

export default {
  name: 'copyForSelect',
  props: {
    value: {
      type: Array,
      default: () => []
    }
  },
  data () {
    return {
      isInit: false,
      roleList: [],
      roleMap: new Map(),
      deptList: [],
      deptMap: new Map(),
      postList: [],
      postMap: new Map(),
      deptPostList: [],
      deptPostMap: new Map()
    }
  },
  methods: {
    onDeleteCopyItem (row) {
      this.$confirm('是否删除此抄送人？').then(res => {
        let temp = (this.value || []).filter(item => {
          return row.type !== item.type;
        });
        this.$emit('input', temp);
      }).catch(e => {});
    },
    onCloseSubItem (row, item) {
      this.$confirm('是否移除此抄送人？').then(res => {
        let temp = (this.value || []).filter(copyItem => {
          if (row.type === copyItem.type) {
            if (copyItem.id == null || copyItem.id === '') return false;
            let tempIdList = (copyItem.id || '').split(',');
            tempIdList = tempIdList.filter(subItemId => {
              return subItemId !== item.id;
            });
            if (tempIdList.length <= 0) {
              return false;
            } else {
              copyItem.id = tempIdList.join(',');
              return true;
            }
          } else {
            return true;
          }
        });
        this.$emit('input', temp);
      }).catch(e => {});
    },
    onEditCopyForItem () {
      this.$dialog.show('添加抄送人', addCopyForItem, {
        area: '600px'
      }, {
        roleList: this.roleList,
        deptList: this.deptList,
        postList: this.postList,
        deptPostList: this.deptPostList
      }).then(res => {
        let copyForType = res.type;
        let bFind = false;
        // 按照抄送类型更新
        let temp = (this.value || []).map(copyItem => {
          if (copyItem.type === copyForType) {
            bFind = true;
            let oldIdList = (copyItem.id || '').split(',');
            let newIdList = (res.id || '').split(',');
            // 合并新旧选项
            let idList = oldIdList;
            newIdList.forEach(id => {
              if (idList.indexOf(id) === -1) idList.push(id);
            });
            return {
              ...copyItem,
              id: idList.join(',')
            };
          } else {
            return {
              ...copyItem
            }
          }
        });
        if (!bFind) {
          temp.push({
            ...res
          });
        }
        this.$emit('input', temp);
      }).catch(e => {});
    },
    loadSysDeptList () {
      return new Promise((resolve, reject) => {
        DictionaryController.dictSysDept(this, {}).then(res => {
          res.getList().forEach(item => {
            this.deptMap.set(item.id, item);
          });
          this.deptList = treeDataTranslate(res.getList());
          resolve();
        }).catch(e => {
          reject(e);
        });
      });
    },
    loadDeptPostList () {
      return new Promise((resolve, reject) => {
        DictionaryController.dictDeptPost(this, {}).then(res => {
          res.forEach(item => {
            this.deptPostMap.set(item.deptPostId, item);
          });
          this.deptPostList = res.sort((value1, value2) => {
            return value1.level - value2.level;
          });
          resolve();
        }).catch(e => {
          reject(e);
        });
      });
    },
    loadSysPostList () {
      this.postMap = new Map();
      return new Promise((resolve, reject) => {
        SysPostController.list(this, {}).then(res => {
          this.postList = res.data.dataList;
          this.postList.forEach(item => {
            this.postMap.set(item.postId, item);
          });
          resolve();
        }).catch(e => {
          reject(e);
        });
      });
    },
    loadSysRoleList () {
      return new Promise((resolve, reject) => {
        DictionaryController.dictSysRole(this, {}).then(res => {
          this.roleList = res.getList();
          this.roleList.forEach(item => {
            this.roleMap.set(item.id, item);
          });
          resolve();
        }).catch(e => {
          reject(e);
        });
      });
    }
  },
  computed: {
    tableDataList () {
      if (this.isInit && Array.isArray(this.value)) {
        return this.value.map(item => {
          let showNameList = (item.id || '').split(',');
          switch (item.type) {
            case this.SysFlowCopyForType.USER:
              showNameList = showNameList.map(id => {
                return (id && id !== '') ? {
                  id: id,
                  name: id
                } : undefined
              }).filter(item => item != null);
              break;
            case this.SysFlowCopyForType.ROLE:
              showNameList = showNameList.map(id => {
                let role = this.roleMap.get(id);
                return role ? {
                  id: id,
                  name: role.name
                } : undefined
              }).filter(item => item != null);
              break;
            case this.SysFlowCopyForType.DEPT:
              showNameList = showNameList.map(id => {
                let dept = this.deptMap.get(id);
                return dept ? {
                  id: id,
                  name: dept.name
                } : undefined
              }).filter(item => item != null);
              break;
            case this.SysFlowCopyForType.SELF_DEPT_LEADER:
            case this.SysFlowCopyForType.UP_DEPT_LEADER:
              showNameList = [];
              break;
            case this.SysFlowCopyForType.POST:
            case this.SysFlowCopyForType.SELF_DEPT_POST:
            case this.SysFlowCopyForType.UP_DEPT_POST:
              showNameList = showNameList.map(id => {
                let post = this.postMap.get(id);
                return post ? {
                  id: id,
                  name: post.postName
                } : undefined
              }).filter(item => item != null);
              break;
            case this.SysFlowCopyForType.DEPT_POST:
              showNameList = showNameList.map(id => {
                let deptPost = this.deptPostMap.get(id);
                return deptPost ? {
                  id: id,
                  name: deptPost.deptName + ' / ' + deptPost.postShowName
                } : undefined
              }).filter(item => item != null);
              break;
          }
          return {
            ...item,
            showNameList
          }
        });
      }
      return [];
    }
  },
  mounted () {
    let httpCalls = [
      this.loadSysDeptList(),
      this.loadSysPostList(),
      this.loadDeptPostList(),
      this.loadSysRoleList()
    ];
    Promise.all(httpCalls).then(res => {
      this.isInit = true;
    }).catch(e => {});
  },
  beforeDestroy () {
    this.roleMap = null;
    this.deptMap = null;
    this.postMap = null;
    this.deptPostMap = null;
  }
}
</script>

<style scoped>
  .full-line-btn {
    width: 100%;
    margin: 10px 0px;
    border: 1px dashed #EBEEF5;
  }

  .copy-select >>> .el-tag {
    margin-right: 10px;
  }
</style>
