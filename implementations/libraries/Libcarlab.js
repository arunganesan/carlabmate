export class Libcarlab {
  constructor (userid, requiredInfo, test, storageHandler) {
    this.lastCheckTime = Math.round(new Date().getTime() / 1000)
    this.userid = userid
    this.test = test
    this.requiredInfo = requiredInfo

    this.localStorage = {}

    this.baseurl = 'http://localhost:1234/packet/'
    this.fetchUrl = (info) => `${this.baseurl}list?information=${info}&person=${this.userid}&sincetime=${this.lastCheckTime}`
    this.pushUrl = (info) => `${this.baseurl}upload?information=${info}&person=${this.userid}`

    this.uploadTimer = false;
    this.uploadEvery = 30*1000; // 30 seconds
    this.storageHandler = storageHandler
  }
  
  checkNewInfo (callback) {
    // Get new data
    // check with local storage first to only get relevant data
    for (let info of this.requiredInfo) 
        fetch(this.fetchUrl(info))
            .then(res => res.json())
            .then(data => callback(info, data));
    
    // XXX This should only be updated IF we successfully made a call. Otherwise info will be lost
    // This could also be the time of the last data we received -- that might be better to avoid time-off-sync-related errors
    this.lastCheckTime = Math.round(new Date().getTime() / 1000)
  }

  outputNewInfo (info, message) {
    console.log('Sending to ', this.pushUrl(info), 'value', info, message);
    
    if (!this.localStorage.hasOwnProperty(info))
      this.localStorage[info] = []
    this.localStorage[info].push(message)
  }

  
  scheduleUploads () {
    if (this.uploadTimer)
      return
    
    this.uploadTimer = setInterval(() => this.performUpload, this.uploadEvery)
  }

  async performUpload () {
    if (Object.keys(this.localStorage).length === 0)
    return;
    
    // Save everything to disk (in case app crashes)
    for (let key in this.localStorage) 
      if (this.localStorage.hasOwnProperty(key)) {
        await this.storageHandler.saveOrUpdateData(key, this.localStorage[key]);
        delete this.localStorage[key];
      }
    

    // Read from disk
    let allOutput = await this.storageHandler.loadAllData()
      
    // Upload each information
    for (let info in allOutput) {
      if (allOutput.hasOwnProperty(info)) {
        fetch(this.push_url(info), {
          method: 'post', mode: 'cors', cache: 'no-cache',
          headers: { 'Content-type': 'application/json' },
          body: JSON.stringify({'message': allOutput[info] })})
          .then(res => {
            // Success
            await this.storageHandler.clearData(info);
          })
      }
    }
  }

  unscheduleUploads () {
    if (this.uploadTimer) {
      clearInterval(this.uploadTimer)
      this.uploadTimer = false;
    }
  }
}