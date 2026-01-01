---
sidebar_title: Template Method Pattern
---

# Template Method Pattern

The **Template Method Design Pattern** is a behavioral design pattern that defines the skeleton of an algorithm in a base class, allowing subclasses to override specific steps of the algorithm without changing its structure. This pattern uses **inheritance** to achieve its goal, contrasting with the **Strategy Design Pattern** which uses **composition**. The key idea is to define a method in a base class that calls several other methods (which might be abstract), and the subclasses implement the specific details.

## Key Concepts

- **Base Class**: Contains the template method, which is the skeleton of the algorithm. This method might depend on several other methods that are abstract or implemented in subclasses.
- **Subclasses**: They provide the concrete implementation for the abstract methods defined in the base class. Subclasses fill in the blanks defined by the template method.
- **Template Method**: This method defines the steps of the algorithm. Subclasses will inherit this method and provide the specific implementation of the missing steps by overriding the abstract methods.

## Differences from Strategy Pattern

- **Strategy Pattern**: Uses composition where the algorithm expects a strategy to be passed in as a parameter, and this strategy conforms to an interface with defined methods.
- **Template Method Pattern**: Uses inheritance, where the base class defines the high-level steps of the algorithm (template method), and subclasses implement the specifics by overriding abstract methods.

## Example Scenario: Game Simulation

The following example demonstrates the **Template Method Pattern** by modeling a game simulation. The base class, `Game`, defines a generic skeleton for running a game, and the subclass `Chess` implements the specifics for a chess game simulation.

## Code Explanation

**Abstract Base Class: Game**

```python
from abc import ABC

class Game(ABC):
    def __init__(self, number_of_players):
        self.number_of_players = number_of_players
        self.current_player = 0

    def run(self):
        self.start()
        while not self.have_winner:
            self.take_turn()
        print(f'Player {self.winning_player} wins!')

    def start(self): pass

    @property
    def have_winner(self): pass

    def take_turn(self): pass

    @property
    def winning_player(self): pass
```

- **Initialization**: The Game class is initialized with a number of players and tracks the current player.
- **Template Method**: The `run()` method defines the skeleton of the game logic:
  - Start the game using `self.start()`.
  - Continue taking turns while there is no winner (`self.have_winner`).
  - When a winner is determined, print the winning player (`self.winning_player`).
- **Abstract Methods**: The methods `start()`, `have_winner`, `take_turn()`, and `winning_player` are defined as placeholders (no implementation) to be overridden by subclasses.

**Concrete Subclass: Chess**

```python
class Chess(Game):
    def __init__(self):
        super().__init__(2)  # Chess is a game for two players
        self.max_turns = 10
        self.turn = 1

    def start(self):
        print(f'Starting a game of chess with {self.number_of_players} players.')

    @property
    def have_winner(self):
        return self.turn == self.max_turns

    def take_turn(self):
        print(f'Turn {self.turn} taken by player {self.current_player}')
        self.turn += 1
        self.current_player = 1 - self.current_player

    @property
    def winning_player(self):
        return self.current_player
```
- **Chess-Specific Logic**: The Chess class overrides the abstract methods to provide specific logic for a chess game simulation:
- **Initialization**: Sets the number of players to 2, and defines max_turns and turn variables.
  - `start()`: Prints a message to start the chess game.
  - `have_winner`: Determines when the game ends (after 10 turns in this case).
  - `take_turn()`: Simulates players taking turns and switches between player 0 and player 1.
  - `winning_player`: Returns the index of the winning player.

```python
if __name__ == '__main__':
    chess = Chess()
    chess.run()
```
**Execution**: When the chess game is executed using chess.run(), the template method defined in the Game class is called. This method uses the specific implementations provided in the Chess class to simulate the chess game.
**Output Example**
```sh
Starting a game of chess with 2 players.
Turn 1 taken by player 0
Turn 2 taken by player 1
Turn 3 taken by player 0
Turn 4 taken by player 1
Turn 5 taken by player 0
Turn 6 taken by player 1
Turn 7 taken by player 0
Turn 8 taken by player 1
Turn 9 taken by player 0
Turn 10 taken by player 1
Player 1 wins!
```
## Conclusion
The Template Method Pattern is useful when you want to define the structure of an algorithm once and allow subclasses to implement the specifics. It enables code reuse and flexibility in designing algorithms that share the same structure but have different details in their implementation.
