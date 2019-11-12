Rails.application.routes.draw do
  get 'dev/dummy_database'
  get 'dev/sync_registry'
  get 'dev/all'
  post 'packet/upload'
  get 'packet/list'
  get 'packet/listall'
  get 'packet/latest'


  get 'login', to: 'users#login'
  post 'login', to: 'users#login'
  get 'createuser', to: 'users#createuser'
  post 'createuser', to: 'users#createuser'
end
