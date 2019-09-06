class PacketController < ApplicationController
    require 'time'
    require 'digest'
    require 'fileutils'

    PUBLIC = Rails.root.join('public')

    def upload
        # Must be post
        # Must contain params about information + person
        # Must contain data
        if !request.post? or !request.has_key? :information or !params.has_key? :person or !request.has_key? :file
            head :invalid
            return
        end

        person = Person.find_by(:id: params[:person])
        information = Information.find_by(:id: params[:information])
        if person.blank? or information.blank?
            head :invalid
            return
        end

        # move file to location
        dest_dir = "#{PUBLIC.to_s}/#{params[:person]}/#{params[:information]}"
        FileUtils.mkdir_p dest_dir
        save_filename = "#{dest_dir}/#{Time.now.strftime('%Y-%m-%d_%H-%M-%S')}"
        file = FileUtils.copy_entry params[:file].tempfile.path, save_filename
        puts (save_filename)
        
        Packet.create(
            :url => save_filename,
            :received => Time.now,
            :person => person,
            :information => information
        )
        
        render :json => {
            'information': Information.all,
            'people': Person.all,
            'packets': Packet.all            
        }
    end


    def list 
        # Must be get
        # Must contain params about info and person and last date
        if !request.get? or !request.has_key? :information or !params.has_key? :person or !params.has_key? :sincetime
            head :invalid
            return
        end

        render :json => {
            Packet.where('received > :sincetime AND person_id = :person_id AND information_id = :information_id', {
                sincetime: params[:sincetime],
                person_id: params[:person],
                information_id: params[:information],
            })
        }
    end
end