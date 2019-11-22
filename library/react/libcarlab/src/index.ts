import { Information, Registry } from './registry'
export {Information, Registry};

export class DataMarshal {
  info: Information;
  message: any;

  constructor(info: Information, value: any) {
    this.info = info;
    this.message = value;
  }

  toJson() {
    return JSON.stringify({
      info: this.info,
      message: this.message
    });
  }
}

export class Libcarlab {
  lastCheckTime: number;
  session: string | null;
  requiredInfo: Information[];
  baseUrl: string;
  portno: number;
  pushUrl: (arg: string) => string;
  fetchUrl: (arg: string) => string;
  registerUrl: (arg: string) => string;
  latestUrl: (arg: string) => string;
  

  constructor(session: string | null, requiredInfo: Information[]) {
    this.lastCheckTime = Math.round(new Date().getTime() / 1000);
    this.session = session;
    this.requiredInfo = requiredInfo;
    this.portno = 8080; // port of the linking server

    this.baseUrl = `http://localhost:${this.portno}/packet/`;
    this.fetchUrl = info =>
      `${this.baseUrl}list?information=${info}&session=${this.session}&sincetime=${this.lastCheckTime}`;

    this.latestUrl = info =>
      `${this.baseUrl}latest?information=${info}&session=${this.session}`;
    this.pushUrl = info =>
      `${this.baseUrl}upload?information=${info}&session=${this.session}`;


    // This goes to the texting server
    this.registerUrl = phoneno => `http://localhost:1234/texting/register_phone?session=${this.session}&number=${phoneno}&serverport=${this.portno}`;
  }


  async checkLatestInfo(callback: Function) {
    // Get new data
    // check with local storage first to only get relevant data
    for (let info of this.requiredInfo)
      fetch(this.latestUrl(info.name))
        .then(res => res.json())
        .then(data => callback(info, data));

    this.lastCheckTime = Math.round(new Date().getTime() / 1000);
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

  outputNewInfo(dm: DataMarshal, callback: Function) {
    if (dm.info == Registry.PhoneNumber) {
      this.registerPhoneNumber(dm.message);
    }


    console.log('Push url is: ', this.pushUrl(dm.info.name))
    fetch(this.pushUrl(dm.info.name), {
      method: "post",
      mode: "cors",
      cache: "no-cache",
      headers: { "Content-type": "application/json" },
      body: dm.toJson()
    }).then(res => callback(res));
  }


  registerPhoneNumber (phoneno: string) {
    fetch(this.registerUrl(phoneno), {
      method: 'post',
      mode: 'cors',
      cache: 'no-cache',
    });
  }
}
