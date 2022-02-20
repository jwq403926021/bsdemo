<template>
  <div>
    <div class="title">
      <p>无论是专业版还是企业版的脚手架，我们全部开源免费，这一切都由<span> 橙单 </span>为您生成。</p>
    </div>
    <el-row type="flex">
      <div style="width: 100%;">
        <el-card class="box-card" shadow="never" :body-style="{padding: '0px 20px'}">
          <el-collapse v-model="currentItem">
            <el-collapse-item title="选择我们的 5 个理由" name="1">
              <ul class="item-list">
                <li>是真正的生成器，而非脚手架。</li>
                <li>可生成卓越的架构和高质量的代码。</li>
                <li>生成后工程代码完全可读，包名和作者署名都由您钦定。</li>
                <li>授权极为宽松，生成后代码版权完全归属于您。</li>
                <li>详尽、完善、免费、护眼的开发文档。</li>
              </ul>
            </el-collapse-item>
            <el-collapse-item title="我们的技术选型" name="2">
              <ul class="item-list">
                <li>Element (Vue) / Ant Design (React) / ECharts / AntV / Axios / Webpack</li>
                <li>Spring Boot / Spring Cloud / Spring Cloud Alibaba + Mybatis + Jwt</li>
                <li>Hutool + Guava + Caffeine + Lombok + MapStruct + Flowable + Activiti</li>
                <li>Redis + Zookeeper + Nacos + Consul + XXL-Job + Quartz + Kafka + RocketMQ + Seata</li>
                <li>ELK + PinPoint / SkyWalking + Grafana + Prometheus</li>
              </ul>
            </el-collapse-item>
            <el-collapse-item title="可快速上手的高质量代码" name="3">
              <ul class="item-list">
                <li>完整的工作流支持。</li>
                <li>完整的在线表单支持。</li>
                <li>完整的静态表单支持。</li>
                <li>完整的多关联数据组装支持。</li>
                <li>完整的中台化核心组件支持。</li>
              </ul>
            </el-collapse-item>
            <el-collapse-item title="设计理念和未来目标" name="4">
              <ul class="item-list">
                <li>全面且实用的日志跟踪、链路跟踪和服务监控体系。</li>
                <li>前沿的单表组合式设计，使系统拆分SO EASY。</li>
                <li>先代码，后SQL的原则，让服务扩充更具弹性。</li>
                <li>统一的数据组装接口，更高效的满足微服务的拆分与再合并。</li>
                <li>支持实时与离线的数据同步，更好的拥抱云原生架构。</li>
              </ul>
            </el-collapse-item>
          </el-collapse>
        </el-card>
      </div>
      <el-card class="box-card" style="min-width: 450px; margin-left: 20px;" shadow="never" :body-style="{padding: '0px 20px'}">
        <div class="item">
          <span style="width: 120px;">产品名称</span>
          <el-tag effect="dark">橙单代码生成器</el-tag>
        </div>
        <el-divider></el-divider>
        <div class="item">
          <span style="width: 120px;">网站首页</span>
          <a href="http://www.orangeforms.com/" target="_blank">http://www.orangeforms.com/</a>
        </div>
        <el-divider></el-divider>
        <div class="item">
          <span style="width: 120px;">教学视频</span>
          <a href="https://www.bilibili.com/video/BV1Wg4y1i7vP" target="_blank">https://www.bilibili.com/video/BV1Wg4y1i7vP</a>
        </div>
        <el-divider></el-divider>
        <div class="item">
          <span style="width: 120px;">更多演示</span>
          <a href="http://demo.orangeforms.com/" target="_blank">http://demo.orangeforms.com/</a>
        </div>
        <el-divider></el-divider>
        <div class="item">
          <span style="width: 120px;">操作指南</span>
          <a href="http://www.orangeforms.com/orange-doc/" target="_blank">http://www.orangeforms.com/orange-doc/</a>
        </div>
        <el-divider></el-divider>
        <div class="item">
          <span style="width: 120px;">开发文档</span>
          <a href="http://www.orangeforms.com/development-doc/" target="_blank">http://www.orangeforms.com/development-doc/</a>
        </div>
        <el-divider></el-divider>
        <div class="item">
          <span style="width: 120px;">开源代码</span>
          <a href="https://gitee.com/orangeform/orange-admin" target="_blank">https://gitee.com/orangeform/orange-admin</a>
        </div>
        <el-divider></el-divider>
        <div class="item">
          <span style="width: 120px;">联系方式</span>
          <span>
            QQ群
            <a target="_blank" style="margin-left: 5px;"
            href="https://qm.qq.com/cgi-bin/qm/qr?k=cMMom9SdX1j57T_58WihLZ4TBrBpokrv&jump_from=webapi">
            883176267
            </a>
          </span>
        </div>
        <el-divider></el-divider>
        <div style="width: 100%; text-align: center; padding: 10px;">
          <img src="../../assets/img/orange-group2.png" />
        </div>
      </el-card>
    </el-row>
  </div>
</template>

<script>
export default {
  data () {
    return {
      currentItem: ['1', '2', '3', '4']
    }
  }
}
</script>

<style scoped>
  >>> .el-collapse {
    border: none;
  }

  >>> .el-divider--horizontal {
    margin: 0px;
  }
  >>> .el-tag {
    padding: 0px 30px;
  }
</style>

<style lang="scss" scoped>
  @import '@/assets/style/element-variables.scss';
  .title {
    border: 1px solid #EDEDED;
    border-left-width: 5px;
    border-left-color: $--color-primary;
    margin-bottom: 20px;
  }

  .title p {
    height: 40px;
    line-height: 40px;
    font-size: 16px;
    margin: 0px;
    padding-left: 20px;
  }

  .title p span {
    font-size: 20px;
    color: $--color-primary;
  }

  .item-list {
    margin: 0px;
  }
  .item-list li {
    margin: 10px 0px;
  }

  .item {
    height: 48px;
    display: flex;
    align-items: center;
  }
</style>
