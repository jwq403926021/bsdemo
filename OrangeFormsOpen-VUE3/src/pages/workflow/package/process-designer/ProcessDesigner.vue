<template>
  <div class="my-process-designer">
    <div class="my-process-designer__header" v-show="diagramType == DiagramType.ORDINARY">
      <slot name="control-header"></slot>
      <template v-if="!$slots['control-header']">
        <el-button-group key="file-control">
          <el-button :size="headerButtonSize" :type="headerButtonType" :icon="Edit" @click="onSave"
            >Save Process</el-button
          >
          <el-button
            :size="headerButtonSize"
            :type="headerButtonType"
            :icon="FolderOpened"
            @click="openFile"
            >Open File</el-button
          >
          <el-tooltip effect="light">
            <template v-slot:content>
              <div class="popper-box">
                <el-button
                  :size="headerButtonSize"
                  link
                  type="primary"
                  @click="downloadProcessAsXml()"
                  >Download as XML File</el-button
                >
                <el-button
                  :size="headerButtonSize"
                  link
                  type="primary"
                  @click="downloadProcessAsSvg()"
                  >Download as SVG File</el-button
                >
                <el-button
                  :size="headerButtonSize"
                  link
                  type="primary"
                  @click="downloadProcessAsBpmn()"
                  >Download as BPMN File</el-button
                >
              </div>
            </template>
            <el-button :size="headerButtonSize" :type="headerButtonType" :icon="Download"
              >Download File</el-button
            >
          </el-tooltip>
          <el-tooltip effect="light">
            <template v-slot:content>
              <div class="popper-box">
                <el-button :size="headerButtonSize" link type="primary" @click="previewProcessXML"
                  >Preview XML</el-button
                >
                <el-button :size="headerButtonSize" link type="primary" @click="previewProcessJson"
                  >Preview JSON</el-button
                >
              </div>
            </template>
            <el-button :size="headerButtonSize" :type="headerButtonType" :icon="View"
              >Preview</el-button
            >
          </el-tooltip>
          <el-tooltip
            v-if="simulation"
            effect="light"
            :content="simulationStatus ? 'Exit Simulation' : 'Start Simulation'"
          >
            <el-button
              :size="headerButtonSize"
              :type="headerButtonType"
              :icon="Cpu"
              @click="processSimulation"
              >Simulation</el-button
            >
          </el-tooltip>
        </el-button-group>
        <el-button-group key="align-control">
          <el-tooltip effect="light" content="Align Left">
            <el-button
              :size="headerButtonSize"
              class="align align-left"
              :icon="Histogram"
              @click="elementsAlign('left')"
            />
          </el-tooltip>
          <el-tooltip effect="light" content="Align Right">
            <el-button
              :size="headerButtonSize"
              class="align align-right"
              :icon="Histogram"
              @click="elementsAlign('right')"
            />
          </el-tooltip>
          <el-tooltip effect="light" content="Align Top">
            <el-button
              :size="headerButtonSize"
              class="align align-top"
              :icon="Histogram"
              @click="elementsAlign('top')"
            />
          </el-tooltip>
          <el-tooltip effect="light" content="Align Bottom">
            <el-button
              :size="headerButtonSize"
              class="align align-bottom"
              :icon="Histogram"
              @click="elementsAlign('bottom')"
            />
          </el-tooltip>
          <el-tooltip effect="light" content="Center Horizontally">
            <el-button
              :size="headerButtonSize"
              class="align align-center"
              :icon="Histogram"
              @click="elementsAlign('center')"
            />
          </el-tooltip>
          <el-tooltip effect="light" content="Center Vertically">
            <el-button
              :size="headerButtonSize"
              class="align align-middle"
              :icon="Histogram"
              @click="elementsAlign('middle')"
            />
          </el-tooltip>
        </el-button-group>
        <el-button-group key="scale-control">
          <el-tooltip effect="light" content="Zoom Out">
            <el-button
              :size="headerButtonSize"
              :disabled="defaultZoom <= 0.3"
              :icon="ZoomOut"
              @click="processZoomOut()"
            />
          </el-tooltip>
          <el-button :size="headerButtonSize">{{
            Math.floor(defaultZoom * 10 * 10) + '%'
          }}</el-button>
          <el-tooltip effect="light" content="Zoom In">
            <el-button
              :size="headerButtonSize"
              :disabled="defaultZoom >= 3.9"
              :icon="ZoomIn"
              @click="processZoomIn()"
            />
          </el-tooltip>
          <el-tooltip effect="light" content="Reset View and Center">
            <el-button :size="headerButtonSize" :icon="ScaleToOriginal" @click="processReZoom()" />
          </el-tooltip>
        </el-button-group>
        <el-button-group key="stack-control">
          <el-tooltip effect="light" content="Undo">
            <el-button
              :size="headerButtonSize"
              :disabled="!revocable"
              :icon="RefreshLeft"
              @click="processUndo()"
            />
          </el-tooltip>
          <el-tooltip effect="light" content="Redo">
            <el-button
              :size="headerButtonSize"
              :disabled="!recoverable"
              :icon="RefreshRight"
              @click="processRedo()"
            />
          </el-tooltip>
          <el-tooltip effect="light" content="Restart">
            <el-button :size="headerButtonSize" :icon="Refresh" @click="processRestart" />
          </el-tooltip>
        </el-button-group>
      </template>
      <!-- Used to open local files -->
      <input
        type="file"
        id="files"
        ref="refFile"
        style="display: none"
        accept=".xml, .bpmn"
        @change="importLocalFile"
      />
    </div>
    <div class="my-process-designer__container" v-show="diagramType == DiagramType.ORDINARY">
      <div class="my-process-designer__canvas" ref="bpmnCanvas"></div>
    </div>
    <el-dialog
      title="Preview"
      width="60%"
      v-model="previewModelVisible"
      append-to-body
      destroy-on-close
    >
      <highlightjs :language="previewType" :code="previewResult" style="height: 60vh" />
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import {
  Edit,
  FolderOpened,
  Download,
  View,
  Cpu,
  Histogram,
  ZoomOut,
  ZoomIn,
  ScaleToOriginal,
  RefreshLeft,
  RefreshRight,
  Refresh,
} from '@element-plus/icons-vue';
import BpmnModeler from 'bpmn-js/lib/Modeler';
// Simulate flow process
import tokenSimulation from 'bpmn-js-token-simulation';
// Import json conversion
import { xml2json } from 'xml-js';
import { ElMessageBox, ElMessage } from 'element-plus';
import { DiagramType } from '@/common/staticDict/flow';
import { ANY_OBJECT } from '@/types/generic';
// Code highlight
import highlightjs from './highlight';
// Translation method
import customTranslate from './plugins/translate/customTranslate';
import translationsCN from './plugins/translate/zh';
import DefaultEmptyXML from './plugins/defaultEmpty';
// Tag parsing builder
// import bpmnPropertiesProvider from "bpmn-js-properties-panel/lib/provider/bpmn";
// Tag parsing Moddle
import camundaModdleDescriptor from './plugins/descriptor/camundaDescriptor.json';
import activitiModdleDescriptor from './plugins/descriptor/activitiDescriptor.json';
import flowableModdleDescriptor from './plugins/descriptor/flowableDescriptor.json';
// Tag parsing Extension
import flowableModdleExtension from './plugins/extension-moddle/flowable/index.js';

const emit = defineEmits<{
  [key: string]: [ANY_OBJECT | string | null, (ANY_OBJECT | null)?, (ANY_OBJECT | null)?];
}>();

// props
const props = withDefaults(
  defineProps<{
    // xml string
    value: string;
    processId: string;
    processName: string;
    // Custom translation file
    translations?: ANY_OBJECT;
    // Custom model
    additionalModel?: ANY_OBJECT | ANY_OBJECT[];
    // Custom moddle
    moddleExtension?: ANY_OBJECT;
    flowEntryInfo: ANY_OBJECT;
    onlyCustomizeAddi?: boolean;
    onlyCustomizeModdle?: boolean;
    simulation?: boolean;
    keyboard?: boolean;
    prefix?: string;
    events?: Array<string>;
    headerButtonSize?: '' | 'default' | 'small' | 'large';
    headerButtonType?: 'default' | 'primary' | 'success' | 'warning' | 'danger' | 'info';
  }>(),
  {
    onlyCustomizeAddi: false,
    onlyCustomizeModdle: false,
    simulation: true,
    keyboard: true,
    prefix: 'flowable',
    events: () => ['element.click'],
    headerButtonSize: 'default',
    headerButtonType: 'primary',
  },
);
// refs
const refFile = ref();
const bpmnCanvas = ref();
const dingflowDesigner = ref();
// object
let bpmnModeler: ANY_OBJECT | undefined = undefined;
// data
const diagramType = ref(DiagramType.ORDINARY);
const defaultZoom = ref(1);
const previewModelVisible = ref(false);
const simulationStatus = ref(false);
const previewResult = ref('');
const previewType = ref('xml');
const recoverable = ref(false);
const revocable = ref(false);
const loaded = ref(false);
// computed
const additionalModules = computed(() => {
  const Modules: ANY_OBJECT[] = [];
  // Only keep user-defined extension modules
  if (props.onlyCustomizeAddi) {
    if (Array.isArray(props.additionalModel)) {
      return props.additionalModel || [];
    }
    return [props.additionalModel];
  }

  // Insert user-defined extension modules
  if (Array.isArray(props.additionalModel)) {
    Modules.push(...props.additionalModel);
  } else {
    props.additionalModel && Modules.push(props.additionalModel);
  }

  // Translation module
  const TranslateModule = {
    translate: ['value', customTranslate(props.translations || translationsCN)],
  };
  Modules.push(TranslateModule);

  // Flow simulation module
  if (props.simulation) {
    Modules.push(tokenSimulation);
  }

  // Set extension element construction module based on required process type
  if (props.prefix === 'camunda') {
    // TODO camundaModdleExtension
    //Modules.push(camundaModdleExtension);
  }
  if (props.prefix === 'flowable') {
    Modules.push(flowableModdleExtension);
  }
  if (props.prefix === 'activiti') {
    // TODO activitiModdleExtension
    //Modules.push(activitiModdleExtension);
  }

  return Modules;
});
const moddleExtensions = computed(() => {
  const Extensions: ANY_OBJECT = {};
  // Only use user-defined modules
  if (props.onlyCustomizeModdle) {
    return props.moddleExtension || null;
  }

  // Insert user-defined modules
  if (props.moddleExtension) {
    for (let key in props.moddleExtension) {
      Extensions[key] = props.moddleExtension[key];
    }
  }

  // Set the corresponding parsing file based on required "process type"
  if (props.prefix === 'activiti') {
    Extensions.activiti = activitiModdleDescriptor;
  }
  if (props.prefix === 'flowable') {
    Extensions.flowable = flowableModdleDescriptor;
  }
  if (props.prefix === 'camunda') {
    Extensions.camunda = camundaModdleDescriptor;
  }

  return Extensions;
});

// Set the connections of BPMN
const setBpmnConnect = (nodeConfig: ANY_OBJECT) => {
  console.log('>>>>>>>>>>>>> Setting BPMN Connections Begin');
  if (!nodeConfig || !bpmnModeler) return;
  const next = (nodeConfig: ANY_OBJECT, endElement?: ANY_OBJECT) => {
    const element = nodeConfig.endElement ? nodeConfig.endElement : nodeConfig.element;
    let y = nodeConfig.element.y;
    let lines = [...element.outgoing];

    if (!bpmnModeler) return;
    if (nodeConfig.conditionNodes) {
      console.log('nodeConfig >>>', nodeConfig);
      if (nodeConfig.endElement) {
        bpmnModeler.get('modeling').resizeShape(nodeConfig.endElement, {
          x: nodeConfig.element.x,
          y: nodeConfig.element.y,
          width: nodeConfig.endElement.width,
          height: nodeConfig.endElement.height,
        });
      }
      nodeConfig.conditionNodes.forEach((row: ANY_OBJECT, index: number) => {
        if (row.childNode && bpmnModeler) {
          const properties = {
            id: row.element.id,
            name: row.element.businessObject.name,
            extensionElements: row.extensionElements,
            conditionExpression: row.conditionExpression,
          };
          bpmnModeler
            .get('modeling')
            .removeElements([
              row.element,
              ...row.childNode.element.outgoing,
              ...row.childNode.element.incoming,
            ]);
          row.element = markRaw(
            bpmnModeler.get('modeling').connect(nodeConfig.element, row.childNode.element),
          );
          bpmnModeler.get('modeling').updateProperties(row.element, properties);
          bpmnModeler.get('modeling').resizeShape(row.childNode.element, {
            x: element.x + index * 150,
            y: y + 80,
            width: row.childNode.element.width,
            height: row.childNode.element.height,
          });
          row.childNode && next(row.childNode, nodeConfig.endElement);
        }
      });
    }

    if (nodeConfig.childNode) {
      bpmnModeler.get('modeling').resizeShape(nodeConfig.childNode.element, {
        x: element.x,
        y: element.y + 100,
        width: nodeConfig.childNode.element.width,
        height: nodeConfig.childNode.element.height,
      });
      lines = lines.concat([...nodeConfig.childNode.element.incoming]);
      bpmnModeler.get('modeling').connect(element, nodeConfig.childNode.element);
      next(nodeConfig.childNode, endElement);
    } else if (endElement) {
      if (endElement.y < element.y + 100) {
        bpmnModeler.get('modeling').resizeShape(endElement, {
          x: endElement.x,
          y: element.y + 120,
          width: endElement.width,
          height: endElement.height,
        });
      }
      bpmnModeler.get('modeling').connect(element, endElement);
    } else {
      const EndEvent = bpmnModeler.get('elementRegistry').find(el => el.type === 'bpmn:EndEvent');
      bpmnModeler.get('modeling').resizeShape(EndEvent, {
        x: element.x,
        y: element.y + 120,
        width: EndEvent.width,
        height: EndEvent.height,
      });
      bpmnModeler.get('modeling').connect(element, EndEvent);
    }

    bpmnModeler.get('modeling').removeElements(lines);
  };

  const StartEvent = bpmnModeler
    .get('elementRegistry')
    .find((el: ANY_OBJECT) => el.type === 'bpmn:StartEvent');
  const EndEvent = bpmnModeler
    .get('elementRegistry')
    .find((el: ANY_OBJECT) => el.type === 'bpmn:EndEvent');
  const lines = [
    ...StartEvent.outgoing,
    ...EndEvent.incoming,
    ...nodeConfig.element.outgoing,
    ...nodeConfig.element.incoming,
  ];
  bpmnModeler.get('modeling').removeElements(lines);
  bpmnModeler.get('modeling').connect(StartEvent, nodeConfig.element);
  next(nodeConfig);

  console.log('>>>>>>>>>>>>> Setting BPMN Connections End');
};
const setDiagramType = () => {
  if (!bpmnModeler) return;
  const process = bpmnModeler
    .get('elementRegistry')
    .find((el: ANY_OBJECT) => el.type === 'bpmn:Process');
  if (!process.businessObject.extensionElements) {
    const ExtensionElements = bpmnModeler.get('moddle').create('bpmn:ExtensionElements', {
      values: [
        bpmnModeler.get('moddle').create('flowable:Properties', {
          values: [
            bpmnModeler.get('moddle').create('flowable:Property', {
              name: '$OrangeDiagramType',
              value: diagramType.value,
            }),
          ],
        }),
      ],
    });

    bpmnModeler.get('modeling').updateProperties(process, {
      extensionElements: ExtensionElements,
    });
  } else {
    let hasDiagramType = false;
    let properties: ANY_OBJECT | null = null;
    process.businessObject.extensionElements.values.forEach((row: ANY_OBJECT) => {
      if (row.$type === 'flowable:Properties') {
        properties = row;
        row.values.forEach((item: ANY_OBJECT) => {
          if (item.name === '$OrangeDiagramType') {
            bpmnModeler?.get('modeling').updateModdleProperties(row, item, {
              value: diagramType.value,
            });
            hasDiagramType = true;
          }
        });
      }
    });
    if (!hasDiagramType) {
      bpmnModeler
        .get('modeling')
        .updateModdleProperties(process.businessObject.extensionElements, properties, {
          values: [
            ...(properties!.values || properties!.$children),
            bpmnModeler.get('moddle').create('flowable:Property', {
              name: '$OrangeDiagramType',
              value: diagramType.value,
            }),
          ],
        });
    }
  }
};

const findErrorNode = (nodeConfig: ANY_OBJECT): ANY_OBJECT | null => {
  let node: ANY_OBJECT | null = null;
  const next = (nodeConfig: ANY_OBJECT) => {
    console.log('findErrorNode', nodeConfig);
    if ((nodeConfig.type === 0 || nodeConfig.type === 1) && nodeConfig.nodeUserList.length === 0) {
      node = nodeConfig;
    }
    if (nodeConfig.childNode && !node) {
      next(nodeConfig.childNode);
    }
    if (nodeConfig.conditionNodes && !node) {
      nodeConfig.conditionNodes.forEach((row: ANY_OBJECT) => {
        next(row);
      });
    }
  };
  nodeConfig && next(nodeConfig);
  return node;
};
const onSave = () => {
  console.log('findErrorNode onSave');
  if (bpmnModeler == null) return;
  if (diagramType.value === DiagramType.DINGDING) {
    setBpmnConnect(dingflowDesigner.value?.getNodeConfig());
  }
  setDiagramType();
  console.log('findErrorNode onSave2', dingflowDesigner.value?.getNodeConfig());
  const errorNode = findErrorNode(dingflowDesigner.value?.getNodeConfig());
  if (errorNode) {
    ElMessageBox.confirm(errorNode.nodeName + ' task has not set a handler yet, do you want to save?', 'Prompt', {
      confirmButtonText: 'OK',
      cancelButtonText: 'Cancel',
      type: 'warning',
    })
      .then(() => {
        bpmnModeler?.saveXML({ format: true }).then(({ xml }: { xml: string }) => {
          emit('save', xml);
        });
      })
      .catch(() => {
        console.log('Cancelled saving');
      });
  } else {
    bpmnModeler.saveXML({ format: true }).then(({ xml }: { xml: string }) => {
      emit('save', xml);
    });
  }
};
const initBpmnModeler = () => {
  if (bpmnModeler) return;
  bpmnModeler = markRaw(
    new BpmnModeler({
      container: bpmnCanvas.value,
      keyboard: props.keyboard ? { bindTo: document } : null,
      additionalModules: additionalModules.value,
      moddleExtensions: moddleExtensions.value,
    }),
  );
  if (bpmnModeler) emit('init-finished', bpmnModeler);
  initModelListeners();
};
const initModelListeners = () => {
  if (!bpmnModeler) return;
  const EventBus = bpmnModeler.get('eventBus');
  // Register necessary listener events, replace . with - to avoid parsing exceptions
  props.events.forEach(event => {
    EventBus.on(event, (eventObj: ANY_OBJECT) => {
      console.log('>>>>>>>>>>>>> bpmnModeler EventBus on', event);
      let eventName: string = event.replace(/\./g, '-');
      let element: ANY_OBJECT | null = eventObj ? eventObj.element : null;
      // TODO dynamic events
      emit(eventName, element, eventObj);
      emit('event', eventName, element, eventObj);
    });
  });
  // Listen for changes in command stack to return xml
  EventBus.on('commandStack.changed', async (event: ANY_OBJECT) => {
    console.log('>>>>>>>>>>>>> bpmnModeler EventBus commandStack', event);
    if (!bpmnModeler) return;
    try {
      recoverable.value = bpmnModeler.get('commandStack').canRedo();
      revocable.value = bpmnModeler.get('commandStack').canUndo();
      let { xml } = await bpmnModeler.saveXML({ format: true });
      emit('commandStack-changed', event);
      emit('update:value', xml);
      emit('change', xml);
    } catch (e: any) {
      console.error(`[Process Designer Warn]: ${e.message || e}`);
    }
  });
  // Listen for changes in view zoom
  bpmnModeler.on('canvas.viewbox.changed', ({ viewbox }: { viewbox: ANY_OBJECT }) => {
    console.log('>>>>>>>>>>>>> bpmnModeler EventBus canvas.viewbox.changed', viewbox);
    emit('canvas-viewbox-changed', { viewbox });
    const { scale } = viewbox;
    defaultZoom.value = Math.floor(scale * 100) / 100;
  });
};
/* Create a new flowchart */
const createNewDiagram = async (xml: string | ArrayBuffer | null) => {
  // Convert the string into a diagram for display
  let newId = props.processId || `Process_${new Date().getTime()}`;
  let newName = props.processName || `Business Process_${new Date().getTime()}`;
  let flowType = props.flowEntryInfo?.flowType || 0;
  let xmlString = xml || DefaultEmptyXML(newId, newName, props.prefix, diagramType.value, flowType);
  try {
    if (bpmnModeler) {
      let { warnings } = await bpmnModeler.importXML(xmlString);
      if (warnings && warnings.length) {
        warnings.forEach((warn: ANY_OBJECT) => console.warn(warn));
      }
    }
  } catch (e: any) {
    console.error(`[Process Designer Warn]: ${e.message || e}`);
  }
};
// Encode as needed and return download address
const setEncoded = (type: string, filename = 'diagram', data: string | number | boolean = '') => {
  console.log('setEncoded', type, filename);
  const encodedData = encodeURIComponent(data);
  return {
    filename: `${filename}.${type}`,
    href: `data:application/${
      type === 'svg' ? 'text/xml' : 'bpmn20-xml'
    };charset=UTF-8,${encodedData}`,
    data: data,
  };
};
// Download flowchart to local
const downloadProcess = async (type: string, name: string | undefined = undefined) => {
  console.log('ProcessDesigner downloadProcess', type, name);
  try {
    if (bpmnModeler) {
      // Create file and download as necessary type
      if (type === 'xml' || type === 'bpmn') {
        const { err, xml } = await bpmnModeler.saveXML();
        // Throw exception when reading error
        if (err) {
          console.error(`[Process Designer Warn ]: ${err.message || err}`);
        }
        let { href, filename } = setEncoded(type.toUpperCase(), name, xml);
        downloadFunc(href, filename);
      } else {
        const { err, svg } = await bpmnModeler.saveSVG();
        // Throw exception when reading error
        if (err) {
          return console.error(err);
        }
        let { href, filename } = setEncoded('SVG', name, svg);
        downloadFunc(href, filename);
      }
    }
  } catch (e: any) {
    console.error(`[Process Designer Warn ]: ${e.message}`);
  }
  // File download method
  function downloadFunc(href: string, filename: string) {
    if (href && filename) {
      let a = document.createElement('a');
      a.download = filename; // Specify download file name
      a.href = href; // URL object
      a.click(); // Simulate click
      URL.revokeObjectURL(a.href); // Release URL object
    }
  }
};

// Load local files
const importLocalFile = () => {
  const file = refFile.value.files[0];
  const reader = new FileReader();
  reader.readAsText(file);
  reader.onload = () => {
    const xmlStr = reader.result as string;
    console.log('ProcessDesigner.importLocalFile result', xmlStr);
    createNewDiagram(xmlStr);
    if (
      diagramType.value === DiagramType.DINGDING &&
      xmlStr.indexOf('name="$OrangeDiagramType" value="1"') === -1
    ) {
      ElMessage.error('Format Error');
      return;
    }
    createNewDiagram(xmlStr).then(() => {
      diagramType.value === DiagramType.DINGDING && dingflowDesigner.value.refresh();
    });
  };
};

onMounted(() => {
  initBpmnModeler();
  console.log('ProcessDesigner diagramType before', diagramType.value);
  diagramType.value = props.flowEntryInfo?.diagramType || DiagramType.ORDINARY;
  createNewDiagram(props.value).then(() => {
    loaded.value = true;
  });
});

onBeforeUnmount(() => {
  if (bpmnModeler) {
    bpmnModeler.destroy();
    emit('destroy', bpmnModeler);
  }
  bpmnModeler = undefined;
});

const downloadProcessAsXml = () => {
  if (diagramType.value === DiagramType.DINGDING) {
    setDiagramType();
    setBpmnConnect(dingflowDesigner.value?.getNodeConfig());
  }
  downloadProcess('xml');
};
const downloadProcessAsBpmn = () => {
  downloadProcess('bpmn');
};
const downloadProcessAsSvg = () => {
  downloadProcess('svg');
};
const processSimulation = () => {
  simulationStatus.value = !simulationStatus.value;
  props.simulation && bpmnModeler && bpmnModeler.get('toggleMode').toggleMode();
};
const processRedo = () => {
  bpmnModeler && bpmnModeler.get('commandStack').redo();
};
const processUndo = () => {
  bpmnModeler && bpmnModeler.get('commandStack').undo();
};
const processZoomIn = (zoomStep = 0.1) => {
  let newZoom = Math.floor(defaultZoom.value * 100 + zoomStep * 100) / 100;
  if (newZoom > 4) {
    throw new Error('[Process Designer Warn ]: The zoom ratio cannot be greater than 4');
  }
  defaultZoom.value = newZoom;
  bpmnModeler && bpmnModeler.get('canvas').zoom(defaultZoom.value);
};
const processZoomOut = (zoomStep = 0.1) => {
  let newZoom = Math.floor(defaultZoom.value * 100 - zoomStep * 100) / 100;
  if (newZoom < 0.2) {
    throw new Error('[Process Designer Warn ]: The zoom ratio cannot be less than 0.2');
  }
  defaultZoom.value = newZoom;
  bpmnModeler && bpmnModeler.get('canvas').zoom(defaultZoom.value);
};
const processZoomTo = (newZoom = 1) => {
  if (newZoom < 0.2) {
    throw new Error('[Process Designer Warn ]: The zoom ratio cannot be less than 0.2');
  }
  if (newZoom > 4) {
    throw new Error('[Process Designer Warn ]: The zoom ratio cannot be greater than 4');
  }
  defaultZoom.value = newZoom;
  bpmnModeler && bpmnModeler.get('canvas').zoom(newZoom);
};
const processReZoom = () => {
  defaultZoom.value = 1;
  bpmnModeler && bpmnModeler.get('canvas').zoom('fit-viewport', 'auto');
};
const processRestart = () => {
  recoverable.value = false;
  revocable.value = false;
  createNewDiagram(null).then(() => {
    bpmnModeler && bpmnModeler.get('canvas').zoom(1, 'auto');
  });
};
const elementsAlign = (align: string) => {
  if (!bpmnModeler) return;
  const Align = bpmnModeler.get('alignElements');
  const Selection = bpmnModeler.get('selection');
  const SelectedElements = Selection.get();
  if (!SelectedElements || SelectedElements.length <= 1) {
    ElMessage.warning('Please hold down the Ctrl key to select multiple elements for alignment');
    return;
  }
  ElMessageBox.confirm('Automatic alignment may deform the graphics, would you like to continue?', 'Warning', {
    confirmButtonText: 'OK',
    cancelButtonText: 'Cancel',
    type: 'warning',
  }).then(() => Align.trigger(SelectedElements, align));
};

const previewProcessXML = () => {
  if (diagramType.value === DiagramType.DINGDING) {
    setDiagramType();
    setBpmnConnect(dingflowDesigner.value?.getNodeConfig());
  }
  bpmnModeler &&
    bpmnModeler.saveXML({ format: true }).then(({ xml }: { xml: string }) => {
      previewResult.value = xml;
      previewType.value = 'xml';
      previewModelVisible.value = true;
    });
};
const previewProcessJson = () => {
  bpmnModeler &&
    bpmnModeler.saveXML({ format: true }).then(({ xml }: { xml: string }) => {
      previewResult.value = xml2json(xml, { spaces: 2 });
      previewType.value = 'json';
      previewModelVisible.value = true;
    });
};
const openFile = () => {
  refFile.value.click();
};

defineExpose({
  onSave,
  processZoomTo,
});
</script>

<style scoped>
.popper-box .el-button {
  display: block;
}
.popper-box .el-button + .el-button {
  margin-top: 8px;
  margin-left: 0px;
  margin-right: 0px;
}
</style>
