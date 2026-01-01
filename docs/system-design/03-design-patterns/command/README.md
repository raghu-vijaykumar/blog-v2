---
sidebar_title: Command Pattern
---

# Command Pattern

The command design pattern is used to encapsulate a request as an object, allowing the application to parameterize clients with queues, requests, and operations, and support undoable operations and logging.

## Motivation

In many applications, you may need to record or undo actions taken by users or processes. For instance:
- **Ordinary Statements**: These are "perishable," meaning actions like variable assignments can't be undone or serialized.
- **Logging and Auditing**: You may want to keep track of system actions for auditing purposes, so every action can be serialized and stored in a database.
- **Undo/Redo**: Systems like text editors require undo/redo operations, which can be implemented using the command pattern.

## Key Concept

A **command** is an object that represents an instruction to perform a specific action. This object typically contains all necessary information to execute the action and supports additional features like logging, auditing, and undo/redo.

## Command Pattern Use Cases

1. **Graphical User Interfaces (GUIs)**: Actions like "copy" or "paste" are typically encapsulated in commands, enabling the undo/redo functionality.
2. **Macro Recording**: A sequence of commands can be recorded and played back later.
3. **System Actions Logging**: Commands are logged and stored for auditing purposes, ensuring that actions can be traced back to their origin.

## Implementing Command Design Pattern

The implementation involves the following components:

1. **Command Interface**: An abstract base class defining the `invoke()` and `undo()` methods.
2. **Concrete Commands**: Classes that implement the command interface. They encapsulate specific actions to be performed on a receiver, such as a bank account.
3. **Receiver**: The object that the command operates on, such as a bank account.

### Example: Bank Account Commands

Consider a bank account with the following operations:
- **Deposit**: Adds money to the account.
- **Withdraw**: Deducts money from the account while respecting an overdraft limit.

These operations are encapsulated as commands, which allow them to be executed, logged, and undone.

```python
from abc import ABC
from enum import Enum

class BankAccount:
    OVERDRAFT_LIMIT = -500

    def __init__(self, balance=0):
        self.balance = balance

    def deposit(self, amount):
        self.balance += amount
        print(f'Deposited {amount}, balance = {self.balance}')

    def withdraw(self, amount):
        if self.balance - amount >= BankAccount.OVERDRAFT_LIMIT:
            self.balance -= amount
            print(f'Withdrew {amount}, balance = {self.balance}')
            return True
        return False

    def __str__(self):
        return f'Balance = {self.balance}'
```
#### Command Interface and Bank Account Command
The Command abstract base class defines the invoke() and undo() methods. The BankAccountCommand class is a concrete implementation that can invoke and undo deposit and withdrawal operations.

```python
Copy code
class Command(ABC):
    def invoke(self):
        pass

    def undo(self):
        pass

class BankAccountCommand(Command):
    def __init__(self, account, action, amount):
        self.amount = amount
        self.action = action
        self.account = account
        self.success = None

    class Action(Enum):
        DEPOSIT = 0
        WITHDRAW = 1

    def invoke(self):
        if self.action == self.Action.DEPOSIT:
            self.account.deposit(self.amount)
            self.success = True
        elif self.action == self.Action.WITHDRAW:
            self.success = self.account.withdraw(self.amount)

    def undo(self):
        if not self.success:
            return
        if self.action == self.Action.DEPOSIT:
            self.account.withdraw(self.amount)
        elif self.action == self.Action.WITHDRAW:
            self.account.deposit(self.amount)
```
####  Example Usage
Here is how the command pattern is used to invoke a deposit and then undo the operation.

```python
if __name__ == '__main__':
    ba = BankAccount()
    cmd = BankAccountCommand(ba, BankAccountCommand.Action.DEPOSIT, 100)
    cmd.invoke()
    print('After $100 deposit:', ba)

    cmd.undo()
    print('$100 deposit undone:', ba)

    illegal_cmd = BankAccountCommand(ba, BankAccountCommand.Action.WITHDRAW, 1000)
    illegal_cmd.invoke()
    print('After impossible withdrawal:', ba)
    illegal_cmd.undo()
    print('After undo:', ba)
```
#### Handling Edge Cases
The command pattern must account for failures in invoking operations. For instance, if a withdrawal exceeds the account's overdraft limit, the operation should not succeed, and any subsequent undo should be ignored.

To handle this, the command object tracks whether the operation succeeded and only performs an undo if the operation was successful.

```python
def invoke(self):
    if self.action == self.Action.DEPOSIT:
        self.account.deposit(self.amount)
        self.success = True
    elif self.action == self.Action.WITHDRAW:
        self.success = self.account.withdraw(self.amount)

def undo(self):
    if not self.success:
        return
```


## Composite Command Design Pattern for Bank Account Operations

In this implementation, we explore the Composite Command Design Pattern, also known as a macro, within a bank account management context. This design pattern allows us to group several commands together and treat them as a single command. We also address the additional complexity of handling operations involving multiple bank accounts, such as money transfers. By doing so, we demonstrate how to handle failures and ensure the correct execution and undoing of grouped operations.

### Bank Account Model
We begin by defining a simple BankAccount class that supports deposit and withdrawal operations. Additionally, we introduce the concept of an overdraft limit to prevent accounts from being overdrawn beyond a certain threshold.

```python
class BankAccount:
    OVERDRAFT_LIMIT = -500

    def __init__(self, balance=0):
        self.balance = balance

    def deposit(self, amount):
        self.balance += amount
        print(f'Deposited {amount}, balance = {self.balance}')

    def withdraw(self, amount):
        if self.balance - amount >= BankAccount.OVERDRAFT_LIMIT:
            self.balance -= amount
            print(f'Withdrew {amount}, balance = {self.balance}')
            return True
        return False

    def __str__(self):
        return f'Balance = {self.balance}'
```
### Command Pattern
The Command class provides an abstract interface for executing and undoing operations. The specific commands for deposit and withdrawal are implemented in the `BankAccountCommand` class, which inherits from `Command`. The success of each operation is tracked to enable proper undo functionality.

```python
class Command(ABC):
    def __init__(self):
        self.success = False

    def invoke(self):
        pass

    def undo(self):
        pass

class BankAccountCommand(Command):
    def __init__(self, account, action, amount):
        super().__init__()
        self.amount = amount
        self.action = action
        self.account = account

    class Action(Enum):
        DEPOSIT = 0
        WITHDRAW = 1

    def invoke(self):
        if self.action == self.Action.DEPOSIT:
            self.account.deposit(self.amount)
            self.success = True
        elif self.action == self.Action.WITHDRAW:
            self.success = self.account.withdraw(self.amount)

    def undo(self):
        if not self.success:
            return
        if self.action == self.Action.DEPOSIT:
            self.account.withdraw(self.amount)
        elif self.action == self.Action.WITHDRAW:
            self.account.deposit(self.amount)
```
### Composite Command
The `CompositeBankAccountCommand` class extends both the Command class and the Python list. It allows us to group multiple commands together and treat them as a single operation. It implements invoke and undo methods that execute or undo the grouped commands in the correct sequence.

```python
class CompositeBankAccountCommand(Command, list):
    def __init__(self, items=[]):
        super().__init__()
        for i in items:
            self.append(i)

    def invoke(self):
        for x in self:
            x.invoke()

    def undo(self):
        for x in reversed(self):
            x.undo()
```
### Money Transfer Command
The `MoneyTransferCommand` class extends `CompositeBankAccountCommand` to handle the special case of transferring money between two bank accounts. This command involves a withdrawal from one account and a deposit into another. The invoke method ensures that the transfer only proceeds if the withdrawal succeeds. If the withdrawal fails (e.g., due to insufficient funds), the entire operation is marked as failed, and the deposit is not attempted.

```python
class MoneyTransferCommand(CompositeBankAccountCommand):
    def __init__(self, from_acct, to_acct, amount):
        super().__init__([
            BankAccountCommand(from_acct,
                               BankAccountCommand.Action.WITHDRAW,
                               amount),
            BankAccountCommand(to_acct,
                               BankAccountCommand.Action.DEPOSIT,
                               amount)])

    def invoke(self):
        ok = True
        for cmd in self:
            if ok:
                cmd.invoke()
                ok = cmd.success
            else:
                cmd.success = False
        self.success = ok
```
### Unit Tests
We create unit tests to verify the functionality of the composite commands and the money transfer logic.
- **Composite Deposit Test**: This test demonstrates the composite command by performing two deposits into a single account and then undoing them.
- **Transfer Failure Test**: This test highlights the potential failure of a money transfer when the source account does not have sufficient funds.
- **Better Transfer Test**: This test implements the improved MoneyTransferCommand and ensures that a transfer only proceeds if the withdrawal succeeds.

```python
class TestSuite(unittest.TestCase):
    def test_composite_deposit(self):
        ba = BankAccount()
        deposit1 = BankAccountCommand(ba, BankAccountCommand.Action.DEPOSIT, 1000)
        deposit2 = BankAccountCommand(ba, BankAccountCommand.Action.DEPOSIT, 1000)
        composite = CompositeBankAccountCommand([deposit1, deposit2])
        composite.invoke()
        print(ba)
        composite.undo()
        print(ba)

    def test_transfer_fail(self):
        ba1 = BankAccount(100)
        ba2 = BankAccount()
        amount = 100

        # Withdrawal and deposit commands for transfer
        wc = BankAccountCommand(ba1, BankAccountCommand.Action.WITHDRAW, amount)
        dc = BankAccountCommand(ba2, BankAccountCommand.Action.DEPOSIT, amount)
        transfer = CompositeBankAccountCommand([wc, dc])

        transfer.invoke()
        print(ba1, ba2)
        transfer.undo()
        print(ba1, ba2)

    def test_better_transfer(self):
        ba1 = BankAccount(100)
        ba2 = BankAccount()
        amount = 100

        transfer = MoneyTransferCommand(ba1, ba2, amount)
        transfer.invoke()
        print(ba1, ba2)
        transfer.undo()
        print(ba1, ba2)
```

The Composite Command Design Pattern is a powerful tool for managing complex operations involving multiple related commands. By extending the basic command pattern to support groups of commands, we can ensure that operations like money transfers succeed or fail as a unit, allowing for better consistency and error handling in financial transactions.
