class CreateInfoTable < ActiveRecord::Migration[6.0]
  def change
    create_table :information do |t|
      t.string :name
    end

    create_table :people do |t|
      t.string :name
    end

    create_table :packets do |t|
      t.string :url
      t.datetime :received
      t.references :person
      t.references :information
    end
  end
end
