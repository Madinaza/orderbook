Feature: Place order

  Scenario: Authenticated trader places a valid limit order
    Given trader id 1 is authenticated
    When the trader places a BUY LIMIT order for AAPL at 100.5 with quantity 10
    Then the order should be accepted