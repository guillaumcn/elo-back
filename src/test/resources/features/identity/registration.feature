Feature: User Registration

  Scenario: Successful registration
    Given no user exists with email "alice@example.com" or username "alice"
    And a registration request with username "alice"
    And the registration email is "alice@example.com"
    And the registration password is "Str0ngP@ss!"
    When I submit the registration request
    Then I receive a 201 Created response
    And the response contains a valid JWT token
    And the response contains the user profile with username "alice"

  Scenario: Registration with duplicate username
    Given a user exists with username "taken_user"
    And a registration request with username "taken_user"
    And the registration email is "new@example.com"
    And the registration password is "Str0ngP@ss!"
    When I submit the registration request
    Then I receive a 409 Conflict response
    And the error message indicates the username is already taken

  Scenario: Registration with duplicate email
    Given a user exists with email "taken@example.com"
    And a registration request with username "newuser"
    And the registration email is "taken@example.com"
    And the registration password is "Str0ngP@ss!"
    When I submit the registration request
    Then I receive a 409 Conflict response
    And the error message indicates the email is already taken

  Scenario: Registration with invalid email
    Given a registration request with username "alice"
    And the registration email is "not-an-email"
    And the registration password is "Str0ngP@ss!"
    When I submit the registration request
    Then I receive a 400 Bad Request response

  Scenario: Registration with blank username
    Given a registration request with username ""
    And the registration email is "alice@example.com"
    And the registration password is "Str0ngP@ss!"
    When I submit the registration request
    Then I receive a 400 Bad Request response

  Scenario: Registration with short password
    Given a registration request with username "alice"
    And the registration email is "alice@example.com"
    And the registration password is "short"
    When I submit the registration request
    Then I receive a 400 Bad Request response

  Scenario: Registration with blank password
    Given a registration request with username "alice"
    And the registration email is "alice@example.com"
    And the registration password is ""
    When I submit the registration request
    Then I receive a 400 Bad Request response

  Scenario: Registration with username that is too short
    Given a registration request with username "ab"
    And the registration email is "alice@example.com"
    And the registration password is "Str0ngP@ss!"
    When I submit the registration request
    Then I receive a 400 Bad Request response

  Scenario: Registration with username that is too long
    Given a registration request with username "this_username_is_way_too_long_and_exceeds_fifty_chars_"
    And the registration email is "alice@example.com"
    And the registration password is "Str0ngP@ss!"
    When I submit the registration request
    Then I receive a 400 Bad Request response
