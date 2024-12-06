import {
  SysCustomWidgetType,
  SysOnlineFormType,
  OnlineFormEventType,
} from '@/common/staticDict/index';
import { ANY_OBJECT } from '@/types/generic';

const bsInput = {
  span: {
    name: '组件宽度',
    widgetType: SysCustomWidgetType.Slider,
    value: 24,
    visible: false,
    disabled: false,
    min: 1,
    max: 24,
  },
  activeStep: {
    name: '归属',
    visible: false,
    value: ''
  },
  tableName: {
    name: '默认表',
    visible: false,
    value: "zz_test_order_first"
  },
  columnName: {
    name: '默认列',
    visible: false,
    value: 'order_name'
  }
};

const bsInputConfig = {
  widgetType: SysCustomWidgetType.BsInput,
  icon: 'online-icon icon-input',
  attribute: bsInput,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE,
  ],
  supportBindTable: true,
  supportBindColumn: true,
};

export default bsInputConfig;
