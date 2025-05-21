<template>
  <div style="padding: 20px">
    <!-- 统计卡片 -->
    <el-row :gutter="24">
      <el-col :span="12">
        <el-card style="margin-bottom: 20px">
          <div style="font-size: 18px; padding: 10px">
            <i class="el-icon-user"></i>
            <span>用户数量: {{ userCount }}</span>
          </div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card style="margin-bottom: 20px">
          <div style="font-size: 18px; padding: 10px">
            <i class="el-icon-picture-outline"></i>
            <span>作品数量: {{ creationCount }}</span>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区域 -->
    <el-row :gutter="24">
      <!-- 饼图 -->
      <el-col :xs="24" :sm="12" :md="8" style="margin-bottom: 20px">
        <el-card>
          <div id="main1" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>

      <!-- 积分排行 -->
      <el-col :xs="24" :sm="12" :md="8" style="margin-bottom: 20px">
        <el-card>
          <div id="main2" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>

      <!-- 下载排行 -->
      <el-col :xs="24" :sm="24" :md="8" style="margin-bottom: 20px">
        <el-card>
          <div id="main3" style="width: 100%; height: 400px"></div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script>

import * as echarts from 'echarts';

export default {
  name: "Home",
  data() {
    return {
      userCount:0,
      creationCount:0,
    };
  },
  created() {
    this.request.get('/user').then(res=>{
      this.userCount=res.data.filter(user=>user.role==='ROLE_USER').length
    })
    this.request.get('/creation').then(res=>{
      this.creationCount=res.data.length
    })
  },
  mounted() {

    const chartDom1 = document.getElementById('main1');
    const myChart1 = echarts.init(chartDom1);

    let option1;

    option1 = {
      title: {
        text: '不同分类作品数量分布',
        left: 'center',
        top: '20px', // 标题下移
        textStyle: {
          fontSize: 16,
          color: '#333'
        }
      },
      tooltip: {
        trigger: 'item',
        formatter: '{b}: {c} ({d}%)' // 显示百分比
      },
      legend: {
        orient: 'vertical',
        left: 'left',
        top: '50px', // 图例下移
        textStyle: {
          color: '#666'
        }
      },
      color: [ // 马卡龙色系配色
        '#5470C6',
        '#91CC75',
        '#FAC858',
        '#EE6666',
        '#73C0DE',
        '#3BA272',
        '#FC8452',
        '#9A60B4',
        '#EA7CCC'
      ],
      series: [{
        name: '作品数量',
        type: 'pie',
        radius: ['40%', '65%'], // 空心饼图
        center: ['50%', '60%'], // 整体下移
        avoidLabelOverlap: true,
        label: {
          show: true,
          formatter: '{d}%',
          fontSize: 14
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16
          }
        },
        data: []
      }]
    };

    this.request.get("/creation/count1").then(res=>{
      res.data.forEach(item=>{
        option1.series[0].data.push(item)
      })
      option1 && myChart1.setOption(option1);
    })


    const chartDom2 = document.getElementById('main2');
    const myChart2 = echarts.init(chartDom2);
    let option2;

    option2 = {
      title: {
        text: '用户积分排行top5柱状图',
        subtext: '柱状图',
        left: 'center'
      },
      tooltip: {
        trigger: 'item'
      },
      legend: {
        orient: 'vertical',
        left: 'left'
      },
      xAxis: {
        type: 'category',
        data: [],
        axisLabel: {
          interval: 0, // 或者设置为大于1的整数
        }
      },
      yAxis: {
        type: 'value'
      },
      series: [
        {
          data: [],
          type: 'bar',
          itemStyle: {
            normal: {
              color: function (params) {
                const colorList = [
                  '#5470C6', '#91CC75', '#FAC858', '#EE6666', '#73C0DE'
                ];
                return colorList[params.dataIndex]
              },
            }
          }
        },
      ]
    };

    this.request.get("/creation/count2").then(res => {
      option2.xAxis.data = res.data.map(v => v.name)
      option2.series[0].data = res.data.map(v => v.value)
      myChart2.setOption(option2);
    })

    const chartDom3 = document.getElementById('main3');
    const myChart3 = echarts.init(chartDom3);

    let option3 = {
      title: {
        text: '热门作者被下载次数排行',
        left: 'center'
      },
      tooltip: {
        trigger: 'axis'
      },
      xAxis: {
        type: 'category',
        data: [],
        axisLabel: {
          rotate: 45 // 防止名称过长重叠
        }
      },
      yAxis: {
        type: 'value'
      },

      series: [{
        data: [],
        type: 'bar',
        itemStyle: {
          color: '#5470C6'
        }
      }]
    };

    this.request.get("/sys-log/downloadStats").then(res => {
      const data = res.data.slice(0, 10); // 取Top10
      option3.xAxis.data = data.map(v => v.name);
      option3.series[0].data = data.map(v => v.value);
      myChart3.setOption(option3);
    });


  },

};
</script>

<style scoped>
@media (max-width: 768px) {
  .el-col {
    margin-bottom: 15px !important;
  }

  #main1, #main2, #main3 {
    height: 300px !important;
  }
}

</style>

