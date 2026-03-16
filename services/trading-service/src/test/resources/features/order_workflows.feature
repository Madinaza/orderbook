Feature: Order workflows

  Scenario: Buy limit order crosses exact resting sell price
    Given a resting SELL order for AAPL at 100.00 quantity 5
    When trader 1 places a BUY LIMIT order for AAPL at 100.00 quantity 5
    Then one trade should be created at 100.00 for quantity 5
    And the incoming order status should be FILLED

  Scenario: Buy limit order does not match when prices do not cross
    Given a resting SELL order for AAPL at 101.00 quantity 5
    When trader 1 places a BUY LIMIT order for AAPL at 100.00 quantity 5
    Then no trade should be created
    And the incoming order status should be NEW

  Scenario: Partially filled order can be replaced after execution
    Given a resting SELL order for AAPL at 100.00 quantity 4
    When trader 1 places a BUY LIMIT order for AAPL at 100.00 quantity 10
    And trader 1 replaces the order price to 101.00 and total quantity to 12
    Then the replaced order should have open quantity 8
    And the incoming order status should be PARTIALLY_FILLED

  Scenario: Market order remains partially filled when liquidity is insufficient
    Given a resting SELL order for AAPL at 100.00 quantity 3
    When trader 1 places a BUY MARKET order for AAPL quantity 10
    Then one trade should be created at 100.00 for quantity 3
    And the incoming order status should be PARTIALLY_FILLED