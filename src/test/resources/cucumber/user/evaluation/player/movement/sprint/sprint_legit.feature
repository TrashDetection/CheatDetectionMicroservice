Feature: Player performs plausible sprinting action

  Scenario: Player starts sprinting and stops
    Given player connected with version "1.8.9"
    And has "survival" gamemode
    And spawns
    When player starts sprinting
    And stands
    And stops sprinting
    Then player has no violations
  Scenario: Player starts and stops sprinting while teleport confirm
    Given player connected with version "1.8.9"
    And has "survival" gamemode
    And spawns
    When player is being teleported to 3.0 6.0 9.0
    And sends teleport pre confirm
    And starts sprinting
    And moves to 3.0 3.0 3.0
    And stops sprinting
    And flies to 3.0 6.0 9.0
    And sends teleport confirm
    Then player has no violations