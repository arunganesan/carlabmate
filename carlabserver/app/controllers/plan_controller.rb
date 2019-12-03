class PlanController < ApplicationController
    require 'time'
    require 'fileutils'
    skip_before_action :verify_authenticity_token

    PUBLIC = Rails.root.join('public')

    def launch
        # Must be post
        # Has information about the algorithms used in this study
        # And some identifying information (maybe)

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
        
        

        render :json => {}
   end


    def design 
        # Must be get
        # Must contain params about the info needed plus any user inputs such as 
        #   - Which devices they have access to
        #   - Which info they want to avoid
        #   - Any info-algorithm selections
        puts params
        requirements_str = request.body.read
        
        # write to file
        save_filename = "#{PUBLIC.to_s}/requirements.jsonc"
        file = File.open(save_filename, 'w')
        file.puts(requirements_str)
        file.close

        requirements_file = 'requirements.jsonc'
        strategy_file = 'strategy.jsonc'

        Dir.chdir(PUBLIC){
            `python3.7 cl-strategy.py requirements.jsonc > strategy.jsonc`
            `python3.7 draw-strategy.py --requirements requirements.jsonc --strategy strategy.jsonc --draw both`
        }
        
        render :json => {}
    end
end