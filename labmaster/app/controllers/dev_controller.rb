class DevController < ApplicationController
    def dummy_database 
        Person.delete_all
        Information.delete_all

        for i in 1..10 do
            Person.create(:name => "Person #{i}")
            Information.create(:name => "Info #{i}")
        end

        # populate a bunch of data

        render :json => {
            'information': Information.all,
            'people': Person.all,
            'packets': Packet.all            
        }
    end
end