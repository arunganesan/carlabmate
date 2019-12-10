class CreateJobs < ActiveRecord::Migration[5.2]
  def change
    create_table :jobs do |t|
      t.text :name
      t.integer :status
      t.text :url
    end
  end
end
