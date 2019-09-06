class DevController < ApplicationController
    def dummy_database 
        render :json => ['ok']
    end
end