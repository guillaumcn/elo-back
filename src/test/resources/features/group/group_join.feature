Feature: Group Join — Open & Invitation

  Scenario: Join an open group
    Given I am authenticated as "alice"
    And another user has created a group named "Ping Pong Club" with join policy "OPEN"
    When I join the group
    Then I receive a 200 OK response
    And I am now a member of the group

  Scenario: Cannot directly join a non-open group
    Given I am authenticated as "alice"
    And another user has created a group named "Chess Club" with join policy "INVITATION"
    When I join the group
    Then I receive a 422 Unprocessable Entity response

  Scenario: Already a member tries to join
    Given I am authenticated as "alice"
    And another user has created a group named "Ping Pong Club" with join policy "OPEN" and added me as a member
    When I join the group
    Then I receive a 409 Conflict response

  Scenario: Cannot join archived group
    Given I am authenticated as "alice"
    And another user has created a group named "Old Club" with join policy "OPEN" and archived it
    When I join the group
    Then I receive a 422 Unprocessable Entity response

  Scenario: Join non-existent group
    Given I am authenticated as "alice"
    And a non-existent group id
    When I join the group
    Then I receive a 404 Not Found response

  Scenario: Create an invitation
    Given I am authenticated as "alice"
    And I have created a group named "Chess Club" with join policy "INVITATION"
    When I create an invitation for the group
    Then I receive a 201 Created response
    And the invitation has a token

  Scenario: Create an invitation with expiration date
    Given I am authenticated as "alice"
    And I have created a group named "Chess Club" with join policy "INVITATION"
    When I create an invitation with expiration "2099-12-31T23:59:59Z"
    Then I receive a 201 Created response
    And the invitation has a token

  Scenario: Non-member cannot create an invitation
    Given I am authenticated as "alice"
    And another user has created a group named "Chess Club"
    When I create an invitation for the group
    Then I receive a 404 Not Found response

  Scenario: Join by invitation token
    Given I am authenticated as "alice"
    And another user has created a group named "Chess Club" with join policy "INVITATION" and created an invitation
    When I join the group by invitation
    Then I receive a 200 OK response
    And I am now a member of the group

  Scenario: Join with expired invitation
    Given I am authenticated as "alice"
    And another user has created a group named "Chess Club" with join policy "INVITATION" and created an expired invitation
    When I join the group by invitation
    Then I receive a 422 Unprocessable Entity response

  Scenario: Join with invalid token
    Given I am authenticated as "alice"
    And I have created a group named "Chess Club" with join policy "INVITATION"
    When I join the group with token "invalid-token-xyz"
    Then I receive a 404 Not Found response

  Scenario: Already a member tries to join by invitation
    Given I am authenticated as "alice"
    And another user has created a group named "Chess Club" with join policy "INVITATION" and added me as a member and created an invitation
    When I join the group by invitation
    Then I receive a 409 Conflict response

  Scenario: Cannot directly join a group with request-to-join policy
    Given I am authenticated as "alice"
    And another user has created a group named "Exclusive Club" with join policy "REQUEST"
    When I join the group
    Then I receive a 422 Unprocessable Entity response

  Scenario: Cannot join a group with a blank token
    Given I am authenticated as "alice"
    And another user has created a group named "Chess Club" with join policy "INVITATION"
    When I join the group with token ""
    Then I receive a 400 Bad Request response

  Scenario: Cannot create an invitation for a non-existent group
    Given I am authenticated as "alice"
    And a non-existent group id
    When I create an invitation for the group
    Then I receive a 404 Not Found response

  Scenario: Cannot join an archived group by invitation
    Given I am authenticated as "alice"
    And another user has created a group named "Closed Club" with join policy "INVITATION" and created an invitation and archived it
    When I join the group by invitation
    Then I receive a 422 Unprocessable Entity response

  Scenario: A group member can create an invitation
    Given I am authenticated as "alice"
    And another user has created a group named "Chess Club" with join policy "INVITATION" and added me as a member
    When I create an invitation for the group
    Then I receive a 201 Created response
    And the invitation has a token

  Scenario: Invitation without expiry has no expiration date
    Given I am authenticated as "alice"
    And I have created a group named "Chess Club" with join policy "INVITATION"
    When I create an invitation for the group
    Then I receive a 201 Created response
    And the invitation expiry is null

  Scenario: Join a group without authentication
    Given a non-existent group id
    When I join the group without authentication
    Then I receive a 401 Unauthorized response

  Scenario: Create an invitation without authentication
    Given a non-existent group id
    When I create an invitation for the group without authentication
    Then I receive a 401 Unauthorized response

  Scenario: Join a group by invitation without authentication
    Given a non-existent group id
    When I join the group by invitation without authentication
    Then I receive a 401 Unauthorized response
