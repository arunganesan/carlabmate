Rails.application.routes.draw do
  get 'plan/design'
  post 'plan/design'
  post 'plan/launch'

  get 'texting/register_phone'
  post 'texting/register_phone'
  get 'texting/schedule_text'
  post 'texting/schedule_text'
  get 'texting/receive_response'
  post 'texting/receive_response'


  # takes the requirements, generate strategy
  # if not possible return false
  # if psosible then launch the compile job, return the job ID
  post 'create/launch'


  # takes the job ID, return the status
  # if completed, it returns teh actual URL to sign up
  get 'create/status'  
end
