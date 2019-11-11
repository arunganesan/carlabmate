class TextingController < ApplicationController
    require 'time'
    require 'fileutils'
    require 'twilio-ruby'


    skip_before_action :verify_authenticity_token

    PUBLIC = Rails.root.join('public')

    def schedule_text
        account_sid = ENV['TWILIO_ACCOUNT_SID']
        auth_token = ENV['TWILIO_AUTH_TOKEN']
    
        @client = Twilio::REST::Client.new(account_sid, auth_token)

        if !params.has_key? :number or !params.has_key? :serverport or !params.has_key? :message
            head :invalid
            return
        end

        # make twilio call
        # if success return that

        message = @client.messages.create(
            body: params[:message],
            from: '+17344363993',
            to: params[:number]
        )
            
        render :json => {}
   end


    def receive_response
        puts request
        render :json => {}
    end
end