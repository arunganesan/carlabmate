export class Libcarlab {
  constructor (userid, required_info, test) {
    this.last_check_time = 0
    this.userid = userid
    this.test = test
    this.required_info = required_info

    this.baseurl = 'http://localhost:1234'
    this.fetch_url = (info) => `${this.baseurl}?information=${info}&person=${this.userid}&sincetime=${this.last_check_time}`
    this.push_url = (info, message) => `${this.baseurl}?information=${info}&person=${this.userid}&message=${message}`
  }
  
  checkNewInfo (callback) {
    // Get new data
    // check with local storage first to only get relevant data
    for (let info of this.required_info) 
        fetch(this.fetch_url(info))
            .then(res => res.json())
            .then(data => callback(info, data));
  }

  outputNewInfo (info, message, callback) {
    fetch(this.push_url(info, message), {
        method: 'post', mode: 'cors', cache: 'no-cache',
        headers: { 'Content-type': 'application/json' },
        body: { 'message': message }})
        .then(res => callback(res))
  }
}