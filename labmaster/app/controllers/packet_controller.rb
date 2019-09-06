class PacketController < ApplicationController
    require 'time'
    require 'digest'
    require 'fileutils'

    PUBLIC = Rails.root.join('public')

    def upload
        # Must be post
        # Must contain params about information + person
        # Must contain data
        if !request.post? or !params.has_key? :information or !params.has_key? :person
            head :invalid
            return
        end

        # must have message or file
        if !params.has_key? :file and !params.has_key? :message
            head :invalid
            return
        end

        person = Person.find_by(:id: params[:person])
        information = Information.find_by(:id: params[:information])
        if person.blank? or information.blank?
            head :invalid
            return
        end

        packet = Packet.new
        packet.received = Time.now
        packet.person = person
        packet.information = information
        
        if params.kas_key? :message
            packet.message = message
        end

        # move file to location
        if params.has_key? :file
            dest_dir = "#{PUBLIC.to_s}/#{params[:person]}/#{params[:information]}"
            FileUtils.mkdir_p dest_dir
            save_filename = "#{dest_dir}/#{Time.now.strftime('%Y-%m-%d_%H-%M-%S')}"
            file = FileUtils.copy_entry params[:file].tempfile.path, save_filename
            puts (save_filename)
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

        render :json => {
            Packet.where('received > :sincetime AND person_id = :person_id AND information_id = :information_id', {
                sincetime: DateTime.parse(params[:sincetime]),
                person_id: params[:person],
                information_id: params[:information],
            })
        }
    end
end