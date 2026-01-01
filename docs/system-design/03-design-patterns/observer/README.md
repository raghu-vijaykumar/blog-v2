---
sidebar_title: Observer Pattern
---

# Observer Pattern

The Observer pattern is a design pattern in which an object (known as the "subject") maintains a list of dependents ("observers") that are notified of any state changes in the subject, typically through method calls. This pattern is useful when you want to inform other objects about events or state changes without tightly coupling the objects.

## Key Concepts

- **Subject**: The main object that changes state and needs to notify observers when a specific event occurs.
- **Observer**: Objects that watch for events in the subject and are notified when a particular event happens.

## Motivation for the Observer Pattern

- You need to notify other parts of your system about changes to an object’s state.
- These changes might trigger other operations or validations, such as prohibiting certain state changes or triggering actions when a specific event occurs.
- The observer pattern allows you to decouple the subject and the observer, thus promoting loose coupling and better system modularity.

## Implementation

This implementation demonstrates how an event is used to notify observers when a state change occurs.

```python
class Event(list):
    def __call__(self, *args, **kwargs):
        for item in self:
            item(*args, **kwargs)

class Person:
    def __init__(self, name, address):
        self.name = name
        self.address = address
        self.falls_ill = Event()

    def catch_a_cold(self):
        self.falls_ill(self.name, self.address)

def call_doctor(name, address):
    print(f'A doctor has been called to {address}')

if __name__ == '__main__':
    person = Person('Sherlock', '221B Baker St')

    person.falls_ill.append(lambda name, addr: print(f'{name} is ill'))
    person.falls_ill.append(call_doctor)

    person.catch_a_cold()

    # Remove the doctor subscription
    person.falls_ill.remove(call_doctor)
    person.catch_a_cold()
```

## Explanation

**Event class**: Inherits from a list and allows callable objects to be added to the list, acting as subscribers. When an event occurs, all subscribers are called.
**Person class**: Maintains an Event for when the person falls ill and triggers notifications.
**Subscriptions**: Subscribers can be functions like `call_doctor()` or lambdas, which are called when the person catches a cold.

## Property Observers

You can extend the observer pattern to monitor changes in specific properties of a class, known as property observers.

```python
class Event(list):
    def __call__(self, *args, **kwargs):
        for item in self:
            item(*args, **kwargs)

class PropertyObservable:
    def __init__(self):
        self.property_changed = Event()

class Person(PropertyObservable):
    def __init__(self, age=0):
        super().__init__()
        self._age = age

    @property
    def age(self):
        return self._age

    @age.setter
    def age(self, value):
        if self._age == value:
            return
        self._age = value
        self.property_changed('age', value)

class TrafficAuthority:
    def __init__(self, person):
        self.person = person
        person.property_changed.append(self.person_changed)

    def person_changed(self, name, value):
        if name == 'age':
            if value < 16:
                print('Sorry, you still cannot drive')
            else:
                print('Okay, you can drive now')
                self.person.property_changed.remove(self.person_changed)

if __name__ == '__main__':
    p = Person()
    ta = TrafficAuthority(p)
    for age in range(14, 20):
        print(f'Setting age to {age}')
        p.age = age
```

**Explanation**

- **PropertyObservable class**: Adds an Event that tracks changes to specific properties.
- **Property Getters and Setters**: The Person class uses property getters and setters to monitor the age property. When the property changes, an event is triggered, and the observers are notified.
- **TrafficAuthority class**: Observes the age property to determine if a person is old enough to drive. When the condition is met (age ≥ 16), the observer unsubscribes from further notifications.

## Property Observers in Dependent Properties

Property observers in programming are useful for tracking changes to object properties. However, challenges arise when properties depend on one another. This documentation highlights a potential issue and demonstrates an approach for handling dependent properties correctly.

### Problem Description

When one property depends on another, tracking changes can become complex. A common scenario involves having one property (can_vote) depend on another property (age). In this example, can_vote determines whether a person is eligible to vote based on their age (18 or older).

Here is a simplified breakdown:

- A person has an age property.
- A can_vote property depends on age and returns True if age >= 18, otherwise False.
- Property change notifications are sent when the value of age changes, but there’s no direct notification for changes in can_vote.
  **Incorrect Approach**
  One might initially consider sending change notifications for can_vote whenever age changes. This, however, leads to incorrect behavior. For example:

If age changes from 15 to 16, can_vote remains False, but a notification would be sent regardless.
This results in unnecessary notifications, which is not desirable.

**Correct Approach**
To handle this scenario correctly:

- Cache the old value of the dependent property (can_vote).
- Update the primary property (age).
- Compare the old and new values of the dependent property.
- Only send a notification if the value of the dependent property has actually changed.
- This avoids sending unnecessary notifications and ensures that changes are communicated accurately.

### Code Example

Below is a Python implementation of the solution. The code uses an event system to handle property changes and ensures that dependent properties are correctly observed.

```python
class Event(list):
    def __call__(self, *args, **kwargs):
        for item in self:
            item(*args, **kwargs)

class PropertyObservable:
    def __init__(self):
        self.property_changed = Event()

class Person(PropertyObservable):
    def __init__(self, age=0):
        super().__init__()
        self._age = age

    @property
    def can_vote(self):
        return self._age >= 18

    @property
    def age(self):
        return self._age

    @age.setter
    def age(self, value):
        if self._age == value:
            return

        old_can_vote = self.can_vote
        self._age = value
        self.property_changed('age', value)

        # Notify change if can_vote status changed
        if old_can_vote != self.can_vote:
            self.property_changed('can_vote', self.can_vote)

if __name__ == '__main__':
    def person_changed(name, value):
        if name == 'can_vote':
            print(f'Voting status changed to {value}')

    p = Person()
    p.property_changed.append(person_changed)

    for age in range(16, 21):
        print(f'Changing age to {age}')
        p.age = age
```

#### Explanation of Code

- **Event Class**: Acts as a container for event handlers. When an event is triggered, all associated handlers are invoked.
- **PropertyObservable Class**: A base class that provides the property_changed event for derived classes.
- **Person Class**: Extends PropertyObservable and defines age and can_vote properties. The age setter caches the old can_vote value and compares it to the new value after age changes. If there is a difference, a notification is sent for the can_vote property.

## Advantages of the Observer Pattern

- **Loose Coupling**: The subject and observers are loosely coupled; they are not dependent on each other’s implementation details.
- **Reusability**: Observers can be reused across different subjects.
- **Dynamic Subscriptions**: New observers can be added or removed at runtime.

## Considerations and Limitations

- **Performance Overhead**: Notifying a large number of observers can incur performance overhead.
- **Complexity**: Maintaining subscriptions and unsubscribing observers can introduce complexity.
- **Property Dependencies**: If one property depends on another, managing event notifications for such dependent properties can become difficult.
