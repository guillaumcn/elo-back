Feature: Group Archive / Unarchive

  Scenario: Admin archives a group
    Given I am authenticated as "alice"
    And I have created a group named "Ping Pong Club" with join policy "OPEN"
    When I archive the group
    Then I receive a 200 OK response
    And the group is archived

  Scenario: Admin unarchives a group
    Given I am authenticated as "alice"
    And I have created a group named "Ping Pong Club" with join policy "OPEN"
    And I have archived the group
    When I unarchive the group
    Then I receive a 200 OK response
    And the group is not archived

  Scenario: Non-admin tries to archive
    Given I am authenticated as "alice"
    And another user has created a group named "Their Club" with join policy "OPEN" and added me as a member
    When I archive the group
    Then I receive a 403 Forbidden response

  Scenario: Non-admin tries to unarchive
    Given I am authenticated as "alice"
    And another user has created a group named "Their Club" with join policy "OPEN" and added me as a member
    And an admin has archived the group
    When I unarchive the group
    Then I receive a 403 Forbidden response

  Scenario: Archive an already-archived group
    Given I am authenticated as "alice"
    And I have created a group named "Ping Pong Club" with join policy "OPEN"
    And I have archived the group
    When I archive the group
    Then I receive a 422 Unprocessable Entity response

  Scenario: Unarchive a non-archived group
    Given I am authenticated as "alice"
    And I have created a group named "Ping Pong Club" with join policy "OPEN"
    When I unarchive the group
    Then I receive a 422 Unprocessable Entity response

  Scenario: View archived group details
    Given I am authenticated as "alice"
    And I have created a group named "Ping Pong Club" with join policy "OPEN"
    And I have archived the group
    When I request the group details
    Then I receive a 200 OK response
    And the group is archived

  Scenario: Archive group without authentication
    Given a non-existent group id
    When I archive the group without authentication
    Then I receive a 401 Unauthorized response

  Scenario: Unarchive group without authentication
    Given a non-existent group id
    When I unarchive the group without authentication
    Then I receive a 401 Unauthorized response

  Scenario: Archive non-existent group
    Given I am authenticated as "alice"
    And a non-existent group id
    When I archive the group
    Then I receive a 404 Not Found response

  Scenario: Unarchive non-existent group
    Given I am authenticated as "alice"
    And a non-existent group id
    When I unarchive the group
    Then I receive a 404 Not Found response

  Scenario: Non-member tries to archive
    Given I am authenticated as "alice"
    And another user has created a group named "Exclusive Club"
    When I archive the group
    Then I receive a 404 Not Found response

  Scenario: Non-member tries to unarchive
    Given I am authenticated as "alice"
    And another user has created a group named "Exclusive Club"
    When I unarchive the group
    Then I receive a 404 Not Found response
