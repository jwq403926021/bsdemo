import { SysCustomWidgetType, OnlineFormEventType } from '@/common/staticDict/index';

const bsRemarksToWarehouse = {
  span: {
    name: 'Span',
    widgetType: SysCustomWidgetType.Slider,
    value: 24,
    visible: false,
    min: 1,
    max: 24,
  },
  activeStep: {
    name: 'Active Step',
    widgetType: SysCustomWidgetType.NumberInput,
    visible: false,
    value: '',
  },
};

const bsRemarksToWarehouseConfig = {
  widgetType: SysCustomWidgetType.BsRemarksToWarehouse,
  icon: 'online-icon icon-dept',
  attribute: bsRemarksToWarehouse,
  allowEventList: [
    OnlineFormEventType.CHANGE,
    OnlineFormEventType.DISABLE,
    OnlineFormEventType.VISIBLE,
  ],
  supportBindTable: true,
  supportBindColumn: true,
};

export default bsRemarksToWarehouseConfig;
