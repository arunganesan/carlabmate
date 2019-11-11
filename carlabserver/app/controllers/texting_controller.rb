class TextingController < ApplicationController
    require 'time'
    require 'fileutils'
    require 'twilio-ruby'

    skip_before_action :verify_authenticity_token
    PUBLIC = Rails.root.join('public')

    @@phone_port_mapping = {}

    def schedule_text
        account_sid = ENV['TWILIO_ACCOUNT_SID']
        auth_token = ENV['TWILIO_AUTH_TOKEN']
    
        @client = Twilio::REST::Client.new(account_sid, auth_token)

        if !params.has_key? :number or !params.has_key? :serverport or !params.has_key? :message
            head :invalid
            return
        end

        # make twilio call
        message = @client.messages.create(
            body: params[:message],
            from: '+17344363993',
            to: params[:number]
        )
        
        @@phone_port_mapping[params[:number]] = params[:serverport]    
        render :json => {}
   end


    def receive_response
        puts request

        message = params[:Body]
        from_number = params[:From]
        from_number.sub! '+', ''
        port_mapping = @@phone_port_mapping[from_number]

        # route it to the proper channels
        # look up the response name.

        puts message
        puts from_number
        puts "PORT IS", port_mapping
        puts @@phone_port_mapping


        # make open uri call to the linking server
        #Linkserver:1234/packet/upload?person=???&information=TEXT&message=VALUE        
        uri = URI.parse("http://localhost:#{port_mapping}/packet/upload")
        response = Net::HTTP.post_form(uri, {
            "person" => "21", 
            "message" => message,
            "information" => "text"
        })
        


        render :json => {}
    end
end