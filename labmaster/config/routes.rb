Rails.application.routes.draw do
  get 'dev/dummy_database'
  get 'dev/all'
  post 'packet/upload'
  get 'packet/list'
end
