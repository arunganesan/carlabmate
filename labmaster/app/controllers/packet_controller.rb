class PacketController < ApplicationController
    require 'time'
    require 'digest'
    require 'fileutils'
    skip_before_action :verify_authenticity_token

    PUBLIC = Rails.root.join('public')

    def upload
        # Must be post
        # Must contain params about information + person
        # Must contain data
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
        
        person = Person.find_by(id: params[:person])
        
        if person.blank?
            head :invalid
            return
        end

        information = Information.find_by(name: params[:information])
        if information.blank?
            information = Information.create(name: params[:information])
        end


        packet = Packet.new
        packet.received = Time.now
        packet.person = person
        packet.information = information
        
        if params.has_key? :message
            packet.message = params[:message]
        end

        # move file to location
        if params.has_key? :file
            dest_dir = "#{PUBLIC.to_s}/#{params[:person]}/#{params[:information]}"
            FileUtils.mkdir_p dest_dir
            save_filename = "#{dest_dir}/#{Time.now.strftime('%Y-%m-%d_%H-%M-%S')}.json"
            file = FileUtils.copy_entry params[:file].tempfile.path, save_filename
            puts 'saved file to ', save_filename
            packet.file = save_filename
        end
        
        packet.save!

        render :json => {
            'information': Information.all,
            'people': Person.all,
            'packets': Packet.all            
        }
    end


    def list 
        # Must be get
        # Must contain params about info and person and last date
        if !request.get? or !params.has_key? :information or !params.has_key? :person or !params.has_key? :sincetime
            head :invalid
            return
        end

        information = Information.find_by(name: params[:information])
        if information.blank?
            render :json => []
        end

        render :json => Packet.where('received > :sincetime AND person_id = :person_id AND information_id = :information_id', {
                sincetime: DateTime.strptime(params[:sincetime], '%s'),
                person_id: params[:person],
                information_id: information.id,
            })
            
    end
end