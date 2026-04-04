Feature: Group CRUD

  Scenario: Create a group
    Given I am authenticated as "alice"
    And a create group request with name "Ping Pong Club"
    And the create group join policy is "OPEN"
    When I submit the create group request
    Then I receive a 201 Created response
    And the group name is "Ping Pong Club"
    And the group join policy is "OPEN"
    And the group member count is 1
    And I am listed as the group creator

  Scenario: Create a group with description
    Given I am authenticated as "alice"
    And a create group request with name "Chess Club"
    And the create group description is "For chess enthusiasts"
    And the create group join policy is "REQUEST"
    When I submit the create group request
    Then I receive a 201 Created response
    And the group name is "Chess Club"

  Scenario: Create a group without join policy
    Given I am authenticated as "alice"
    And a create group request with name "My Group"
    When I submit the create group request
    Then I receive a 400 Bad Request response

  Scenario: Get group details as member
    Given I am authenticated as "alice"
    And I have created a group named "Ping Pong Club" with join policy "OPEN"
    When I request the group details
    Then I receive a 200 OK response
    And the group name is "Ping Pong Club"
    And the group member count is 1

  Scenario: Get group details as non-member
    Given I am authenticated as "alice"
    And another user has created a group named "Private Club"
    When I request the group details
    Then I receive a 404 Not Found response

  Scenario: Update group as admin
    Given I am authenticated as "alice"
    And I have created a group named "Ping Pong Club" with join policy "OPEN"
    And an update group request with name "Table Tennis Club"
    When I submit the update group request
    Then I receive a 200 OK response
    And the group name is "Table Tennis Club"

  Scenario: Update group join policy as admin
    Given I am authenticated as "alice"
    And I have created a group named "My Club" with join policy "OPEN"
    And an update group request with join policy "INVITATION"
    When I submit the update group request
    Then I receive a 200 OK response
    And the group join policy is "INVITATION"

  Scenario: Update group as non-admin member
    Given I am authenticated as "alice"
    And another user has created a group named "Their Club" with join policy "OPEN" and added me as a member
    And an update group request with name "New Name"
    When I submit the update group request
    Then I receive a 403 Forbidden response

  Scenario: List my groups
    Given I am authenticated as "alice"
    And I have created a group named "Ping Pong Club" with join policy "OPEN"
    And I have created another group named "Chess Club" with join policy "REQUEST"
    When I request my groups
    Then I receive a 200 OK response
    And the group list contains "Ping Pong Club"
    And the group list contains "Chess Club"

  Scenario: List groups when I belong to none
    Given I am authenticated as "alice"
    When I request my groups
    Then I receive a 200 OK response
    And the group list is empty

  Scenario: Update group description only
    Given I am authenticated as "alice"
    And I have created a group named "My Club" with join policy "OPEN"
    And an update group request with description "A club for friends"
    When I submit the update group request
    Then I receive a 200 OK response
    And the group description is "A club for friends"

  Scenario: Create group without authentication
    Given a create group request with name "Ping Pong Club"
    And the create group join policy is "OPEN"
    When I submit the create group request without authentication
    Then I receive a 401 Unauthorized response

  Scenario: List groups without authentication
    When I try to list my groups without authentication
    Then I receive a 401 Unauthorized response

  Scenario: Get group without authentication
    Given a non-existent group id
    When I request the group details without authentication
    Then I receive a 401 Unauthorized response

  Scenario: Create group with blank name
    Given I am authenticated as "alice"
    And a create group request with name ""
    And the create group join policy is "OPEN"
    When I submit the create group request
    Then I receive a 400 Bad Request response

  Scenario: Create group with name that is too long
    Given I am authenticated as "alice"
    And a create group request with name "Group name exceeding the max length limit of one hundred characters by having extra content here abcde"
    And the create group join policy is "OPEN"
    When I submit the create group request
    Then I receive a 400 Bad Request response

  Scenario: Get non-existent group
    Given I am authenticated as "alice"
    And a non-existent group id
    When I request the group details
    Then I receive a 404 Not Found response

  Scenario: Update group as non-member
    Given I am authenticated as "alice"
    And another user has created a group named "Exclusive Club"
    And an update group request with name "Hacked Name"
    When I submit the update group request
    Then I receive a 404 Not Found response

  Scenario: Update group with blank name
    Given I am authenticated as "alice"
    And I have created a group named "My Group" with join policy "OPEN"
    And an update group request with name ""
    When I submit the update group request
    Then I receive a 400 Bad Request response

  Scenario: Update group without authentication
    Given a non-existent group id
    And an update group request with name "New Name"
    When I submit the update group request without authentication
    Then I receive a 401 Unauthorized response

  Scenario: Update non-existent group
    Given I am authenticated as "alice"
    And a non-existent group id
    And an update group request with name "New Name"
    When I submit the update group request
    Then I receive a 404 Not Found response

  Scenario: Update group name that is too long
    Given I am authenticated as "alice"
    And I have created a group named "My Group" with join policy "OPEN"
    And an update group request with name "Group name exceeding the max length limit of one hundred characters by having extra content here abcde"
    When I submit the update group request
    Then I receive a 400 Bad Request response

  Scenario: List groups I belong to as a member
    Given I am authenticated as "alice"
    And another user has created a group named "Their Club" with join policy "OPEN" and added me as a member
    When I request my groups
    Then I receive a 200 OK response
    And the group list contains "Their Club"

  Scenario: Update an archived group
    Given I am authenticated as "alice"
    And I have created a group named "My Group" with join policy "OPEN"
    And I have archived the group
    And an update group request with name "Updated Name"
    When I submit the update group request
    Then I receive a 200 OK response
    And the group name is "Updated Name"
