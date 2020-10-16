Feature: Player eats golden apple

  Scenario: Player won't stop sprinting
    Given player connected with version "1.8.9"
    And has "survival" gamemode
    And spawns
    When player starts sprinting
    And eats "golden apple"
    And stands
    Then player has following violations
    | On blocking kept sprint |
    
  Scenario: Player is unable to eat due to being in creative
    Given player connected with version "1.8.9"
    And has "creative" gamemode
    And spawns
    When player starts sprinting
    And eats "golden apple"
    And stands
    Then player has no violations