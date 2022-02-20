import state from '../store/state.js';
// 开发环境不使用懒加载, 因为懒加载页面太多的话会造成webpack热更新太慢
const _import = require('./import-' + process.env.NODE_ENV)

function getProps (route) {
  return route.query;
}

// 系统生成路由
const routers = [
  { path: '/login', component: _import('login/index'), name: 'login', props: getProps, desc: '登录' },
  {
    path: '/',
    component: _import('login/index'),
    name: 'root'
  },
  {
    path: '/main',
    component: _import('layout/index'),
    name: 'main',
    props: getProps,
    redirect: {
      name: 'welcome'
    },
    meta: {
      title: '主页',
      showOnly: true
    },
    children: [
      {path: 'formSysUser', component: _import('upms/formSysUser/index'), name: 'formSysUser', meta: {title: '用户管理'}},
      {path: 'formSysDept', component: _import('upms/formSysDept/index'), name: 'formSysDept', meta: {title: '部门管理'}},
      {path: 'formSysRole', component: _import('upms/formSysRole/index'), name: 'formSysRole', meta: {title: '角色管理'}},
      {path: 'formSysDataPerm', component: _import('upms/formSysDataPerm/index'), name: 'formSysDataPerm', meta: {title: '数据权限管理'}},
      {path: 'formSysMenu', component: _import(state.supportColumn ? 'upms/formSysMenu/formSysColumnMenu' : 'upms/formSysMenu/index'), name: 'formSysMenu', meta: {title: '菜单列表'}},
      {path: 'formSysDict', component: _import('upms/formDictManagement/index'), name: 'formSysDict', meta: {title: '字典管理'}},
      {path: 'formSysPermCode', component: _import('upms/formSysPermCode/index'), name: 'formSysPermCode', meta: {title: '权限字管理'}},
      {path: 'formSysPerm', component: _import('upms/formSysPerm/index'), name: 'formSysPerm', meta: {title: '权限资源管理'}},
      {path: 'formSysOperationLog', component: _import('upms/formSysOperationLog/index'), name: 'formSysOperationLog', meta: {title: '操作日志'}},
      {path: 'formSysLoginUser', component: _import('upms/formSysLoginUser/index'), name: 'formSysLoginUser', meta: {title: '在线用户'}},
      {path: 'welcome', component: _import('welcome/index'), name: 'welcome', meta: {title: '欢迎'}}
    ]
  }
];

export default routers;
