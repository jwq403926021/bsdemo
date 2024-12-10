import {
  SysCustomWidgetType,
  SysOnlineFormType,
  OnlineFormEventType,
} from '@/common/staticDict/index';
import { ANY_OBJECT } from '@/types/generic';

const bsPhone = {
  span: {
    name: 'Span',
    widgetType: SysCustomWidgetType.Slider,
    value: 13,
    visible: false,
    min: 1,
    max: 24,
  },
  depend: {
    name: 'Depend',
    widgetType: SysCustomWidgetType.Input,
    value: '',
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
    value: 'phone',
  },
};

const bsPhoneConfig = {
  widgetType: SysCustomWidgetType.BsPhone,
  icon: 'online-icon icon-dept',
  attribute: bsPhone,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE,
  ],
  supportBindTable: true,
  supportBindColumn: true,
};

export default bsPhoneConfig;
