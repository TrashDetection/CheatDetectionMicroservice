Feature: Player performs impossible sprinting action

  Scenario: Start sprinting two times in a row
    Given player connected with version "1.8.9"
    And has "survival" gamemode
    And spawns
    When player starts sprinting
    And starts sprinting
    Then player has following violations
    | Sprint status update violation \| Sprinting: true |
    | Entity action too fast violation \| Action: sprint |