Rails.application.routes.draw do
  get 'dev/dummy_database'
  post 'packet/upload'
  get 'packet/list'
end
