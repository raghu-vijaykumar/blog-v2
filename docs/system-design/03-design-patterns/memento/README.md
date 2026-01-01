---
sidebar_title: Memento Pattern
---

# Memento Pattern

The Memento design pattern is used to capture and store the current state of an object so that it can be restored later. This is particularly useful in scenarios where an object undergoes several changes, and you might need to revert to a previous state. Unlike the Command design pattern, which records every change and allows undo operations, the Memento pattern saves the state at specific points in time, allowing for simpler rollback mechanisms.

## Concept

A **Memento** is an object that holds the state of another object at a specific moment in time. The Memento pattern involves three key components:

1. **Originator**: The object whose state needs to be saved and restored.
2. **Memento**: The object that stores the state of the Originator.
3. **Caretaker**: The object that keeps track of multiple Mementos, usually to implement undo and redo functionality.

## Implementation

In this example, a bank account is used to illustrate the Memento pattern. The bank account maintains a balance and allows deposits, with each deposit returning a Memento object that captures the account’s balance at that point.

### Code Example: Basic Memento Pattern

```python
class Memento:
    def __init__(self, balance):
        self.balance = balance

class BankAccount:
    def __init__(self, balance=0):
        self.balance = balance

    def deposit(self, amount):
        self.balance += amount
        return Memento(self.balance)

    def restore(self, memento):
        self.balance = memento.balance

    def __str__(self):
        return f'Balance = {self.balance}'

if __name__ == '__main__':
    ba = BankAccount(100)
    m1 = ba.deposit(50)
    m2 = ba.deposit(25)
    print(ba)  # Balance = 175

    ba.restore(m1)
    print(ba)  # Balance = 150

    ba.restore(m2)
    print(ba)  # Balance = 175
```

### Advanced Implementation: Undo/Redo with Memento
To enhance the Memento pattern, the implementation can be extended to include undo and redo functionality. This involves maintaining a list of all Mementos and a pointer to the current state.

```python
class Memento:
    def __init__(self, balance):
        self.balance = balance

class BankAccount:
    def __init__(self, balance=0):
        self.balance = balance
        self.changes = [Memento(self.balance)]
        self.current = 0

    def deposit(self, amount):
        self.balance += amount
        m = Memento(self.balance)
        self.changes.append(m)
        self.current += 1
        return m

    def restore(self, memento):
        if memento:
            self.balance = memento.balance
            self.changes.append(memento)
            self.current = len(self.changes)-1

    def undo(self):
        if self.current > 0:
            self.current -= 1
            m = self.changes[self.current]
            self.balance = m.balance
            return m
        return None

    def redo(self):
        if self.current + 1 < len(self.changes):
            self.current += 1
            m = self.changes[self.current]
            self.balance = m.balance
            return m
        return None

    def __str__(self):
        return f'Balance = {self.balance}'

if __name__ == '__main__':
    ba = BankAccount(100)
    ba.deposit(50)
    ba.deposit(25)
    print(ba)  # Balance = 175

    ba.undo()
    print(f'Undo 1: {ba}')  # Balance = 150
    ba.undo()
    print(f'Undo 2: {ba}')  # Balance = 100
    ba.redo()
    print(f'Redo 1: {ba}')  # Balance = 150
```
## Key Points
- **State Preservation**: Mementos store the state of an object at a particular time, allowing for rollback operations.
- **Immutability**: The state stored in a Memento should be immutable to prevent accidental changes.
- **Undo/Redo**: By maintaining a history of Mementos, you can implement undo and redo functionalities efficiently.
The Memento pattern is a powerful tool for managing state, especially in scenarios where objects undergo frequent changes, and rollback capabilities are required.
