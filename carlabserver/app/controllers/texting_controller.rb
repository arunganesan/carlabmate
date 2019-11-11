class TextingController < ApplicationController
    require 'time'
    require 'fileutils'
    require 'twilio-ruby'

    account_sid = ENV['TWILIO_ACCOUNT_SID']
    auth_token = ENV['TWILIO_AUTH_TOKEN']
    @client = Twilio::REST::Client.new(account_sid, auth_token)

    skip_before_action :verify_authenticity_token

    PUBLIC = Rails.root.join('public')

    def schedule_text
        if !params.has_key? :number or !params.has_key? :serverport or !params.has_key? :message
            head :invalid
            return
        end

        # make twilio call
        # if success return that



        message = @client.messages.create(
                body: 'Hi there!',
                from: '+15017122661',
                to: '+15558675310'
            )
            
        render :json => {}
   end


    def receive_response
        if !params.has_key? :information or !params.has_key? :person or !params.has_key? :sincetime
            head :invalid
            return
        end
        
        
        render :json => {}
    end
end