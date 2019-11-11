Rails.application.routes.draw do
  get 'plan/design'
  post 'plan/launch'

  get 'texting/schedule_text'
  post 'texting/schedule_text'

  get 'texting/receive_response'
  post 'texting/receive_response'
end
