---
sidebar_title: Prototype Pattern
---
# Prototype Pattern

The prototype design pattern is a creational design pattern that allows an object to be cloned, creating new instances by copying an existing object, known as the prototype. This pattern is particularly useful when creating a new object from scratch is resource-intensive or when an object should be replicated with slight modifications.

## Motivation
In many real-world scenarios, objects are not designed from scratch but are built upon existing designs. The prototype pattern follows this approach by allowing an existing object to be duplicated and then customized without affecting the original object. This is especially useful when the construction of objects is complex or resource-heavy.

## Key Concepts
Prototype: The original object that will be copied.
- Cloning: Creating a new instance by copying the prototype.
- Deep Copy: A type of copy where all attributes of the object are recursively copied, ensuring that the new object does not reference the same attributes as the original.
- Shallow Copy: A type of copy where only the object's references are copied, meaning that both the original and the copied object share the same references.

## Example Implementation in Python
```python
import copy

class Address:
    def __init__(self, street_address, city, country):
        self.country = country
        self.city = city
        self.street_address = street_address

    def __str__(self):
        return f'{self.street_address}, {self.city}, {self.country}'

class Person:
    def __init__(self, name, address):
        self.name = name
        self.address = address

    def __str__(self):
        return f'{self.name} lives at {self.address}'
```

### Usage

Creating a Prototype:
```python
john = Person("John", Address("123 London Road", "London", "UK"))
print(john)
```

Cloning with Deep Copy:
```python
jane = copy.deepcopy(john)
jane.name = "Jane"
jane.address.street_address = "124 London Road"
print(john, jane)
```
Output
```sh
John lives at 123 London Road, London, UK
John lives at 123 London Road, London, UK Jane lives at 124 London Road, London, UK
```
In this example, jane is created as a deep copy of john. This means jane has the same initial properties as john, but changes to jane do not affect john.

Shallow Copy Example (Not Recommended for This Case):
```python
jane = copy.copy(john)
jane.name = "Jane"
jane.address.street_address = "124 London Road"
print(john, jane)
```

Output
```sh
John lives at 124 London Road, London, UK Jane lives at 124 London Road, London, UK
```
## Benefits of the Prototype Pattern
- Efficiency: Reduces the overhead of creating complex objects from scratch.
- Flexibility: Allows the creation of new objects by varying the state of the existing objects.
- Decoupling: Clients are decoupled from the specific classes they are using, as they interact with the prototype instead of directly with the class.