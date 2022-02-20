/**
 * 常量字典数据
 */
import Vue from 'vue';

class DictionaryBase extends Map {
  constructor (name, dataList, keyId = 'id', symbolId = 'symbol') {
    super();
    this.showName = name;
    this.setList(dataList, keyId, symbolId);
  }

  setList (dataList, keyId = 'id', symbolId = 'symbol') {
    this.clear();
    if (Array.isArray(dataList)) {
      dataList.forEach((item) => {
        this.set(item[keyId], item);
        if (item[symbolId] != null) {
          Object.defineProperty(this, item[symbolId], {
            get: function () {
              return item[keyId];
            }
          });
        }
      });
    }
  }

  getList (valueId = 'name', parentIdKey = 'parentId', filter) {
    let temp = [];
    this.forEach((value, key) => {
      let obj = {
        id: key,
        name: (typeof value === 'string') ? value : value[valueId],
        parentId: value[parentIdKey]
      };
      if (typeof filter !== 'function' || filter(obj)) {
        temp.push(obj);
      }
    });

    return temp;
  }

  getValue (id, valueId = 'name') {
    // 如果id为boolean类型，则自动转换为0和1
    if (typeof id === 'boolean') {
      id = id ? 1 : 0;
    }
    return (this.get(id) || {})[valueId];
  }
}

const SysUserStatus = new DictionaryBase('用户状态', [
  {
    id: 0,
    name: '正常状态',
    symbol: 'NORMAL'
  },
  {
    id: 1,
    name: '锁定状态',
    symbol: 'LOCKED'
  }
]);
Vue.prototype.SysUserStatus = SysUserStatus;

const SysUserType = new DictionaryBase('用户类型', [
  {
    id: 0,
    name: '管理员',
    symbol: 'ADMIN'
  },
  {
    id: 1,
    name: '系统操作员',
    symbol: 'SYSTEM'
  },
  {
    id: 2,
    name: '普通操作员',
    symbol: 'OPERATOR'
  }
]);
Vue.prototype.SysUserType = SysUserType;

const SysOperationType = new DictionaryBase('操作日志操作类型', [
  {
    id: 0,
    name: '登录',
    symbol: 'LOGIN'
  },
  {
    id: 5,
    name: '登出',
    symbol: 'LOGOUT'
  },
  {
    id: 10,
    name: '新增',
    symbol: 'ADD'
  },
  {
    id: 15,
    name: '修改',
    symbol: 'UPDATE'
  },
  {
    id: 20,
    name: '删除',
    symbol: 'DELETE'
  },
  {
    id: 35,
    name: '查询',
    symbol: 'LIST'
  },
  {
    id: 40,
    name: '分组查询',
    symbol: 'LIST_WITH_GROUP'
  },
  {
    id: 45,
    name: '导出',
    symbol: 'EXPORT'
  },
  {
    id: 25,
    name: '新增多对多关联',
    symbol: 'ADD_M2M'
  },
  {
    id: 30,
    name: '移除多对多关联',
    symbol: 'DELETE_M2M'
  },
  {
    id: 50,
    name: '上传',
    symbol: 'UPLOAD'
  },
  {
    id: 55,
    name: '下载',
    symbol: 'DOWNLOAD'
  },
  {
    id: 60,
    name: '重置缓存',
    symbol: 'RELOAD_CACHE'
  }
]);
Vue.prototype.SysOperationType = SysOperationType;

const SysPermModuleType = new DictionaryBase('权限分组类型', [
  {
    id: 0,
    name: '分组模块',
    symbol: 'GROUP'
  }, {
    id: 1,
    name: '接口模块',
    symbol: 'CONTROLLER'
  }
]);
Vue.prototype.SysPermModuleType = SysPermModuleType;

const SysPermCodeType = new DictionaryBase('权限字类型', [{
  id: 0,
  name: '表单',
  symbol: 'FORM'
}, {
  id: 1,
  name: '片段',
  symbol: 'FRAGMENT'
}, {
  id: 2,
  name: '操作',
  symbol: 'OPERATION'
}]);
Vue.prototype.SysPermCodeType = SysPermCodeType;

const SysMenuType = new DictionaryBase('菜单类型', [
  {
    id: 0,
    name: '目录',
    symbol: 'DIRECTORY'
  },
  {
    id: 1,
    name: '表单',
    symbol: 'MENU'
  },
  {
    id: 2,
    name: '片段',
    symbol: 'FRAGMENT'
  },
  {
    id: 3,
    name: '按钮',
    symbol: 'BUTTON'
  }
]);
Vue.prototype.SysMenuType = SysMenuType;

const SysDataPermType = new DictionaryBase('数据权限类型', [
  {
    id: 0,
    name: '查看全部',
    symbol: 'ALL'
  },
  {
    id: 1,
    name: '仅看自己',
    symbol: 'ONLY_USER'
  },
  {
    id: 2,
    name: '仅看所在部门',
    symbol: 'ONLY_DEPT'
  },
  {
    id: 3,
    name: '仅看所在部门及子部门',
    symbol: 'ONLY_DEPT_AND_CHILD'
  },
  {
    id: 4,
    name: '自选部门及子部门',
    symbol: 'CUSTOM_DEPT_AND_CHILD'
  },
  {
    id: 5,
    name: '仅自选部门',
    symbol: 'CUSTOM_DEPT'
  }
]);
Vue.prototype.SysDataPermType = SysDataPermType;

export {
  DictionaryBase,
  SysUserStatus,
  SysUserType,
  SysDataPermType,
  SysOperationType,
  SysPermModuleType,
  SysPermCodeType,
  SysMenuType
}
