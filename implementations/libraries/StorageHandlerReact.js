export class StorageHandlerReact {
    constructor (requiredInfo) {
        this.requiredInfo = requiredInfo;
    }

    async saveOrUpdateData(info, message) {
        let dataStr = window.localStorage.getItem(info);
        let data = (dataStr !== null) ? [] : JSON.parse(dataStr);
        data.push(message)
        window.localStorage.setItem(info, JSON.stringify(data))
    }

    async loadAllData() {
        let allData = {}
        for (let info of this.requiredInfo) {
            allData[info] = window.localStorage.getItem(info)
        }
        
        return allData;
    }
  
    async clearData (info) {
        window.localStorage.removeItem(info);
    }
}