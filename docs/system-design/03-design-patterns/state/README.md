---
sidebar_title: State Pattern
---

# State Pattern

The State Design Pattern is used to manage the behavior of an object that varies depending on its internal state. The idea behind this pattern is that an object can transition between different states and its behavior will be defined by the current state. This approach is particularly useful in situations where an object's behavior changes in response to events or conditions.

## Key Concepts

**State**: The state defines the behavior of an object in a particular condition. Each state is represented by a separate class, and the transitions between states are explicitly handled by the state classes.

**State Transition**: Objects can move from one state to another based on specific actions or events. The transitions are handled by the state classes, which modify the state of the context object.

**Finite State Machine**: This is a formalized construct to manage states and their transitions. A finite state machine has a finite number of states and the ability to transition between them based on input or events.

## Example: Light Switch

The classic example used to demonstrate the State Design Pattern is a light switch, which can be in one of two states: On or Off. The behavior of the light switch is determined by its current state.

The following Python code demonstrates a basic implementation of the State Design Pattern using a light switch:

```python
from abc import ABC

class Switch:
    def __init__(self):
        self.state = OffState()

    def on(self):
        self.state.on(self)

    def off(self):
        self.state.off(self)


class State(ABC):
    def on(self, switch):
        print('Light is already on')

    def off(self, switch):
        print('Light is already off')


class OnState(State):
    def __init__(self):
        print('Light turned on')

    def off(self, switch):
        print('Turning light off...')
        switch.state = OffState()


class OffState(State):
    def __init__(self):
        print('Light turned off')

    def on(self, switch):
        print('Turning light on...')
        switch.state = OnState()


if __name__ == '__main__':
    sw = Switch()

    sw.on()  # Turning light on...
             # Light turned on

    sw.off()  # Turning light off...
              # Light turned off

    sw.off()  # Light is already off
```

**Explanation**

- **Switch Class**: The Switch class maintains the current state of the switch (OnState or OffState). The state is changed by calling the on and off methods, which delegate the behavior to the current state's methods.
- **State Base Class**: The State class defines the default behavior for the on and off methods. If a state transition is not valid (e.g., turning off the light when it's already off), the base class provides a fallback response.
- **OnState and OffState Classes**: These classes inherit from the State class and define the specific behavior for the light switch when it is in the "on" or "off" state. For example, if the switch is in the OnState, it can transition to the OffState by calling the off method.

**Pros**:

- Clear separation of states and behaviors.
- Easily extendable to add more states without modifying the existing code.
- Encapsulates state-specific behavior and reduces conditional logic.

**Cons**:

- Overhead in creating separate classes for each state, especially for simple scenarios.
- The ceremony of defining explicit states may not be necessary for all use cases.

## Phone Call State Machine

This section outlines the implementation of a state machine to model a phone call. The state machine manages transitions between different states of a phone call based on user actions (triggers).

**States**

The phone call can be in one of the following states:

- OFF_HOOK: The phone is off the hook, ready to make a call.
- CONNECTING: The phone is connecting the call.
- CONNECTED: The call is established.
- ON_HOLD: The call is on hold.
- ON_HOOK: The phone is placed back on the hook after the call.

**Triggers**

Triggers cause transitions between states:

- CALL_DIALED: Initiates the connection.
- HUNG_UP: Ends the call.
- CALL_CONNECTED: Establishes the connection.
- PLACED_ON_HOLD: Puts the call on hold.
- TAKEN_OFF_HOLD: Resumes the call from hold.
- LEFT_MESSAGE: Ends the call by leaving a message.
  **Rules**

A dictionary maps each state to its possible triggers and resulting states:

- OFF_HOOK:
  - CALL_DIALED → CONNECTING
- CONNECTING:
  - HUNG_UP → ON_HOOK
  - CALL_CONNECTED → CONNECTED
- CONNECTED:
  - LEFT_MESSAGE → ON_HOOK
  - HUNG_UP → ON_HOOK
  - PLACED_ON_HOLD → ON_HOLD
- ON_HOLD:
  - TAKEN_OFF_HOLD → CONNECTED
  - HUNG_UP → ON_HOOK

**Execution**

Initialization: The initial state is OFF_HOOK, and the exit state is ON_HOOK.
Simulation Loop:

- Print the current state.
- Display available triggers.
- Allow user input to select a trigger.
- Update the state based on the selected trigger.
- Exit the loop when the state matches the exit state.

```python
from enum import Enum, auto

class State(Enum):
OFF_HOOK = auto()
CONNECTING = auto()
CONNECTED = auto()
ON_HOLD = auto()
ON_HOOK = auto()

class Trigger(Enum):
CALL_DIALED = auto()
HUNG_UP = auto()
CALL_CONNECTED = auto()
PLACED_ON_HOLD = auto()
TAKEN_OFF_HOLD = auto()
LEFT_MESSAGE = auto()

if **name** == '**main**':
rules = {
State.OFF_HOOK: [
(Trigger.CALL_DIALED, State.CONNECTING)
],
State.CONNECTING: [
(Trigger.HUNG_UP, State.ON_HOOK),
(Trigger.CALL_CONNECTED, State.CONNECTED)
],
State.CONNECTED: [
(Trigger.LEFT_MESSAGE, State.ON_HOOK),
(Trigger.HUNG_UP, State.ON_HOOK),
(Trigger.PLACED_ON_HOLD, State.ON_HOLD)
],
State.ON_HOLD: [
(Trigger.TAKEN_OFF_HOLD, State.CONNECTED),
(Trigger.HUNG_UP, State.ON_HOOK)
]
}

    state = State.OFF_HOOK
    exit_state = State.ON_HOOK

    while state != exit_state:
        print(f'The phone is currently {state}')

        for i in range(len(rules[state])):
            t = rules[state][i][0]
            print(f'{i}: {t}')

        idx = int(input('Select a trigger:'))
        s = rules[state][idx][1]
        state = s

    print('We are done using the phone.')
```

## Combination Lock State Machine

This section demonstrates an alternative state machine implementation for a combination lock using a switch-based approach, similar to switch statements in other languages.

**States**

The combination lock can be in one of the following states:

- LOCKED: The lock is locked by default.
- FAILED: Incorrect code entered.
- UNLOCKED: The lock is successfully unlocked.
  **Execution**

- Initialization: The starting state is LOCKED, and the entry code is empty.
  Simulation Loop:
- LOCKED: Accept user input to enter the code. Transition to UNLOCKED if the correct code is entered, or to FAILED if incorrect.
- FAILED: Print failure message, reset the entry, and transition back to LOCKED.
- UNLOCKED: Print success message and exit the loop.

```python
from enum import Enum, auto

class State(Enum):
LOCKED = auto()
FAILED = auto()
UNLOCKED = auto()

if **name** == '**main**':
code = '1234'
state = State.LOCKED
entry = ''

    while True:
        if state == State.LOCKED:
            entry += input(entry)

            if entry == code:
                state = State.UNLOCKED

            if not code.startswith(entry):
                state = State.FAILED
        elif state == State.FAILED:
            print('\nFAILED')
            entry = ''
            state = State.LOCKED
        elif state == State.UNLOCKED:
            print('\nUNLOCKED')
            break
```

## Summary

The phone call state machine and the combination lock state machine illustrate two approaches to modeling state-based systems. The phone call example uses a dictionary-based approach to manage state transitions, while the combination lock example demonstrates a simple switch-based approach using conditional statements. Both methods provide ways to manage and simulate state transitions effectively.
