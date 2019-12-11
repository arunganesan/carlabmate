class CreateController < ApplicationController
    require 'time'
    require 'fileutils'
    skip_before_action :verify_authenticity_token

    PUBLIC = Rails.root.join('public')

    def launch
        puts "Got upload. Params are: ", params[:message]
        if !request.post?
            head :invalid
            return
        end
        
        requirements_str = request.body.read
        requirements_json = JSON.parse(requirements_str)
        
        appname = requirements_json['name']
        puts(appname)
        
        CreatePlatformJob.perform_later requirements_json

        head :ok
        return
    end


    def status
        if !request.get? or  !params.has_key? :appname
            head :invalid
            return
        end

        job = Job.find_by name: params[:appname]
        render :json => job.status
    end
end
