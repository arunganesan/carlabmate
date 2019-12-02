class UsersController < ApplicationController
    require 'time'
    require 'digest'
    require 'fileutils'
    require 'SecureRandom'

    skip_before_action :verify_authenticity_token

    PUBLIC = Rails.root.join('public')
    MAX_DB_MESSAGE_SIZE = 50

    def login
        # if authenticated, return a unique string for this user
        # we can use this to create a sessions ID... that is stored on the client for all future communications.... we look up session ID in database... before .... that is done in linking server anyway so it works fine
        if !params.has_key? :username or !params.has_key? :password
            head :invalid
            return
        end

        username = params[:username]
        password = params[:password]
        user = User.find_by(username: username, password: password)
        if user.blank?
            head :unauthorized
            puts 'Invalid username and password'
            return 
        end

        render :json => {
            'username': username,
            'session': user.session
        }
    end

    def createuser
        @return_message = ""

        if request.post?
            if !params.has_key? :username or !params.has_key? :password
                @return_message = "<font color='red'>Invalid form input</font>"
                return
            end
            username = params[:username]
            password = params[:password]
            user = User.find_by(username: username)
            if !user.blank?
                @return_message = "<font color='red'>User already exists</font>"
                return
            end

            user = User.new
            user.username = username
            user.password = password
            user.session = SecureRandom.hex
            user.save
            
            @return_message = "<font color='green'>Success</font>"
        else
            # make the log in page.
        end
    end
end
