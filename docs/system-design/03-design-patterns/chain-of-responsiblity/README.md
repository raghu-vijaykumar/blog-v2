---
sidebar_title: Chain of Responsibility Pattern
---

# Chain of Responsibility 
The Chain of Responsibility design pattern is used to pass a request along a chain of potential handlers, allowing each handler to either process the request or pass it to the next handler in the chain. This pattern is particularly useful in scenarios where multiple components might handle an action, and the exact handler is determined dynamically at runtime.

## Motivation
Consider a scenario involving a company's unethical behavior. Depending on the severity of the behavior, responsibility may shift from an individual employee to a manager or even the CEO. The Chain of Responsibility allows handling such a situation by escalating the responsibility up the hierarchy based on the nature of the event.

Similarly, in user interfaces or games, an event, such as a click on a button, can be handled by the button itself, or, if not, passed to a container element (e.g., group box or window) for handling. This illustrates how this pattern can delegate responsibility through a hierarchy.

## Key Concepts
- **Chained Handlers**: Each handler in the chain can either process the request or pass it to the next handler.
- **Command Processing**: The pattern is suitable for scenarios where multiple processing steps need to be applied, often dynamically.
- **Default Handling**: If no handler in the chain can process the request, a default handling method can be provided.
- **Early Termination**: A handler in the chain can terminate further processing if necessary, halting the propagation of the event or request.
## Practical Example
In a multiplayer game, a creature's attributes such as attack and defense values can be modified by various game elements, such as picking up a sword or consuming a potion. By implementing this scenario using the Chain of Responsibility, multiple modifiers can be applied to a creature sequentially, where each modifier either processes the request (e.g., boosting attack) or passes it to the next modifier in the chain.

### Code Implementation
```python
class Creature:
    def __init__(self, name, attack, defense):
        self.defense = defense
        self.attack = attack
        self.name = name

    def __str__(self):
        return f'{self.name} ({self.attack}/{self.defense})'


class CreatureModifier:
    def __init__(self, creature):
        self.creature = creature
        self.next_modifier = None

    def add_modifier(self, modifier):
        if self.next_modifier:
            self.next_modifier.add_modifier(modifier)
        else:
            self.next_modifier = modifier

    def handle(self):
        if self.next_modifier:
            self.next_modifier.handle()


class NoBonusesModifier(CreatureModifier):
    def handle(self):
        print('No bonuses for you!')


class DoubleAttackModifier(CreatureModifier):
    def handle(self):
        print(f'Doubling {self.creature.name}''s attack')
        self.creature.attack *= 2
        super().handle()


class IncreaseDefenseModifier(CreatureModifier):
    def handle(self):
        if self.creature.attack <= 2:
            print(f'Increasing {self.creature.name}''s defense')
            self.creature.defense += 1
        super().handle()


if __name__ == '__main__':
    goblin = Creature('Goblin', 1, 1)
    print(goblin)

    root = CreatureModifier(goblin)

    root.add_modifier(NoBonusesModifier(goblin))
    root.add_modifier(DoubleAttackModifier(goblin))
    root.add_modifier(DoubleAttackModifier(goblin))
    root.add_modifier(IncreaseDefenseModifier(goblin))

    root.handle()  # apply modifiers
    print(goblin)
```
### Explanation
- **Creature Class**: This class represents a game entity with attack and defense attributes.
- **CreatureModifier Class**: This is a base class for all creature modifiers. It contains logic for adding modifiers and chaining them.
- **Specific Modifiers**:
  - `NoBonusesModifier` prevents any further modifiers from being applied.
  - `DoubleAttackModifier` doubles the creature's attack value.
  - `IncreaseDefenseModifier` increases the creature's defense if its attack is below a certain threshold.
- **Chain Building**
  - The chain is built dynamically by adding modifiers using the add_modifier method. Each modifier is linked to the next, forming a chain where each can process or pass control.

## Event Broker Implementation with Command-Query Separation (CQS) and Observer Pattern

Event broker system that leverages Command-Query Separation (CQS), the Observer design pattern, and modifiers for creatures in a game environment. The event broker architecture allows automatic propagation of changes, where commands and queries are processed through an event-driven model without direct invocations by the user.

## Key Concepts:
- **Event Broker**: Central system for handling events, where various actions or queries are processed by notifying subscribers (listeners).
- **Command-Query Separation (CQS)**: Design principle that distinguishes between commands (requests to change the state) and queries (requests for information that do not alter the state).
- **Observer Design Pattern**: The system allows multiple objects to listen to and respond to events.
## Components
### Event Class
An Event class represents a list of subscribers that can be notified by calling the event. This acts as a foundational component of the event broker:

```python
class Event(list):
    def __call__(self, *args, **kwargs):
        for item in self:
            item(*args, **kwargs)
```
This allows multiple functions to listen to events and execute their logic when an event is triggered.

### Query Class
The Query class represents a request for information. It holds the creature's name, the type of query (e.g., attack or defense), and a default value. Other objects can modify the value field as needed.

```python
class Query:
    def __init__(self, creature_name, what_to_query, default_value):
        self.value = default_value
        self.what_to_query = what_to_query
        self.creature_name = creature_name
```
### Game Class (Event Broker)
The Game class acts as the event broker, managing queries sent by creatures and triggering the appropriate event listeners (modifiers).

```python
class Game:
    def __init__(self):
        self.queries = Event()  # Event to handle queries

    def perform_query(self, sender, query):
        self.queries(sender, query)
```        
### Creature Class
The Creature class represents a game entity with attributes like name, attack, and defense. The attack and defense values can be modified by the event broker and various modifiers.

**Properties**:
- **Attack**: Uses the event broker to query and retrieve the final attack value after applying any active modifiers.
- **Defense**: Uses the event broker to query and retrieve the final defense value after applying any active modifiers.
  
```python
class Creature:
    def __init__(self, game, name, attack, defense):
        self.initial_defense = defense
        self.initial_attack = attack
        self.name = name
        self.game = game

    @property
    def attack(self):
        q = Query(self.name, WhatToQuery.ATTACK, self.initial_attack)
        self.game.perform_query(self, q)
        return q.value

    @property
    def defense(self):
        q = Query(self.name, WhatToQuery.DEFENSE, self.initial_defense)
        self.game.perform_query(self, q)
        return q.value
```
### CreatureModifier (Abstract Base Class)
CreatureModifier is an abstract class that listens to query events and modifies the attack or defense values accordingly. It subscribes to the event broker upon initialization and unsubscribes when its lifetime ends.

```python
class CreatureModifier(ABC):
    def __init__(self, game, creature):
        self.creature = creature
        self.game = game
        self.game.queries.append(self.handle)

    def handle(self, sender, query):
        pass

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        self.game.queries.remove(self.handle)
```
### Modifiers (Concrete Implementations)
Concrete modifiers like DoubleAttackModifier and IncreaseDefenseModifier modify the attack or defense values of creatures based on certain conditions.

**DoubleAttackModifier**: Doubles the creature's attack value if the query is related to the attack.

```python
class DoubleAttackModifier(CreatureModifier):
    def handle(self, sender, query):
        if sender.name == self.creature.name and query.what_to_query == WhatToQuery.ATTACK:
            query.value *= 2
```
**IncreaseDefenseModifier**: Increases the creature's defense value by 3 when the query pertains to defense.

```python
class IncreaseDefenseModifier(CreatureModifier):
    def handle(self, sender, query):
        if sender.name == self.creature.name and query.what_to_query == WhatToQuery.DEFENSE:
            query.value += 3
```
### Enum for Query Types
An enumeration WhatToQuery defines constants for ATTACK and DEFENSE to differentiate between query types.

```python
class WhatToQuery(Enum):
    ATTACK = 1
    DEFENSE = 2
```
### Usage Example
The following code demonstrates the usage of the event broker and the modifiers in action:

```python
if __name__ == '__main__':
    game = Game()
    goblin = Creature(game, 'Strong Goblin', 2, 2)
    print(goblin)  # Strong Goblin (2/2)

    with DoubleAttackModifier(game, goblin):
        print(goblin)  # Strong Goblin (4/2)
        with IncreaseDefenseModifier(game, goblin):
            print(goblin)  # Strong Goblin (4/5)

    print(goblin)  # Strong Goblin (2/2) - Modifiers no longer apply after 'with' scope exits
```
**Output**
```sh
Strong Goblin (2/2)
Strong Goblin (4/2) (double attack modifier applied)
Strong Goblin (4/5) (double attack and defense modifiers applied)
Strong Goblin (2/2) (modifiers no longer applied)
```

This implementation of an event broker with Command-Query Separation (CQS) and the Observer Pattern offers a dynamic way to manage and modify creature attributes in real time. Modifiers are applied via event listeners and can be temporary or permanent, making it a flexible architecture for game development or similar event-driven applications.
