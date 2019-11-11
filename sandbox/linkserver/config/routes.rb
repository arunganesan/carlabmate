Rails.application.routes.draw do
  get 'dev/dummy_database'
  get 'dev/sync_registry'
  get 'dev/all'
  post 'packet/upload'
  get 'packet/list'
  get 'packet/listall'
end
