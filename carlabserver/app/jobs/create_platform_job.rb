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
      Job.create :name => job_name, :status => 0
    else
      existing_job.status = 0
      existing_job.save
    end
  end


  def perform requirements_json
    job_name = requirements_json['name']
    existing_job = Job.find_by name: job_name

    save_filename = "#{PUBLIC.to_s}/requirements.jsonc"
    file = File.open(save_filename, 'w')
    file.puts(requirements_json.to_json)
    file.close

    
    requirements_file = 'requirements.jsonc'
    
    Dir.chdir(PUBLIC){
      returnvalue = `python3.7 cl-strategy.py requirements.jsonc`

      if $?.exitstatus != 0
          # XXX how do we handle errors?
          existing_job.status = -1
          return
      end

      file = File.open("#{PUBLIC.to_s}/strategy.jsonc", 'w')
      file.puts(returnvalue)
      file.close

      existing_job.status = 1
      existing_job.save!
      `python3.7 create-sandbox.py --step 1 strategy.jsonc`

      existing_job.status = 2
      existing_job.save!
      `python3.7 create-sandbox.py --step 2 strategy.jsonc`

      existing_job.status = 3
      existing_job.save!
      `python3.7 create-sandbox.py --step 3 strategy.jsonc`

      existing_job.status = 4
      existing_job.save!
      `python3.7 create-sandbox.py --step 4 strategy.jsonc`

      existing_job.status = 5
      existing_job.save!
      `python3.7 create-sandbox.py --step 5 strategy.jsonc`

      existing_job.status = 6
      existing_job.save!
      `python3.7 create-sandbox.py --step 6 strategy.jsonc`

      existing_job.status = 7
      existing_job.save!
      `python3.7 create-sandbox.py --step 7 strategy.jsonc`
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
      existing_job.status = 8
      existing_job.save
    end
  end
end
