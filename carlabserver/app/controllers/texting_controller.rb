class TextingController < ApplicationController
    require 'time'
    require 'fileutils'
    require 'twilio-ruby'
    require 'colorize'

    skip_before_action :verify_authenticity_token
    PUBLIC = Rails.root.join('public')
    
    def register_phone    
        ActiveRecord::Base.logger = nil
        Rails.logger.level = 5 # at any time

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
        tablerow.phoneno = params[:number]
        tablerow.save
        head :ok
        return
    end

    def schedule_text
        ActiveRecord::Base.logger = nil
        Rails.logger.level = 5 # at any time

        account_sid = ENV['TWILIO_ACCOUNT_SID']
        auth_token = ENV['TWILIO_AUTH_TOKEN']
    
        @client = Twilio::REST::Client.new(account_sid, auth_token)
        
            head :invalid
            return
        end
        
        tablerow = Phone.find_by(session: params[:session])
        if tablerow.blank?
            head :invalid
            return
        end
        
        # make twilio call
        message = @client.messages.create(
            body: params[:message],
            from: '+17344363993',
            to: tablerow.phoneno
        )

        head :ok
   end


    def receive_response
        puts request
        ActiveRecord::Base.logger = nil
        Rails.logger.level = 5 # at any time

        message = params[:Body]
        from_number = params[:From]
        from_number.sub! '+1', ''
        
        tablerow = Phone.find_by(phoneno: from_number)
        if tablerow.blank?
            puts "Error - phone number >#{from_number}< not registered".colorize(:color => :white, :background => :red)

            account_sid = ENV['TWILIO_ACCOUNT_SID']
            auth_token = ENV['TWILIO_AUTH_TOKEN']
            @client = Twilio::REST::Client.new(account_sid, auth_token)
            message = @client.messages.create(
                body: "This phone number isn't registered with CarLab.",
                from: '+17344363993',
                to: from_number
            )
            
            head :ok
            return
        else
            port = tablerow.port
            session = tablerow.session
            
            # make open uri call to the linking server
            uri = URI.parse("http://localhost:#{port}/packet/upload")
            response = Net::HTTP.post_form(uri, {
                "session" => session, 
                "message" => message,
                "information" => "user-text"
            })
        end
        
        render :json => {}
    end
end
