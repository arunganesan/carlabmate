class Information {
  name: string;
  dtype: any;

  constructor(name: string, dtype: any) {
    this.name = name;
    this.dtype = dtype;
  }
}

export class DataMarshal {
  info: Information;
  value: any;

  constructor(info: Information, value: any) {
    this.info = info;
    this.value = value;
  }

  toJson() {
    return JSON.stringify({
      info: this.info,
      value: this.value
    });
  }
}

class Registry {
  static WorldAlignedAccel = new Information("world-aligned-accel", 0);
  static WorldAlignedGyro = new Information("world-aligned-gyro", 0);
  static CarModel = new Information("car-model", "");
}

export class Libcarlab {
  lastCheckTime: number;
  userid: string;
  requiredInfo: Information[];
  baseUrl: string;
  pushUrl: (arg: string) => string;
  fetchUrl: (arg: string) => string;

  constructor(userid: string, requiredInfo: Information[]) {
    this.lastCheckTime = Math.round(new Date().getTime() / 1000);
    this.userid = userid;
    this.requiredInfo = requiredInfo;

    this.baseUrl = "http://localhost:1234/packet/";
    this.fetchUrl = info =>
      `${this.baseUrl}list?information=${info}&person=${this.userid}&sincetime=${this.lastCheckTime}`;
    this.pushUrl = info =>
      `${this.baseUrl}upload?information=${info}&person=${this.userid}`;
  }


  async checkNewInfo(callback: Function) {
    // Get new data
    // check with local storage first to only get relevant data
    for (let info of this.requiredInfo)
      fetch(this.fetchUrl(info.name))
        .then(res => res.json())
        .then(data => callback(info, data));

    this.lastCheckTime = Math.round(new Date().getTime() / 1000);
  }

  outputNewInfo(dm: DataMarshal) {
      // XXX this should fail
    fetch(this.pushUrl(dm.info.name), {
      method: "post",
      mode: "cors",
      cache: "no-cache",
      headers: { "Content-type": "application/json" },
      body: dm.toJson()
    }).then(res => {
      // Success
      // await this.storageHandler.clearData(info);
    });
  }
}