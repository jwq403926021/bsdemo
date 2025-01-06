import { DictionaryBase } from './types';

const SysUserStatus = new DictionaryBase('User Status', [
  {
    id: 0,
    name: 'Normal',
    symbol: 'NORMAL',
  },
  {
    id: 1,
    name: 'Locked',
    symbol: 'LOCKED',
  },
]);

const SysUserType = new DictionaryBase('User Type', [
  {
    id: 0,
    name: 'Admin',
    symbol: 'ADMIN',
  },
  {
    id: 1,
    name: 'System',
    symbol: 'SYSTEM',
  },
  {
    id: 2,
    name: 'Operator',
    symbol: 'OPERATOR',
  },
]);

const SysOperationType = new DictionaryBase('Operation Log Operation Type', [
  {
    id: 0,
    name: 'Login',
    symbol: 'LOGIN',
  },
  {
    id: 1,
    name: 'Mobile Login',
    symbol: 'MOBILE_LOGIN',
  },
  {
    id: 5,
    name: 'Logout',
    symbol: 'LOGOUT',
  },
  {
    id: 10,
    name: 'Add',
    symbol: 'ADD',
  },
  {
    id: 15,
    name: 'Update',
    symbol: 'UPDATE',
  },
  {
    id: 20,
    name: 'Delete',
    symbol: 'DELETE',
  },
  {
    id: 35,
    name: 'List',
    symbol: 'LIST',
  },
  {
    id: 40,
    name: 'List With Group',
    symbol: 'LIST_WITH_GROUP',
  },
  {
    id: 45,
    name: 'Export',
    symbol: 'EXPORT',
  },
  {
    id: 25,
    name: 'Add M2M',
    symbol: 'ADD_M2M',
  },
  {
    id: 30,
    name: 'Delete M2M',
    symbol: 'DELETE_M2M',
  },
  {
    id: 50,
    name: 'Upload',
    symbol: 'UPLOAD',
  },
  {
    id: 55,
    name: 'Download',
    symbol: 'DOWNLOAD',
  },
  {
    id: 60,
    name: 'Reload Cache',
    symbol: 'RELOAD_CACHE',
  },
  {
    id: 65,
    name: 'Publish',
    symbol: 'PUBLISH',
  },
  {
    id: 70,
    name: 'Unpublish',
    symbol: 'UNPUBLISH',
  },
  {
    id: 75,
    name: 'Suspend',
    symbol: 'SUSPEND',
  },
  {
    id: 80,
    name: 'Resume',
    symbol: 'RESUME',
  },
  {
    id: 100,
    name: 'Start Flow',
    symbol: 'START_FLOW',
  },
  {
    id: 105,
    name: 'Stop Flow',
    symbol: 'STOP_FLOW',
  },
  {
    id: 110,
    name: 'Delete Flow',
    symbol: 'DELETE_FLOW',
  },
  {
    id: 115,
    name: 'Cancel Flow',
    symbol: 'CANCEL_FLOW',
  },
  {
    id: 120,
    name: 'Submit Task',
    symbol: 'SUBMIT_TASK',
  },
  {
    id: 125,
    name: 'Remind Task',
    symbol: 'REMIND_TASK',
  },
  {
    id: 126,
    name: 'Intervene Flow',
    symbol: 'INTERVENE_FLOW',
  },
  {
    id: 127,
    name: 'Fix Flow Business Data',
    symbol: 'FIX_FLOW_BUSINESS_DATA',
  },
]);

const SysPermModuleType = new DictionaryBase('Permission Group Type', [
  {
    id: 0,
    name: 'Group Module',
    symbol: 'GROUP',
  },
  {
    id: 1,
    name: 'Controller Module',
    symbol: 'CONTROLLER',
  },
]);

const SysPermCodeType = new DictionaryBase('Permission Code Type', [
  {
    id: 0,
    name: 'Form',
    symbol: 'FORM',
  },
  {
    id: 1,
    name: 'Fragment',
    symbol: 'FRAGMENT',
  },
  {
    id: 2,
    name: 'Operation',
    symbol: 'OPERATION',
  },
]);

/**
 * Menu Type
 *
 * DIRECTORY(0: Directory)
 * MENU(1: Form)
 * FRAGMENT(2: Fragment)
 * BUTTON(3: Button)
 */
const SysMenuType = new DictionaryBase('Menu Type', [
  {
    id: 0,
    name: 'Directory',
    symbol: 'DIRECTORY',
  },
  {
    id: 1,
    name: 'Menu',
    symbol: 'MENU',
  },
  {
    id: 2,
    name: 'Fragment',
    symbol: 'FRAGMENT',
  },
  {
    id: 3,
    name: 'Button',
    symbol: 'BUTTON',
  },
]);

const MobileEntryType = new DictionaryBase('Mobile Home Page Configuration Type', [
  {
    id: 0,
    name: 'Carousel',
    symbol: 'BANNER',
  },
  {
    id: 1,
    name: 'Sudoku',
    symbol: 'SUDOKU',
  },
  {
    id: 2,
    name: 'Group',
    symbol: 'GROUP',
  },
]);

/**
 * Menu Bind Type
 *
 * ROUTER(0: Router Menu)
 * ONLINE_FORM(1: Online Form)
 * WORK_ORDER(2: Work Order List)
 * REPORT(3: Report Page)
 * THRID_URL(4: External Link)
 */
const SysMenuBindType = new DictionaryBase('Menu Bind Type', [
  {
    id: 0,
    name: 'Router Menu',
    symbol: 'ROUTER',
  },
  {
    id: 1,
    name: 'Online Form',
    symbol: 'ONLINE_FORM',
  },
  {
    id: 2,
    name: 'Work Order List',
    symbol: 'WORK_ORDER',
  },
  {
    id: 3,
    name: 'Report Page',
    symbol: 'REPORT',
  },
  {
    id: 4,
    name: 'External Link',
    symbol: 'THRID_URL',
  },
]);

const SysDataPermType = new DictionaryBase('Data Permission Type', [
  {
    id: 0,
    name: 'View All',
    symbol: 'ALL',
  },
  {
    id: 1,
    name: 'Only Self',
    symbol: 'ONLY_USER',
  },
  // {
  //   id: 2,
  //   name: 'Only Department',
  //   symbol: 'ONLY_DEPT',
  // },
  // {
  //   id: 3,
  //   name: 'Only Department And Child',
  //   symbol: 'ONLY_DEPT_AND_CHILD',
  // },
  // {
  //   id: 4,
  //   name: 'Custom Department And Child',
  //   symbol: 'CUSTOM_DEPT_AND_CHILD',
  // },
  {
    id: 5,
    name: 'Only Custom Department',
    symbol: 'CUSTOM_DEPT',
  },
  // {
  //   id: 6,
  //   name: 'Department User',
  //   symbol: 'DEPT_USER',
  // },
  // {
  //   id: 7,
  //   name: 'Department And Child User',
  //   symbol: 'DEPT_AND_CHILD_USER',
  // },
]);

const ScatterSymbolType = new DictionaryBase('Vertical Axis Position', [
  {
    id: 0,
    name: 'Fixed Size',
    symbol: 'FIXED',
  },
  {
    id: 1,
    name: 'Value Size',
    symbol: 'VALUE',
  },
]);

const SysCustomWidgetType = new DictionaryBase('Component Type', [
  {
    id: 0,
    name: 'Label',
    symbol: 'Label',
  },
  {
    id: 1,
    name: 'Input',
    symbol: 'Input',
  },
  {
    id: 3,
    name: 'Number Input',
    symbol: 'NumberInput',
  },
  {
    id: 4,
    name: 'Number Range Input',
    symbol: 'NumberRange',
  },
  {
    id: 5,
    name: 'Switch',
    symbol: 'Switch',
  },
  {
    id: 6,
    name: 'Slider',
    symbol: 'Slider',
  },
  {
    id: 7,
    name: 'Radio',
    symbol: 'Radio',
  },
  {
    id: 8,
    name: 'CheckBox',
    symbol: 'CheckBox',
  },
  {
    id: 10,
    name: 'Select',
    symbol: 'Select',
  },
  {
    id: 12,
    name: 'Cascader',
    symbol: 'Cascader',
  },
  {
    id: 13,
    name: 'Tree',
    symbol: 'Tree',
  },
  {
    id: 14,
    name: 'Rate',
    symbol: 'Rate',
  },
  {
    id: 15,
    name: 'Stepper',
    symbol: 'Stepper',
  },
  {
    id: 16,
    name: 'Calendar',
    symbol: 'Calendar',
  },
  {
    id: 20,
    name: 'Date',
    symbol: 'Date',
  },
  {
    id: 21,
    name: 'Date Range',
    symbol: 'DateRange',
  },
  {
    id: 30,
    name: 'Color Picker',
    symbol: 'ColorPicker',
  },
  {
    id: 31,
    name: 'Upload',
    symbol: 'Upload',
  },
  {
    id: 32,
    name: 'RichEditor',
    symbol: 'RichEditor',
  },
  {
    id: 40,
    name: 'Divider',
    symbol: 'Divider',
  },
  {
    id: 41,
    name: 'Text',
    symbol: 'Text',
  },
  {
    id: 42,
    name: 'Image',
    symbol: 'Image',
  },
  {
    id: 43,
    name: 'Link',
    symbol: 'Link',
  },
  {
    id: 100,
    name: 'Table',
    symbol: 'Table',
  },
  {
    id: 101,
    name: 'Pivot Table',
    symbol: 'PivotTable',
  },
  {
    id: 102,
    name: 'List',
    symbol: 'List',
  },
  {
    id: 103,
    name: 'Query List',
    symbol: 'QueryList',
  },
  {
    id: 104,
    name: 'Work Order List',
    symbol: 'WorkOrderList',
  },
  {
    id: 200,
    name: 'Line Chart',
    symbol: 'LineChart',
  },
  {
    id: 201,
    name: 'Bar Chart',
    symbol: 'BarChart',
  },
  {
    id: 202,
    name: 'Pie Chart',
    symbol: 'PieChart',
  },
  {
    id: 203,
    name: 'Scatter Chart',
    symbol: 'ScatterChart',
  },
  {
    id: 204,
    name: 'Data View Table',
    symbol: 'DataViewTable',
  },
  {
    id: 205,
    name: 'Carousel',
    symbol: 'Carousel',
  },
  {
    id: 206,
    name: 'Rich Text',
    symbol: 'RichText',
  },
  {
    id: 207,
    name: 'Gauge Chart',
    symbol: 'GaugeChart',
  },
  {
    id: 208,
    name: 'Funnel Chart',
    symbol: 'FunnelChart',
  },
  {
    id: 209,
    name: 'Radar Chart',
    symbol: 'RadarChart',
  },
  {
    id: 210,
    name: 'Progress Bar',
    symbol: 'ProgressBar',
  },
  {
    id: 211,
    name: 'Progress Circle',
    symbol: 'ProgressCircle',
  },
  {
    id: 212,
    name: 'Data Card',
    symbol: 'DataCard',
  },
  {
    id: 213,
    name: 'Common List',
    symbol: 'CommonList',
  },
  {
    id: 214,
    name: 'Data Progress Card',
    symbol: 'DataProgressCard',
  },
  {
    id: 300,
    name: 'Block',
    symbol: 'Block',
  },
  {
    id: 301,
    name: 'Card',
    symbol: 'Card',
  },
  {
    id: 302,
    name: 'Tabs',
    symbol: 'Tabs',
  },
  {
    id: 303,
    name: 'Image Card',
    symbol: 'ImageCard',
  },
  {
    id: 304,
    name: 'Cell Group',
    symbol: 'CellGroup',
  },
  {
    id: 400,
    name: 'User Select',
    symbol: 'UserSelect',
  },
  {
    id: 404,
    name: 'Division',
    symbol: 'BsDivision',
  },
  {
    id: 416,
    name: 'Product Level',
    symbol: 'BsProductLevel',
  },
  {
    id: 417,
    name: 'Product Level Name',
    symbol: 'BsProductLevelName',
  },
  {
    id: 405,
    name: 'SR',
    symbol: 'BsSr',
  },
  {
    id: 406,
    name: 'Account Name',
    symbol: 'BsAccountName',
  },
  {
    id: 412,
    name: 'Ship To',
    symbol: 'BsShipTo',
  },
  {
    id: 407,
    name: 'Contact Info',
    symbol: 'BsContactInfo',
  },
  {
    id: 408,
    name: 'Stock Location',
    symbol: 'BsStockLocation',
  },
  {
    id: 409, // do not change
    name: 'Product', // do not change
    symbol: 'BsProduct', // do not change
  },
  {
    id: 414,
    name: 'Product Confirm',
    symbol: 'BsProductConfirm',
  },
  {
    id: 415,
    name: 'UPN • Product Name',
    symbol: 'BsUpnProductName',
  },
  {
    id: 410,
    name: 'Shipping Order Info',
    symbol: 'BsShippingOrderInfo',
  },
  {
    id: 411,
    name: 'Contact Info For Packing List',
    symbol: 'BsContactInfoForPackingList',
  },
  {
    id: 413,
    name: 'Request Delivery Date',
    symbol: 'BsRequestDeliveryDate',
  },
  {
    id: 420,
    name: 'Recipient',
    symbol: 'BsRecipient',
  },
  {
    id: 421,
    name: 'Phone',
    symbol: 'BsPhone',
  },
  {
    id: 401,
    name: 'Dept Select',
    symbol: 'DeptSelect',
  },
  {
    id: 402,
    name: 'Data Select',
    symbol: 'DataSelect',
  },
  {
    id: 403,
    name: 'Table Container',
    symbol: 'TableContainer',
  },
  {
    id: 500,
    name: 'Mobile Radio Filter',
    symbol: 'MobileRadioFilter',
  },
  {
    id: 501,
    name: 'Mobile CheckBox Filter',
    symbol: 'MobileCheckBoxFilter',
  },
  {
    id: 502,
    name: 'Mobile Input Filter',
    symbol: 'MobileInputFilter',
  },
  {
    id: 503,
    name: 'Mobile Switch Filter',
    symbol: 'MobileSwitchFilter',
  },
  {
    id: 504,
    name: 'Mobile Date Range Filter',
    symbol: 'MobileDateRangeFilter',
  },
  {
    id: 505,
    name: 'Mobile Number Range Filter',
    symbol: 'MobileNumberRangeFilter',
  },
]);

const OnlineFormEventType = new DictionaryBase('Online Form Event Type', [
  {
    id: 'change',
    name: 'Data Change',
    symbol: 'CHANGE',
  },
  {
    id: 'disable',
    name: 'Is Disabled',
    symbol: 'DISABLE',
  },
  {
    id: 'visible',
    name: 'Is Visible',
    symbol: 'VISIBLE',
  },
  {
    id: 'dropdownChange',
    name: 'Dropdown Data Change',
    symbol: 'DROPDOWN_CHANGE',
  },
  {
    id: 'linkHerf',
    name: 'Link Address',
    symbol: 'LINK_HERF',
  },
  {
    id: 'disabledDate',
    name: 'Date Availability',
    symbol: 'DISABLED_DATE',
  },
  {
    id: 'afterLoadTableData',
    name: 'After Load Table Data',
    symbol: 'AFTER_LOAD_TABLE_DATA',
  },
  {
    id: 'beforeLoadTableData',
    name: 'Before Load Table Data',
    symbol: 'BEFORE_LOAD_TABLE_DATA',
  },
  {
    id: 'afterLoadFormData',
    name: 'After Load Form Data',
    symbol: 'AFTER_LOAD_FORM_DATA',
  },
  {
    id: 'beforeLoadFormData',
    name: 'Before Load Form Data',
    symbol: 'BEFORE_LOAD_FORM_DATA',
  },
  {
    id: 'beforeCommitFormData',
    name: 'Before Commit Form Data',
    symbol: 'BEFORE_COMMIT_FORM_DATA',
  },
  {
    id: 'formCreated',
    name: 'After Create Form',
    symbol: 'AFTER_CREATE_FORM',
  },
  {
    id: 'tableOperationVisible',
    name: 'Operation Visibility',
    symbol: 'OPERATION_VISIBLE',
  },
  {
    id: 'tableOperationDisbled',
    name: 'Operation Disabled',
    symbol: 'OPERATION_DISABLED',
  },
]);

/**
 * Form Type
 *
 * QUERY(1: Query Form)
 * ADVANCE_QUERY(2: Left Tree Right Table Query)
 * ONE_TO_ONE_QUERY(3: One-To-One Query)
 * FORM(5: Edit Form)
 * FLOW(10: Flow Form)
 * WORK_ORDER(11: Work Order List)
 * REPORT(50: Report Page)
 */
const SysOnlineFormType = new DictionaryBase('Form Type', [
  {
    id: 1,
    name: 'Query Form',
    symbol: 'QUERY',
  },
  {
    id: 2,
    name: 'Left Tree Right Table Query',
    symbol: 'ADVANCE_QUERY',
  },
  {
    id: 3,
    name: 'One-To-One Query',
    symbol: 'ONE_TO_ONE_QUERY',
  },
  {
    id: 5,
    name: 'Edit Form',
    symbol: 'FORM',
  },
  {
    id: 10,
    name: 'Flow Form',
    symbol: 'FLOW',
  },
  {
    id: 11,
    name: 'Work Order List',
    symbol: 'WORK_ORDER',
  },
  {
    id: 50,
    name: 'Report Page',
    symbol: 'REPORT',
  },
]);

const SysCustomWidgetOperationType = new DictionaryBase('Operation Type', [
  {
    id: 0,
    name: 'Add',
    symbol: 'ADD',
  },
  {
    id: 1,
    name: 'Edit',
    symbol: 'EDIT',
  },
  {
    id: 2,
    name: 'Delete',
    symbol: 'DELETE',
  },
  {
    id: 3,
    name: 'Export',
    symbol: 'EXPORT',
  },
  {
    id: 10,
    name: 'Batch Delete',
    symbol: 'BATCH_DELETE',
  },
  {
    id: 20,
    name: 'Form Operation',
    symbol: 'FORM',
  },
  {
    id: 22,
    name: 'Copy',
    symbol: 'COPY',
  },
  {
    id: 30,
    name: 'Save',
    symbol: 'SAVE',
  },
  {
    id: 31,
    name: 'Cancel',
    symbol: 'CANCEL',
  },
  {
    id: 50,
    name: 'Script Operation',
    symbol: 'SCRIPT',
  },
  {
    id: 51,
    name: 'Drill Event',
    symbol: 'DRILL',
  },
  {
    id: 52,
    name: 'Route Jump',
    symbol: 'ROUTE',
  },
]);

const OnlineSystemVariableType = new DictionaryBase('System Variable Type', [
  {
    id: 0,
    name: 'Current User',
    symbol: 'CURRENT_USER',
  },
  {
    id: 1,
    name: 'Current Department',
    symbol: 'CURRENT_DEPT',
  },
  {
    id: 10,
    name: 'Current Date',
    symbol: 'CURRENT_DATE',
  },
  {
    id: 11,
    name: 'Current Time',
    symbol: 'CURRENT_TIME',
  },
  {
    id: 20,
    name: 'Flow Initiator',
    symbol: 'FLOW_CREATE_USER',
  },
]);

const SysCustomWidgetBindDataType = new DictionaryBase('Component Bind Data Type', [
  {
    id: 0,
    name: 'Field',
    symbol: 'Column',
  },
  {
    id: 5,
    name: 'System Variable',
    symbol: 'SYSTEM_VARIABLE',
  },
  {
    id: 10,
    name: 'Custom Field',
    symbol: 'Custom',
  },
  {
    id: 20,
    name: 'Fixed Value',
    symbol: 'Fixed',
  },
]);

const DirectionType = new DictionaryBase('Direction', [
  {
    id: 0,
    name: 'Horizontal',
    symbol: 'HORIZONTAL',
  },
  {
    id: 1,
    name: 'Vertical',
    symbol: 'VERTICAL',
  },
]);

const DblinkType = new DictionaryBase('Database Connection Type', [
  {
    id: 0,
    name: 'MySQL',
    symbol: 'MYSQL',
  },
  /*
  {
    id: 1,
    name: 'PostgreSQL',
    symbol: 'POSTGRESQL',
  },
  {
    id: 2,
    name: 'Oracle',
    symbol: 'ORACLE',
  },
  {
    id: 3,
    name: 'DM Database',
    symbol: 'DM_DB',
  },
  {
    id: 4,
    name: 'Kingbase',
    symbol: 'KINGBASE',
  },
  {
    id: 5,
    name: 'OpenGauss',
    symbol: 'OPENGAUSS',
  },
  {
    id: 10,
    name: 'ClickHouse',
    symbol: 'CLICK_HOUSE',
  },
  {
    id: 11,
    name: 'Doris',
    symbol: 'DORIS',
  },
  */
]);

const CriteriaFilterType = new DictionaryBase('Criteria Filter Type', [
  {
    id: 0,
    name: 'Equal',
    symbol: 'EQ',
  },
  {
    id: 1,
    name: 'Not Equal',
    symbol: 'NOT_EQ',
  },
  {
    id: 2,
    name: 'Greater Than Or Equal',
    symbol: 'GE',
  },
  {
    id: 3,
    name: 'Greater Than',
    symbol: 'GT',
  },
  {
    id: 4,
    name: 'Less Than Or Equal',
    symbol: 'LE',
  },
  {
    id: 5,
    name: 'Less Than',
    symbol: 'LT',
  },
  {
    id: 6,
    name: 'Like',
    symbol: 'LIKE',
  },
  {
    id: 7,
    name: 'In',
    symbol: 'IN',
  },
  {
    id: 8,
    name: 'Not In',
    symbol: 'NOT_IN',
  },
  {
    id: 9,
    name: 'Between',
    symbol: 'BETWEEN',
  },
  {
    id: 100,
    name: 'Not Null',
    symbol: 'NOT_NULL',
  },
  {
    id: 101,
    name: 'Is Null',
    symbol: 'IS_NULL',
  },
]);
export {
  SysUserStatus,
  SysUserType,
  SysDataPermType,
  SysOperationType,
  SysPermModuleType,
  SysPermCodeType,
  SysMenuBindType,
  MobileEntryType,
  SysMenuType,
  ScatterSymbolType,
  SysCustomWidgetType,
  OnlineFormEventType,
  SysOnlineFormType,
  SysCustomWidgetOperationType,
  OnlineSystemVariableType,
  SysCustomWidgetBindDataType,
  DirectionType,
  DblinkType,
  CriteriaFilterType,
};
