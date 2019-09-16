import calendar
import os
import pickle
import requests
import time

class libcarlab ():
    def __init__ (self, userid, required_info, output_info, save_filename, test=False):
        self.last_check_time = calendar.timegm(time.gmtime()) - 10000
        self.userid = userid
        self.test = test
        self.required_info = required_info
        self.baseurl = 'http://localhost:1234/packet/'
        self.fetch_url = '{base}list?information={info}&person={person}&sincetime={time}'
        self.push_url = '{base}upload?information={info}&person={person}'
        self.save_filename = save_filename

        if not os.path.exists(save_filename):
            ofile = open(save_filename, 'wb')
            pickle.dump({}, ofile)
            ofile.close()

    
    def check_new_info (self):
        new_data = {}

        for info in self.required_info:
            url = self.fetch_url.format(
                base=self.baseurl,
                info=info,
                person=self.userid,
                time=self.last_check_time,
            )
            
            results = requests.get(url)

            print(url)
            new_data[info] = results.json()

        self.last_check_time = calendar.timegm(time.gmtime())
        return new_data
    
    def output_new_info (self, info, value):
        existingData = pickle.load(open(self.save_filename, 'rb'))
        existingData.setdefault(info, [])
        existingData[info].append(value)
        ofile = open(self.save_filename, 'wb')
        pickle.dump(existingData, ofile)
        ofile.close()
    
    
    def upload_data (self):
        for info in self.output_info:
            existingData = pickle.load(open(self.save_filename, 'rb'))

            if info in existingData:
                result = requests.post(self.push_url.format(
                    base=self.baseurl,
                    info=info,
                    person=self.userid,
                ), { 'message': existingData[info] })
            
                if result.status_code == 200:
                    del existingData[info]
                    ofile = open(self.save_filename, 'wb')
                    pickle.dump(existingData, ofile)
                    ofile.close()
