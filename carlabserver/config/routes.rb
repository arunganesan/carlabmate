Rails.application.routes.draw do
  get 'plan/design'
  post 'plan/launch'

  get 'texting/register_phone'
  post 'texting/register_phone'
  get 'texting/schedule_text'
  post 'texting/schedule_text'
  get 'texting/receive_response'
  post 'texting/receive_response'
end
