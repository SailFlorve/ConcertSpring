<template>
  <p>
    <a-space>
      <a-date-picker v-model:value="params.date" :disabled-date="disabledDate" placeholder="请选择日期"
                     valueFormat="YYYY-MM-DD"></a-date-picker>
      <station-select-view v-model="params.start" hidden="hidden" width="200px"></station-select-view>
      <station-select-view v-model="params.end" hidden="hidden" width="200px"></station-select-view>
      <a-button type="primary" @click="handleQuery()">查找</a-button>
    </a-space>
  </p>
  <a-table :columns="columns"
           :dataSource="dailyTrainTickets"
           :loading="loading"
           :pagination="pagination"
           @change="handleTableChange">
    <template #bodyCell="{ column, record }">
      <template v-if="column.dataIndex === 'operation'">
        <a-space>
          <a-button :disabled="isExpire(record)" type="primary" @click="toOrder(record)">
            {{ isExpire(record) ? "过期" : "预订" }}
          </a-button>
          <router-link :to="{
            path: '/seat',
            query: {
              date: record.date,
              trainCode: record.trainCode,
              start: record.start,
              startIndex: record.startIndex,
              end: record.end,
              endIndex: record.endIndex
            }
          }">
          </router-link>
        </a-space>
      </template>
    </template>
  </a-table>

  <!-- 途经车站 -->
  <a-modal v-model:visible="visible" :closable="false" :footer="null" :title="null" style="top: 30px">
    <a-table :data-source="stations" :pagination="false">
      <a-table-column key="index" data-index="index" title="站序"/>
      <a-table-column key="name" data-index="name" title="站名"/>
      <a-table-column key="inTime" data-index="inTime" title="进站时间">
        <template #default="{ record }">
          {{ record.index === 0 ? '-' : record.inTime }}
        </template>
      </a-table-column>
      <a-table-column key="outTime" data-index="outTime" title="出站时间">
        <template #default="{ record }">
          {{ record.index === (stations.length - 1) ? '-' : record.outTime }}
        </template>
      </a-table-column>
      <a-table-column key="stopTime" data-index="stopTime" title="停站时长">
        <template #default="{ record }">
          {{ record.index === 0 || record.index === (stations.length - 1) ? '-' : record.stopTime }}
        </template>
      </a-table-column>
    </a-table>
  </a-modal>
</template>

<script>
import {defineComponent, onMounted, ref} from 'vue';
import {notification} from "ant-design-vue";
import axios from "axios";
import StationSelectView from "@/components/station-select";
import dayjs from "dayjs";
import router from "@/router";

export default defineComponent({
  name: "ticket-view",
  components: {StationSelectView},
  setup() {
    const visible = ref(false);
    let dailyTrainTicket = ref({
      id: undefined,
      title: undefined,
      performer: undefined,
      venue: undefined,
      concertDate: undefined,
      startTime: undefined,
      endTime: undefined,
      tickerLeft: undefined,
    });
    const dailyTrainTickets = ref([]);
    // 分页的三个属性名是固定的
    const pagination = ref({
      total: 0,
      current: 1,
      pageSize: 10,
    });
    let loading = ref(false);
    const params = ref({});
    const columns = [
      {
        title: '演唱会名称',
        dataIndex: 'title',
        key: 'title',
      },
      {
        title: '主唱',
        dataIndex: 'performer',
      },
      {
        title: '地点',
        dataIndex: 'venue',
      },
      {
        title: '日期',
        dataIndex: 'concertDate',
        customRender: ({text}) => {
          return dayjs(text).format('YYYY-MM-DD');
        },
      },
      {
        title: '开始时间',
        dataIndex: 'startTime',
        key: 'startTime',
        customRender: ({text}) => {
          return dayjs(text).format('HH:mm');
        },
      },
      {
        title: '结束时间',
        dataIndex: 'endTime',
        key: 'endTime',
        customRender: ({text}) => {
          return dayjs(text).format('HH:mm');
        },
      },
      {
        title: '余票',
        dataIndex: 'ticketLeft',
        key: 'ticketLeft',
      },
      {
        title: '操作',
        dataIndex: 'operation'
      },
    ];


    const handleQuery = (param) => {
      // if (Tool.isEmpty(params.value.date)) {
      //   notification.error({description: "请输入日期"});
      //   return;
      // }
      // if (Tool.isEmpty(params.value.start)) {
      //   notification.error({description: "请输入出发地"});
      //   return;
      // }
      // if (Tool.isEmpty(params.value.end)) {
      //   notification.error({description: "请输入目的地"});
      //   return;
      // }
      if (!param) {
        param = {
          page: 1,
          size: pagination.value.pageSize
        };
      }

      // 保存查询参数
      SessionStorage.set(SESSION_TICKET_PARAMS, params.value);

      loading.value = true;
      axios.get("/business/concert/query-list", {
        params: {
          page: param.page,
          size: param.size,
          trainCode: params.value.trainCode,
          date: params.value.date,
          start: params.value.start,
          end: params.value.end
        }
      }).then((response) => {
        loading.value = false;
        let data = response.data;
        if (data.success) {
          dailyTrainTickets.value = data.content;
          console.log("看看查询到的演唱会列表：" + JSON.stringify(dailyTrainTickets.value));
          // 设置分页控件的值
          pagination.value.current = param.page;
          pagination.value.total = data.content.total;
        } else {
          notification.error({description: data.message});
        }
      });
    };

    const handleTableChange = (page) => {
      // console.log("看看自带的分页参数都有啥：" + JSON.stringify(page));
      pagination.value.pageSize = page.pageSize;
      handleQuery({
        page: page.current,
        size: page.pageSize
      });
    };

    const calDuration = (startTime, endTime) => {
      let diff = dayjs(endTime, 'HH:mm:ss').diff(dayjs(startTime, 'HH:mm:ss'), 'seconds');
      return dayjs('00:00:00', 'HH:mm:ss').second(diff).format('HH:mm:ss');
    };

    const toOrder = (record) => {
      dailyTrainTicket.value = Tool.copy(record);
      SessionStorage.set(SESSION_ORDER, dailyTrainTicket.value);
      router.push("/order")
    };

    // ---------------------- 途经车站 ----------------------
    const stations = ref([]);
    const showStation = record => {
      visible.value = true;
      axios.get("/business/daily-train-station/query-by-train-code", {
        params: {
          date: record.date,
          trainCode: record.trainCode
        }
      }).then((response) => {
        let data = response.data;
        if (data.success) {
          stations.value = data.content;
        } else {
          notification.error({description: data.message});
        }
      });
    };

    // 不能选择今天以前及两周以后的日期
    const disabledDate = current => {
      return current && (current <= dayjs().add(-1, 'day') || current > dayjs().add(14, 'day'));
    };

    // 判断是否过期
    const isExpire = (record) => {

      return record.date < dayjs().format('YYYY-MM-DD');
    };

    onMounted(() => {
      handleQuery(
          {
            page: 1,
            size: pagination.value.pageSize
          }
      )
      //  "|| {}"是常用技巧，可以避免空指针异常
      params.value = SessionStorage.get(SESSION_TICKET_PARAMS) || {};
      if (Tool.isNotEmpty(params.value)) {
        // handleQuery({
        //   page: 1,
        //   size: pagination.value.pageSize
        // });
      }
    });

    return {
      dailyTrainTicket,
      visible,
      dailyTrainTickets,
      pagination,
      columns,
      handleTableChange,
      handleQuery,
      loading,
      params,
      calDuration,
      toOrder,
      showStation,
      stations,
      disabledDate,
      isExpire
    };
  },
});
</script>
