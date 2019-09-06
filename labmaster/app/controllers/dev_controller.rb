class DevController < ApplicationController
    def dummy_database 
        ActiveRecord::Base.logger = nil

        Person.delete_all
        Information.delete_all
        Packet.delete_all

        for i in 1..10 do
            Person.create(:name => "Person #{i}")
            Information.create(:name => "Info #{i}")
        end

        # populate a bunch of data
        for i in 1..100 do
            person = Person.find_by(:name => "Person #{rand(1..10)}")
            info = Information.find_by(:name => "Info #{rand(1..10)}")

            Packet.create(
                :message => "Packet #{i}",
                :received => Time.now,
                :person => person,
                :information => info)    
        end

        render :json => {
            'information': Information.all,
            'people': Person.all,
            'packets': Packet.all            
        }
    end
end