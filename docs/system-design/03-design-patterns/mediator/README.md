---
sidebar_title: Mediator Pattern
---

# Mediator Pattern

The mediator design pattern is used to facilitate communication between different components of a system while keeping these components unaware of each other. This pattern is particularly useful in systems where components frequently join and leave, such as chat rooms or multiplayer online games. By using a central mediator, components can interact without having direct references to each other, thus avoiding issues related to dead or invalid references.

## Key Concepts

- **Mediator**: A central component that manages communication between other components. It prevents direct dependencies between components, thereby reducing complexity and coupling.
- **Participants**: Components that communicate through the mediator. They do not need to be aware of each other directly.

## Chat Room Implementation

A chat room is a classic example of the mediator pattern. In this scenario, the chat room acts as the mediator that handles communication between participants (people) without them needing to know about each other directly.

**Class Definitions**

- **Person Class**:
  - **Attributes**:
    - **name**: The name of the person.
    - **chat_log**: A log to store messages received.
    - **room**: The chat room the person is currently in.
  - **Methods**:
    - **receive(sender, message)**: Receives a message from another person and adds it to the chat log.
    - **say(message)**: Sends a message to the chat room to be broadcasted.
    - **private_message(who, message)**: Sends a private message to another person in the chat room.
- **ChatRoom Class**:
  - **Attributes**:
    - **people**: A list of people currently in the chat room.
  - **Methods**:
    - **broadcast(source, message)**: Sends a message to all people in the room except the source.
    - **join(person)**: Adds a person to the chat room and broadcasts a message announcing their arrival.
    - **message(source, destination, message)**: Sends a private message from the source to a specified destination.

**Example Usage**
Here’s a simple script to demonstrate how the mediator pattern works using the chat room example:

```python
class Person:
    def __init__(self, name):
        self.name = name
        self.chat_log = []
        self.room = None

    def receive(self, sender, message):
        s = f'{sender}: {message}'
        print(f'[{self.name}\'s chat session] {s}')
        self.chat_log.append(s)

    def say(self, message):
        self.room.broadcast(self.name, message)

    def private_message(self, who, message):
        self.room.message(self.name, who, message)


class ChatRoom:
    def __init__(self):
        self.people = []

    def broadcast(self, source, message):
        for p in self.people:
            if p.name != source:
                p.receive(source, message)

    def join(self, person):
        join_msg = f'{person.name} joins the chat'
        self.broadcast('room', join_msg)
        person.room = self
        self.people.append(person)

    def message(self, source, destination, message):
        for p in self.people:
            if p.name == destination:
                p.receive(source, message)


if __name__ == '__main__':
    room = ChatRoom()

    john = Person('John')
    jane = Person('Jane')

    room.join(john)
    room.join(jane)

    john.say('hi room')
    jane.say('oh, hey john')

    simon = Person('Simon')
    room.join(simon)
    simon.say('hi everyone!')

    jane.private_message('Simon', 'glad you could join us!')
```

## Mediator Design Pattern with Events

The mediator design pattern can also be implemented using events, as discussed in the observer pattern. This approach involves using events to allow various components to subscribe and react to changes without direct references between them.

### Key Concepts

- **Event**: A mechanism to hold and invoke a list of functions when something happens. It allows multiple subscribers to be notified about events in a centralized way.
- **Mediator**: A central component that generates events. It allows other components to subscribe to these events and react accordingly.

### Implementation Details

**Event Class**:

- Manages a list of functions (subscribers) and invokes them with provided arguments when an event occurs.

```python
class Event(list):
    def __call__(self, *args, **kwargs):
        for item in self:
            item(*args, **kwargs)
```

**Game Class (Mediator)**:

- Acts as the central mediator and maintains events to which other components can subscribe.
- Provides functionality to fire events.

```python
class Game:
    def __init__(self):
        self.events = Event()

    def fire(self, args):
        self.events(args)
```

**GoalScoredInfo Class**:

- Contains information about a goal scored, including the player who scored and the total goals scored by the player.

```python
class GoalScoredInfo:
    def __init__(self, who_scored, goals_scored):
        self.goals_scored = goals_scored
        self.who_scored = who_scored
```

**Player Class**:

- Represents a player who scores goals.
- Notifies the game (mediator) with goal-scoring information through the event system.

```python
class Player:
    def __init__(self, name, game):
        self.name = name
        self.game = game
        self.goals_scored = 0

    def score(self):
        self.goals_scored += 1
        args = GoalScoredInfo(self.name, self.goals_scored)
        self.game.fire(args)
```

**Coach Class**:

- Represents a coach who subscribes to the game's events.
- Reacts to goal-scoring events based on specific criteria (e.g., congratulates only if goals scored are less than three).

```python
class Coach:
    def __init__(self, game):
        game.events.append(self.celebrate_goal)

    def celebrate_goal(self, args):
        if isinstance(args, GoalScoredInfo) and args.goals_scored < 3:
            print(f'Coach says: well done, {args.who_scored}!')
```

### Example Usage

```python
if __name__ == '__main__':
    game = Game()
    player = Player('Sam', game)
    coach = Coach(game)

    player.score()  # Coach says: well done, Sam!
    player.score()  # Coach says: well done, Sam!
    player.score()  # ignored by coach
```

## Summary

Using events to implement the mediator pattern allows for a flexible and decoupled communication mechanism between components. In this approach, the mediator (Game) manages events, and components (Player and Coach) subscribe to these events to react to changes, such as goal scores in a football game. This setup reduces direct dependencies between components and enhances the system's modularity.
