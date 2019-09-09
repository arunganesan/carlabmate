export class Libcarlab {
  constructor (userid, required_info, test) {
    this.last_check_time = Math.round(new Date().getTime() / 1000)
    this.userid = userid
    this.test = test
    this.required_info = required_info

    this.baseurl = 'http://localhost:1234/packet/'
    this.fetch_url = (info) => `${this.baseurl}list?information=${info}&person=${this.userid}&sincetime=${this.last_check_time}`
    this.push_url = (info) => `${this.baseurl}upload?information=${info}&person=${this.userid}`
  }
  
  checkNewInfo (callback) {
    // Get new data
    // check with local storage first to only get relevant data
    for (let info of this.required_info) 
        fetch(this.fetch_url(info))
            .then(res => res.json())
            .then(data => callback(info, data));
        
      
    this.last_check_time = Math.round(new Date().getTime() / 1000)
  }

  outputNewInfo (info, message, callback) {
    console.log('Sending to ', this.push_url(info), 'value', info, message);
    fetch(this.push_url(info), {
        method: 'post', mode: 'cors', cache: 'no-cache',
        headers: { 'Content-type': 'application/json' },
        body: JSON.stringify({'message': message })})
        .then(res => callback(res))
  }
}