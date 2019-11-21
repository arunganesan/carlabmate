class TextingController < ApplicationController
    require 'time'
    require 'fileutils'
    require 'twilio-ruby'

    skip_before_action :verify_authenticity_token
    PUBLIC = Rails.root.join('public')
    
    def register_phone    
        if !params.has_key? :number or !params.has_key? :serverport or !params.has_key? :session
            head :invalid
            return
        end

        tablerow = Phone.find_by(phoneno: params[:number])
        if tablerow.blank?
            tablerow = Phone.new
        end

        tablerow.session = params[:session]
        tablerow.port = params[:serverport]
        head :ok
        return
    end

    def schedule_text
        account_sid = ENV['TWILIO_ACCOUNT_SID']
        auth_token = ENV['TWILIO_AUTH_TOKEN']
    
        @client = Twilio::REST::Client.new(account_sid, auth_token)
        
        if !params.has_key? :number or !params.has_key? :message
            head :invalid
            return
        end
        
        tablerow = Phone.find_by(phoneno: params[:number])
        if tablerow.blank?
            head :invalid
            return
        end
        
        # make twilio call
        message = @client.messages.create(
            body: params[:message],
            from: '+17344363993',
            to: params[:number]
        )

        head :ok
   end


    def receive_response
        puts request

        message = params[:Body]
        from_number = params[:From]
        from_number.sub! '+', ''

        tablerow = Phone.find_by(phoneno: from_number)
        if tablerow.blank?
            puts "Error - phone number not registered"
        else
            port = tablerow.port
            session = tablerow.session
            
            # make open uri call to the linking server
            uri = URI.parse("http://localhost:#{port}/packet/upload")
            response = Net::HTTP.post_form(uri, {
                "session" => session, 
                "message" => message,
                "information" => "text"
            })
        end
        
        render :json => {}
    end
end