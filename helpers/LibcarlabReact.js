class Information {
  constructor(name, dtype) {
    this.name = name
    this.dtype = dtype
  }
}

export class DataMarshal {
  constructor(info, value) {
    this.info = info
    this.value = value
  }

  toJson() {
    return JSON.stringify({
      info: this.info,
      value: this.value
    })
  }
}

class Registry {
  static WorldAlignedAccel = new Information('world-aligned-accel', 0)
  static WorldAlignedGyro = new Information('world-aligned-gyro', 0)  
  static CarModel = new Information('car-model', '')
}

export class Libcarlab {
  constructor(userid, requiredInfo, test, storageHandler) {
    this.lastCheckTime = Math.round(new Date().getTime() / 1000);
    this.userid = userid;
    this.test = test;
    this.requiredInfo = requiredInfo;

    this.localStorage = {};

    this.baseurl = "http://localhost:1234/packet/";
    this.fetchUrl = info =>
      `${this.baseurl}list?information=${info}&person=${this.userid}&sincetime=${this.lastCheckTime}`;
    this.pushUrl = info =>
      `${this.baseurl}upload?information=${info}&person=${this.userid}`;

    this.uploadEvery = 1000; // 30 seconds
    this.storageHandler = storageHandler;
  }

  async checkNewInfo(callback) {
    // Get new data
    // check with local storage first to only get relevant data
    for (let info of this.requiredInfo)
      fetch(this.fetchUrl(info))
        .then(res => res.json())
        .then(data => callback(info, data));

    this.lastCheckTime = Math.round(new Date().getTime() / 1000);
  }

  outputNewInfo(datamarshal: DataMarshal) {
    fetch(this.push_url(info), {
      method: "post",
      mode: "cors",
      cache: "no-cache",
      headers: { "Content-type": "application/json" },
      body: datamarshal.toJson()
    }).then(res => {
      // Success
      // await this.storageHandler.clearData(info);
      this.storageHandler.clearData(info);
    });
  }

  scheduleUploads() {
    if (this.uploadTimer) return;
    this.uploadTimer = setInterval(() => this.performUpload, this.uploadEvery);
  }

  async performUpload() {
    if (Object.keys(this.localStorage).length === 0) return;

    // Save everything to disk (in case app crashes)
    for (let key in this.localStorage)
      if (this.localStorage.hasOwnProperty(key)) {
        await this.storageHandler.saveOrUpdateData(key, this.localStorage[key]);
        delete this.localStorage[key];
      }

    // Read from disk
    let allOutput = await this.storageHandler.loadAllData();

    // Upload each information
    for (let info in allOutput) {
      if (allOutput.hasOwnProperty(info)) {
        fetch(this.push_url(info), {
          method: "post",
          mode: "cors",
          cache: "no-cache",
          headers: { "Content-type": "application/json" },
          body: JSON.stringify({ message: allOutput[info] })
        }).then(res => {
          // Success
          // await this.storageHandler.clearData(info);
          this.storageHandler.clearData(info);
        });
      }
    }
  }

  unscheduleUploads() {
    if (this.uploadTimer) {
      clearInterval(this.uploadTimer);
      this.uploadTimer = false;
    }
  }
}

export class StorageHandler {
  constructor(requiredInfo) {
    this.requiredInfo = requiredInfo;
  }

  async saveOrUpdateData(info, message) {
    let dataStr = window.localStorage.getItem(info);
    let data = dataStr !== null ? [] : JSON.parse(dataStr);
    data.push(message);
    window.localStorage.setItem(info, JSON.stringify(data));
  }

  async loadAllData() {
    let allData = {};
    for (let info of this.requiredInfo) {
      allData[info] = window.localStorage.getItem(info);
    }

    return allData;
  }

  async clearData(info) {
    window.localStorage.removeItem(info);
  }
}
