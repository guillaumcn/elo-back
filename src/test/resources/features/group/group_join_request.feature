Feature: Group Join — Request to Join

  Scenario: Submit a join request for a REQUEST group
    Given I am authenticated as "alice"
    And another user has created a group named "Private Club" with join policy "REQUEST"
    When I submit a join request for the group
    Then I receive a 201 Created response
    And the join request status is "PENDING"

  Scenario: Cannot submit a duplicate join request
    Given I am authenticated as "alice"
    And another user has created a group named "Private Club" with join policy "REQUEST"
    And I have submitted a join request for the group
    When I submit a join request for the group
    Then I receive a 409 Conflict response

  Scenario: Cannot request to join a group you are already a member of
    Given I am authenticated as "alice"
    And another user has created a group named "Private Club" with join policy "REQUEST" and added me as a member
    When I submit a join request for the group
    Then I receive a 409 Conflict response

  Scenario: Cannot request to join an archived group
    Given I am authenticated as "alice"
    And another user has created a group named "Private Club" with join policy "REQUEST" and archived it
    When I submit a join request for the group
    Then I receive a 422 Unprocessable Entity response

  Scenario: Cannot request to join a group with a non-REQUEST join policy
    Given I am authenticated as "alice"
    And another user has created a group named "Open Club" with join policy "OPEN"
    When I submit a join request for the group
    Then I receive a 422 Unprocessable Entity response

  Scenario: Admin approves a join request
    Given I am authenticated as "alice"
    And I have created a group named "Private Club" with join policy "REQUEST"
    And another user has submitted a join request for the group
    When I approve the join request
    Then I receive a 200 OK response
    And the join request status is "APPROVED"
    And the requester is now a member of the group

  Scenario: Admin denies a join request
    Given I am authenticated as "alice"
    And I have created a group named "Private Club" with join policy "REQUEST"
    And another user has submitted a join request for the group
    When I deny the join request
    Then I receive a 200 OK response
    And the join request status is "DENIED"

  Scenario: Non-admin cannot approve a join request
    Given I am authenticated as "alice"
    And another user has created a group named "Private Club" with join policy "REQUEST"
    And another user has submitted a join request for the group
    When I approve the join request
    Then I receive a 403 Forbidden response

  Scenario: Admin lists all join requests
    Given I am authenticated as "alice"
    And I have created a group named "Private Club" with join policy "REQUEST"
    And another user has submitted a join request for the group
    When I list join requests for the group
    Then I receive a 200 OK response
    And the join requests list is not empty

  Scenario: Non-admin cannot list join requests
    Given I am authenticated as "alice"
    And another user has created a group named "Private Club" with join policy "REQUEST"
    When I list join requests for the group
    Then I receive a 403 Forbidden response

  Scenario: Cannot resolve an already resolved join request
    Given I am authenticated as "alice"
    And I have created a group named "Private Club" with join policy "REQUEST"
    And another user has submitted a join request for the group
    And I have approved the join request
    When I approve the join request
    Then I receive a 422 Unprocessable Entity response

  Scenario: Submit a join request without authentication
    Given a non-existent group id
    When I submit a join request for the group without authentication
    Then I receive a 401 Unauthorized response

  Scenario: List join requests without authentication
    Given a non-existent group id
    When I list join requests for the group without authentication
    Then I receive a 401 Unauthorized response

  Scenario: Handle a join request without authentication
    Given a non-existent group id
    When I handle a join request without authentication
    Then I receive a 401 Unauthorized response

  Scenario: Cannot submit a join request for a non-existent group
    Given I am authenticated as "alice"
    And a non-existent group id
    When I submit a join request for the group
    Then I receive a 404 Not Found response

  Scenario: Cannot handle a non-existent join request
    Given I am authenticated as "alice"
    And I have created a group named "Private Club" with join policy "REQUEST"
    And a non-existent join request id
    When I approve the join request
    Then I receive a 404 Not Found response

  Scenario: Handle join request with missing action returns 400
    Given I am authenticated as "alice"
    And I have created a group named "Private Club" with join policy "REQUEST"
    And another user has submitted a join request for the group
    When I handle the join request with no action
    Then I receive a 400 Bad Request response

  Scenario: Cannot list join requests for a non-existent group
    Given I am authenticated as "alice"
    And a non-existent group id
    When I list join requests for the group
    Then I receive a 404 Not Found response

  Scenario: Cannot deny an already denied join request
    Given I am authenticated as "alice"
    And I have created a group named "Private Club" with join policy "REQUEST"
    And another user has submitted a join request for the group
    And I have denied the join request
    When I deny the join request
    Then I receive a 422 Unprocessable Entity response

  Scenario: Admin cannot submit a join request to their own group
    Given I am authenticated as "alice"
    And I have created a group named "Private Club" with join policy "REQUEST"
    When I submit a join request for the group
    Then I receive a 409 Conflict response

  Scenario: Approved join request response contains resolver information
    Given I am authenticated as "alice"
    And I have created a group named "Private Club" with join policy "REQUEST"
    And another user has submitted a join request for the group
    When I approve the join request
    Then I receive a 200 OK response
    And the join request status is "APPROVED"
    And the join request resolver is set

  Scenario: Admin lists join requests when none have been submitted
    Given I am authenticated as "alice"
    And I have created a group named "Private Club" with join policy "REQUEST"
    When I list join requests for the group
    Then I receive a 200 OK response
    And the join requests list is empty
