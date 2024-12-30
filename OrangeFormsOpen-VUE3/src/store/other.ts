import { defineStore } from 'pinia';
import { ANY_OBJECT } from '@/types/generic';

export default defineStore('other', {
  state: () => {
    return {
      userShowNameData: {
        '${startUserName}': 'Process Initiator',
        '${appointedAssignee}': 'Appointed Approver',
      } as ANY_OBJECT,
    };
  },
  getters: {
    getUserShowNameData(): ANY_OBJECT {
      return this.userShowNameData;
    },
  },
  actions: {
    setUserShowNameData(data: ANY_OBJECT) {
      this.userShowNameData = data;
    },
  },
});
