import requests
import time

class libcarlab ():
    def __init__ (self, userid, required_info, test=False):
        self.last_check_time = 0
        self.userid = userid
        self.test = test
        self.required_info = required_info
        self.baseurl = 'http://localhost:1234'
        self.fetch_url = '{base}?information={info}&person={person}&sincetime={time}'
        self.push_url = '{base}?information={info}&person={person}'
    
    def check_new_info (self):
        # Make server call to /packet/list
        # if there are files, copy them over 
        # (stick to HTTP routing so we can put server scripts on a separate machine)
        # XXX do differently if testing
        new_data = []

        for info in self.required_info:
            results = requests.get(self.fetch_url.format(
                base=self.baseurl,
                info=info,
                person=self.userid,
                time=self.last_check_time,
            ))
            new_data += results.json()

        self.last_check_time = time.time()
        return new_data
    
    def output_new_info (self, info, value):
        # XXX do differently for test
        
        # Make server call to /packet/upload
        result = requests.post(self.push_url.format(
            base=self.baseurl,
            info=info,
            person=self.userid,
        ), { 'message': value })

        # Check if return succeeded or failed
        return True
