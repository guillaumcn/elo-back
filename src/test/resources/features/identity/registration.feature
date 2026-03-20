Feature: User Registration

  Scenario: Successful registration
    Given no user exists with email "alice@example.com" or username "alice"
    When I register with username "alice", email "alice@example.com", and password "Str0ngP@ss!"
    Then I receive a 201 Created response
    And the response contains a valid JWT token
    And the response contains the user profile with username "alice"

  Scenario: Registration with duplicate username
    Given a user exists with username "taken_user"
    When I register with username "taken_user", email "new@example.com", and password "Str0ngP@ss!"
    Then I receive a 409 Conflict response
    And the error message indicates the username is already taken

  Scenario: Registration with duplicate email
    Given a user exists with email "taken@example.com"
    When I register with username "newuser", email "taken@example.com", and password "Str0ngP@ss!"
    Then I receive a 409 Conflict response
    And the error message indicates the email is already taken

  Scenario: Registration with invalid email
    When I register with username "alice", email "not-an-email", and password "Str0ngP@ss!"
    Then I receive a 400 Bad Request response

  Scenario: Registration with blank username
    When I register with username "", email "alice@example.com", and password "Str0ngP@ss!"
    Then I receive a 400 Bad Request response

  Scenario: Registration with short password
    When I register with username "alice", email "alice@example.com", and password "short"
    Then I receive a 400 Bad Request response
