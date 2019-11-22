class PacketController < ApplicationController
    require 'time'
    require 'digest'
    require 'fileutils'
    require 'colorize'

    skip_before_action :verify_authenticity_token

    PUBLIC = Rails.root.join('public')
    MAX_DB_MESSAGE_SIZE = 50

    def upload
        ActiveRecord::Base.logger = nil
        Rails.logger.level = 5 # at any time

        # Must be post
        # Must contain params about information + person
        # Must contain data
        if !request.post? or !params.has_key? :information or !params.has_key? :session
            puts 'Invalid post params'
            head :invalid
            return
        end

        # must have message or file
        if !params.has_key? :file and !params.has_key? :message
            head :invalid
            return
        end
        
        user = User.find_by(session: params[:session])
        
        if user.blank?
            head :invalid
            return
        end

        information = Information.find_by(name: params[:information])
        if information.blank?
            information = Information.create(name: params[:information])
        end


        packet = Packet.new
        packet.received = Time.now
        packet.user = user
        packet.information = information
        
        dest_dir = "#{PUBLIC.to_s}/#{user.username}/#{params[:information]}"
        FileUtils.mkdir_p dest_dir
        save_filename = "#{dest_dir}/#{Time.now.strftime('%Y-%m-%d_%H-%M-%S')}.json"
        
        if params.has_key? :message
            file = File.open(save_filename, 'w')
            file.puts(params[:message])
            file.close
            
            if params[:message].length > MAX_DB_MESSAGE_SIZE
                packet.file = save_filename
            else
                packet.message = params[:message]
            end
        end

        puts "-> [#{Time.now.utc.iso8601}] Received #{params[:information]}".colorize(:color => :green)

        # move file to location
        if params.has_key? :file
            file = FileUtils.copy_entry params[:file].tempfile.path, save_filename
            puts 'saved file to ', save_filename
            packet.file = save_filename
        end
        
        packet.save!

        render :json => {
            'information': Information.all,
            'users': User.all,
            'packets': Packet.all            
        }
    end



    def listall
        ActiveRecord::Base.logger = nil
        Rails.logger.level = 5 # at any time


        if !request.get? or !params.has_key? :session
            head :invalid
            return
        end

        user = User.find_by(session: params[:session])
        if user.blank?
            head :invalid
            return
        end

        render :json => Packet.where(user: user)
    end


    def latest
        ActiveRecord::Base.logger = nil
        Rails.logger.level = 5 # at any time

        # Must be get
        # Must contain params about info and person and last date
        
        if !request.get? or !params.has_key? :information or !params.has_key? :session
            puts 'Invalid post params'
            head :invalid
            return
        end
        
        user = User.find_by(session: params[:session])
        
        if user.blank?
            head :invalid
            puts 'No user found for session', session
            User.all.each do | u |
              puts u.to_json
            end
            return
        end
        
        information = Information.find_by(name: params[:information])
        if information.blank?
            information = Information.create(name: params[:information])
        end
        last_info = Packet.where(user: user, information: information).order('received DESC').first
        puts "<- [#{Time.now.utc.iso8601}] Return latest #{information}: #{last_info}: ".colorize(:color => :green)
        render :json => last_info
    end
    
    def list 
        ActiveRecord::Base.logger = nil
        Rails.logger.level = 5 # at any time

        # Must be get
        # Must contain params about info and person and last date
        if !request.get? or !params.has_key? :information or !params.has_key? :session or !params.has_key? :sincetime
            head :invalid
            return
        end

        user = User.find_by(session: params[:session])
        if user.blank?
            head :invalid
            return
        end
        
        information = Information.find_by(name: params[:information])
        if information.blank? or information.nil?
            render :json => []
            return
        end


        return_data = Packet.where('received > :sincetime AND user_id = :user_id AND information_id = :information_id', {
            sincetime: DateTime.strptime(params[:sincetime], '%s'),
            user_id: user.id,
            information_id: information.id,
        })

        if return_data.size == 0
            puts "~ [#{Time.now.utc.iso8601}] Request #{params[:information]}".colorize(:color => :black)
        else
            puts "<- [#{Time.now.utc.iso8601}] Downloaded #{params[:information]}".colorize(:color => :green)
        end
      
        render :json => return_data
    end
end
