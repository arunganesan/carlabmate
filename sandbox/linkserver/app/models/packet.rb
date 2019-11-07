class Packet < ApplicationRecord
    belongs_to :person
    belongs_to :information
end