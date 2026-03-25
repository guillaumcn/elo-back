Feature: User Authentication (Login)

  Scenario: Successful login
    Given a registered user with email "alice@example.com" and password "Str0ngP@ss!"
    And a login request with email "alice@example.com"
    And the login password is "Str0ngP@ss!"
    When I submit the login request
    Then I receive a 200 OK response
    And the response contains a valid JWT token

  Scenario: Login with wrong password
    Given a registered user with email "bob@example.com" and password "Str0ngP@ss!"
    And a login request with email "bob@example.com"
    And the login password is "WrongPassword"
    When I submit the login request
    Then I receive a 401 Unauthorized response

  Scenario: Login with non-existent email
    Given a login request with email "unknown@example.com"
    And the login password is "anything123"
    When I submit the login request
    Then I receive a 401 Unauthorized response

  Scenario: Login with deleted account
    Given a user with email "deleted@example.com" has deleted their account
    And a login request with email "deleted@example.com"
    And the login password is "Str0ngP@ss!"
    When I submit the login request
    Then I receive a 401 Unauthorized response

  Scenario: Access protected endpoint without token
    When I access a protected endpoint without a token
    Then I receive a 401 Unauthorized response

  Scenario: Access protected endpoint with expired token
    When I access a protected endpoint with an expired token
    Then I receive a 401 Unauthorized response
