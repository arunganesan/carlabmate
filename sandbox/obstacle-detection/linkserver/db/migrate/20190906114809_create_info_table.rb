class CreateInfoTable < ActiveRecord::Migration[6.0]
  def change
    create_table :information do |t|
      t.string :name
    end


    create_table :users do |t|
      t.string :username
      t.string :password
      t.string :session
    end


    create_table :packets do |t|
      t.string :file
      t.string :message
      t.datetime :received
      t.references :user
      t.references :information
    end
  end
end
