import {AsyncStorage} from 'react-native';

export class StorageHandlerReact {
    constructor (requiredInfo) {
        this.requiredInfo = requiredInfo;
    }

    async saveOrUpdateData(info, message) {
        let dataStr = await AsyncStorage.getItem(info);
        let data = (dataStr !== null) ? [] : JSON.parse(dataStr);
        data.push(message)
        await AsyncStorage.setItem(info, JSON.stringify(data))
    }


    async loadAllData() {
        let allData = {}
        for (let info of this.requiredInfo) {
            allData[info] = await AsyncStorage.getItem(info);
        }
        return allData;
    }

    async clearData (info) {
        await AsyncStorage.removeItem(info);
    }
}