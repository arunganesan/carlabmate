class Packet < ApplicationRecord
    belongs_to :user
    belongs_to :information
end