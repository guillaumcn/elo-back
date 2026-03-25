Feature: User Profile Management

  Scenario: Get own profile
    Given I am authenticated as "alice"
    When I request my profile
    Then I receive a 200 OK response
    And the profile username is "alice"

  Scenario: Update own profile
    Given I am authenticated as "alice"
    And a profile update request with username "alice_updated"
    And the profile update bio is "Hello!"
    When I submit the profile update
    Then I receive a 200 OK response
    And the profile username is "alice_updated"

  Scenario: Update username to taken value
    Given a user exists with username "bob"
    And I am authenticated as "alice"
    And a profile update request with username "bob"
    When I submit the profile update
    Then I receive a 409 Conflict response

  Scenario: View another user's public profile
    Given I am authenticated as "alice"
    When I request the public profile of "bob"
    Then I receive a 200 OK response
    And the public profile username is "bob"

  Scenario: View non-existent user's profile
    Given I am authenticated as "alice"
    When I request the public profile of "nobody"
    Then I receive a 404 Not Found response

  Scenario: Delete own account
    Given I am authenticated as "alice"
    When I delete my account
    Then I receive a 204 No Content response
    And I can no longer authenticate as "alice"

  Scenario: Deleted account is anonymized
    Given I am authenticated as "alice"
    When I delete my account
    Then the account data is anonymized
