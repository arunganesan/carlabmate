class CreatePhoneTable < ActiveRecord::Migration[6.0]
  def change
    create_table :phones do |t|
      t.string :session
      t.string :port
      t.string :phoneno
    end
  end
end
