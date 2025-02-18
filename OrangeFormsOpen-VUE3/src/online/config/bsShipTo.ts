import {
  SysCustomWidgetType,
  SysOnlineFormType,
  OnlineFormEventType,
} from '@/common/staticDict/index';
import { ANY_OBJECT } from '@/types/generic';

const bsShipTo = {
  span: {
    name: 'Span',
    widgetType: SysCustomWidgetType.Slider,
    value: 13,
    visible: function (formConfig: ANY_OBJECT) {
      return (
        formConfig &&
        formConfig.form.formType !== SysOnlineFormType.QUERY &&
        formConfig.activeMode === 'pc'
      );
    },
    disabled: false,
    min: 1,
    max: 24,
  },
  placeholder: {
    name: 'Place Holder',
    widgetType: SysCustomWidgetType.Input,
    value: '',
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
    value: 'ship_to',
  },
};

const bsShipToConfig = {
  widgetType: SysCustomWidgetType.BsShipTo,
  icon: 'online-icon icon-input',
  attribute: bsShipTo,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE,
  ],
  supportBindTable: true,
  supportBindColumn: true,
};

export default bsShipToConfig;
