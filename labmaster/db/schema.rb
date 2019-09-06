# This file is auto-generated from the current state of the database. Instead
# of editing this file, please use the migrations feature of Active Record to
# incrementally modify your database, and then regenerate this schema definition.
#
# This file is the source Rails uses to define your schema when running `rails
# db:schema:load`. When creating a new database, `rails db:schema:load` tends to
# be faster and is potentially less error prone than running all of your
# migrations from scratch. Old migrations may fail to apply correctly if those
# migrations use external dependencies or application code.
#
# It's strongly recommended that you check this file into your version control system.

ActiveRecord::Schema.define(version: 2019_09_06_114809) do

  create_table "information", force: :cascade do |t|
    t.string "name"
  end

  create_table "packets", force: :cascade do |t|
    t.string "name"
    t.string "url"
    t.datetime "received"
    t.integer "person_id"
    t.integer "information_id"
    t.index ["information_id"], name: "index_packets_on_information_id"
    t.index ["person_id"], name: "index_packets_on_person_id"
  end

  create_table "people", force: :cascade do |t|
    t.string "name"
  end

end
