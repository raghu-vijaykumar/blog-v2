---
sidebar_title: Singleton Pattern
---
# Singleton Pattern

The singleton design pattern ensures that a class has only one instance and provides a global point of access to that instance. This pattern is often used when exactly one object is needed to coordinate actions across the system, such as when managing shared resources like databases or configuration settings.

## Motivation
The singleton pattern is motivated by scenarios where certain classes should only be instantiated once. Common examples include:

- Database Connections: You typically only want one instance of a database connection throughout the lifecycle of an application to avoid redundant connections and ensure consistent access to the data.
- Object Factories: An object factory is often stateless and doesn't require multiple instances, making a singleton a natural choice.

The pattern is often criticized because its improper use can lead to design issues, such as hidden dependencies and difficulties in testing. However, when used appropriately, it can be a powerful tool for managing global state.

## Key Concepts
Single Instance: The singleton pattern restricts the instantiation of a class to one "single" instance.
- Global Access: Provides a global point of access to that instance.
- Lazy Instantiation: The singleton instance is created only when it is actually needed, rather than at the time of application startup.

## Example Implementation in Python

```python
import random

class Database:
    initialized = False

    def __init__(self):
        # Initialization logic (e.g., loading a database) would go here.
        pass

    _instance = None

    def __new__(cls, *args, **kwargs):
        if not cls._instance:
            cls._instance = super(Database, cls).__new__(cls, *args, **kwargs)
        return cls._instance
```

### Usage

```python
# Creating the Singleton Instance
database = Database()

# Verifying Singleton Behavior
if __name__ == '__main__':
    d1 = Database()
    d2 = Database()

    print(d1 == d2)  # True, as both d1 and d2 refer to the same instance
    print(database == d1)  # True, database and d1 refer to the same instance
```

- Overriding __new__: The __new__ method is overridden to control the object allocation. If an instance already exists (_instance is not None), the existing instance is returned. If no instance exists, a new one is created and assigned to _instance.
- Initialization (__init__): The __init__ method is called every time an instance is created or accessed. In a singleton, this can cause issues if initialization logic (e.g., loading resources) is placed here. To avoid repeated initialization, consider adding a guard clause to prevent the execution of initialization logic if the instance is already initialized.


### Potential Issues
- Repeated Initialization: Even though the singleton ensures that only one instance of the class exists, the __init__ method might still be called multiple times. This can be problematic if __init__ performs expensive operations, like loading a database. One way to handle this is by adding an initialization flag.
- Testing: Singletons can make unit testing challenging, as they introduce hidden dependencies. In testing, you may need to reset or mock the singleton instance to ensure tests are isolated from each other.

## Benefits of the Singleton Pattern
- Controlled Access: Ensures a class has only one instance, which is useful for resources like configuration settings or loggers.
- Global State: Provides a global point of access to the instance, making it easy to share the instance across different parts of the application.


## Singleton Using a Decorator
A decorator can be used to implement the Singleton pattern by controlling the instantiation of a class and ensuring that only one instance of the class is ever created.

Code Example:

```python
def singleton(class_):
    instances = {}

    def get_instance(*args, **kwargs):
        if class_ not in instances:
            instances[class_] = class_(*args, **kwargs)
        return instances[class_]

    return get_instance


@singleton
class Database:
    def __init__(self):
        print('Loading database')


if __name__ == '__main__':
    d1 = Database()
    d2 = Database()
    print(d1 == d2)  # True, indicating that d1 and d2 are the same instance
```
- The singleton decorator keeps a dictionary of instances.
- When a class is instantiated, the decorator checks if an instance of the class already exists. If not, it creates one; otherwise, it returns the existing instance.
- The initializer is only called once, preventing multiple initializations.

## Singleton Using a Metaclass
A metaclass can also be used to create a Singleton. The metaclass ensures that only one instance of the class is created, similar to the decorator approach.

Code Example:

```python
class Singleton(type):
    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]


class Database(metaclass=Singleton):
    def __init__(self):
        print('Loading database')


if __name__ == '__main__':
    d1 = Database()
    d2 = Database()
    print(d1 == d2)  # True, indicating that d1 and d2 are the same instance
```

- The Singleton metaclass overrides the __call__ method, which is responsible for object creation.
- It checks if an instance of the class exists; if not, it creates one using the super() function.
- This approach is similar to the decorator but leverages Python's metaclass capabilities.

## Monostate (Borg) Singleton Pattern
The Monostate pattern is a variation of the Singleton pattern where all instances of the class share the same state. Unlike the traditional Singleton, it allows multiple instances but with shared state.

Code Example:
```python
class CEO:
    __shared_state = {
        'name': 'Steve',
        'age': 55
    }

    def __init__(self):
        self.__dict__ = self.__shared_state

    def __str__(self):
        return f'{self.name} is {self.age} years old'


class Monostate:
    _shared_state = {}

    def __new__(cls, *args, **kwargs):
        obj = super(Monostate, cls).__new__(cls, *args, **kwargs)
        obj.__dict__ = cls._shared_state
        return obj


class CFO(Monostate):
    def __init__(self):
        self.name = ''
        self.money_managed = 0

    def __str__(self):
        return f'{self.name} manages ${self.money_managed}bn'


if __name__ == '__main__':
    ceo1 = CEO()
    print(ceo1)

    ceo1.age = 66
    ceo2 = CEO()
    ceo2.age = 77
    print(ceo1)
    print(ceo2)

    ceo2.name = 'Tim'
    ceo3 = CEO()
    print(ceo1, ceo2, ceo3)

    cfo1 = CFO()
    cfo1.name = 'Sheryl'
    cfo1.money_managed = 1
    print(cfo1)

    cfo2 = CFO()
    cfo2.name = 'Ruth'
    cfo2.money_managed = 10
    print(cfo1, cfo2, sep='\n')
```
- In the Monostate pattern, the __shared_state or _shared_state dictionary is used to store the state of the class.
- Each instance shares the same state by setting self.__dict__ to reference this shared state.
- Changes made through any instance will affect all other instances since they all reference the same state.

## Singleton Testing

The Singleton pattern ensures that a class has only one instance and provides a global point of access to that instance. In this example, the Singleton is implemented using a metaclass.
```python
class Singleton(type):
    _instances = {}

    def __call__(cls, *args, **kwargs):
        if cls not in cls._instances:
            cls._instances[cls] = super(Singleton, cls).__call__(*args, **kwargs)
        return cls._instances[cls]
```
Here, the Singleton metaclass keeps a dictionary of instances, ensuring that only one instance of a class exists. If an instance already exists, it returns the existing instance instead of creating a new one.

**Database Class**: The Database class uses the Singleton pattern to ensure that only one instance of the database is loaded into memory.
```python
class Database(metaclass=Singleton):
    def __init__(self):
        self.population = {}
        f = open('capitals.txt', 'r')
        lines = f.readlines()
        for i in range(0, len(lines), 2):
            self.population[lines[i].strip()] = int(lines[i + 1].strip())
        f.close()
```
The Database class reads from a file named capitals.txt to load population data for various cities.

**Singleton Record Finder**: The SingletonRecordFinder class uses the Database singleton to calculate the total population of a list of cities.

```python
class SingletonRecordFinder:
    def total_population(self, cities):
        result = 0
        for c in cities:
            result += Database().population[c]
        return result
```
This approach tightly couples the record finder to the singleton database, which can make testing difficult.

**Configurable Record Finder**: To improve testability, the ConfigurableRecordFinder allows dependency injection, enabling the use of different databases for testing.

```python
class ConfigurableRecordFinder:
    def __init__(self, db):
        self.db = db

    def total_population(self, cities):
        result = 0
        for c in cities:
            result += self.db.population[c]
        return result
```

This class takes a database object as an argument, making it possible to inject a dummy database during testing.

**Dummy Database for Testing**: The DummyDatabase class is a mock database used for unit testing.
```python
class DummyDatabase:
    population = {
        'alpha': 1,
        'beta': 2,
        'gamma': 3
    }

    def get_population(self, name):
        return self.population[name]
```

This class provides predictable, hardcoded population data to facilitate reliable testing.

**Unit Tests**: The following unit tests ensure the Singleton behavior and verify the functionality of both the SingletonRecordFinder and ConfigurableRecordFinder.

```python
import unittest

class SingletonTests(unittest.TestCase):
    def test_is_singleton(self):
        db = Database()
        db2 = Database()
        self.assertEqual(db, db2)

    def test_singleton_total_population(self):
        """ This tests on a live database :( """
        rf = SingletonRecordFinder()
        names = ['Seoul', 'Mexico City']
        tp = rf.total_population(names)
        self.assertEqual(tp, 17500000 + 17400000)  # what if these change?

    ddb = DummyDatabase()

    def test_dependent_total_population(self):
        crf = ConfigurableRecordFinder(self.ddb)
        self.assertEqual(
            crf.total_population(['alpha', 'beta']),
            3
        )

if __name__ == '__main__':
    unittest.main()
```
- test_is_singleton: Verifies that only one instance of the Database class exists.
- test_singleton_total_population: Tests the total population calculation using the SingletonRecordFinder with live data.
- test_dependent_total_population: Tests the total population calculation using the ConfigurableRecordFinder with a dummy database.

These tests demonstrate the importance of decoupling code from singletons to allow for better testability.