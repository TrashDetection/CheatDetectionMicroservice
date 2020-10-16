Feature: Player performs plausible blocking action

  #The client removes the player entity when the chunk they are in is unloaded
  Scenario: Player was removed from chunk while blocking
    Given player connected with version "1.8.9"
    And has "survival" gamemode
    And spawns inside chunk 3 3
    When player starts blocking
    And received chunk unload at 3 3
    And stops blocking
    Then player has no violations