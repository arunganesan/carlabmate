Rails.application.routes.draw do
  # For details on the DSL available within this file, see https://guides.rubyonrails.org/routing.html

  get 'dev/dummy_database'
  
  post 'packet/upload'
  get 'packet/list'
end
