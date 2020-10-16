Feature: Player performs impossible blocking action

  Scenario: Start immediately blocking after stopping
    Given player connected with version "1.8.9"
    And has "survival" gamemode
    And spawns
    When player stops blocking
    And starts blocking
    Then player has following violations
    | Just stopped blocking |