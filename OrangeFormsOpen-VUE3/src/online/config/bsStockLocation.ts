import {
  SysCustomWidgetType,
  SysOnlineFormType,
  OnlineFormEventType,
} from '@/common/staticDict/index';
import { ANY_OBJECT } from '@/types/generic';

const bsStockLocation = {
  span: {
    name: '组件宽度',
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
    name: '占位文本',
    widgetType: SysCustomWidgetType.Input,
    value: '',
  },
  depend: {
    name: '依赖于',
    widgetType: SysCustomWidgetType.Input,
    value: '',
  },
  activeStep: {
    name: '归属',
    widgetType: SysCustomWidgetType.NumberInput,
    visible: false,
    value: '',
  },
  tableName: {
    name: '默认表',
    visible: false,
    value: 'zz_test_order_first',
  },
  columnName: {
    name: '默认列',
    visible: false,
    value: 'stock_location',
  },
};

const bsStockLocationConfig = {
  widgetType: SysCustomWidgetType.BsStockLocation,
  icon: 'online-icon icon-input',
  attribute: bsStockLocation,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE,
  ],
  supportBindTable: true,
  supportBindColumn: true,
};

export default bsStockLocationConfig;
