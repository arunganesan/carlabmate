class PacketController < ApplicationController
    require 'time'
    require 'fileutils'
    skip_before_action :verify_authenticity_token

    PUBLIC = Rails.root.join('public')

    def launch
        # Must be post
        # Has information about the algorithms used in this study
        # And some identifying information (maybe)
        puts "Got upload. Params are: ", params[:message]
        if !request.post? or !params.has_key? :information or !params.has_key? :person
            head :invalid
            return
        end

        # must have message or file
        if !params.has_key? :file and !params.has_key? :message
            head :invalid
            return
        end
        
        

        render :json => {}
   end


    def design 
        # Must be get
        # Must contain params about the info needed plus any user inputs such as 
        #   - Which devices they have access to
        #   - Which info they want to avoid
        #   - Any info-algorithm selections
        if !request.get? or !params.has_key? :information or !params.has_key? :person or !params.has_key? :sincetime
            head :invalid
            return
        end
        
        
        render :json => {}
    end
end