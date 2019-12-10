class CreatePlatformJob < ApplicationJob
  queue_as :default
  PUBLIC = Rails.root.join('public')

  before_enqueue do |job|
    requirements_json = job.arguments.first
    job_name = requirements_json['name']
    existing_job = Job.find_by name: job_name
    if existing_job == nil
      Job.create :name => job_name, :status => 0
    else
      existing_job.status = 0
      existing_job.save
    end
  end 

  before_perform do |job|
    requirements_json = job.arguments.first
    job_name = requirements_json['name']
    existing_job = Job.find_by name: job_name
    if existing_job == nil
      Job.create :name => job_name, :status => 1
    else
      existing_job.status = 1
      existing_job.save
    end
  end


  def perform requirements_json
    save_filename = "#{PUBLIC.to_s}/requirements.jsonc"
    file = File.open(save_filename, 'w')
    file.puts(requirements_json.to_json)
    file.close
    
    requirements_file = 'requirements.jsonc'
    
    Dir.chdir(PUBLIC){
      returnvalue = `python3.7 cl-strategy.py requirements.jsonc`

      puts "CALLING STRATEGY"
      if $?.exitstatus != 0
          # XXX how do we handle errors?
          return
      end

      file = File.open("#{PUBLIC.to_s}/strategy.jsonc", 'w')
      file.puts(returnvalue)
      file.close

      puts "CALLING SANDBOX"
      returnval = `python3.7 create-sandbox.py strategy.jsonc`
  }

  end


  after_perform do |job|
    # 5. Mark the job as done. 
    requirements_json = job.arguments.first
    job_name = requirements_json['name']
    existing_job = Job.find_by name: job_name
    if existing_job == nil
      Job.create :name => job_name, :status => 2
    else
      existing_job.status = 2
      existing_job.save
    end
  end
end
