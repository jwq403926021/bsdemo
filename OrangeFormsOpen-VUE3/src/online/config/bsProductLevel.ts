import {
  SysCustomWidgetType,
  SysOnlineFormType,
  OnlineFormEventType,
} from '@/common/staticDict/index';
import { ANY_OBJECT } from '@/types/generic';

const bsProductLevel = {
  span: {
    name: 'Span',
    widgetType: SysCustomWidgetType.Slider,
    value: 13,
    visible: true,
    min: 1,
    max: 24,
  },
  activeStep: {
    name: 'Active Step',
    widgetType: SysCustomWidgetType.NumberInput,
    visible: false,
    value: '',
  },
  tableName: {
    name: 'Table Name',
    visible: false,
    value: 'zz_test_order_first',
  },
  columnName: {
    name: 'Column Name',
    visible: false,
    value: 'product_level',
  },
};

const bsProductLevelConfig = {
  widgetType: SysCustomWidgetType.BsProductLevel,
  icon: 'online-icon icon-dept',
  attribute: bsProductLevel,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE,
  ],
  supportBindTable: true,
  supportBindColumn: true,
};

export default bsProductLevelConfig;
