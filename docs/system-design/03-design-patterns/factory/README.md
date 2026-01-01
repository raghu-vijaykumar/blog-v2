---
sidebar_title: Factory Pattern
---

# Factory Pattern
The Factory Design Pattern is a creational design pattern that provides an interface for creating objects in a superclass but allows subclasses to alter the type of objects that will be created. This pattern is particularly useful when the exact type of the object to be created is determined at runtime or when the creation process is complex.
## Factory Method and Factory Class Implementation

The Factory Method and the Factory Class design patterns, which are essential for creating objects in a way that is both flexible and adheres to object-oriented principles like the Single Responsibility Principle and Open/Closed Principle.

The discussion begins by introducing a simple Point class and evolves into explaining how to initialize points using both Cartesian and Polar coordinates while maintaining clean and understandable code.

### Factory Method Pattern

The Factory Method is a design pattern that provides an alternative to using constructors (initializers) directly for object creation. It allows for the creation of objects without specifying the exact class of the object that will be created.

#### Problem Statement:
Initially, the Point class is designed to accept Cartesian coordinates (x, y). However, if we want to add support for polar coordinates (rho, theta), we encounter issues:

- Redefining the constructor for different coordinate systems isn’t possible.
- Expanding the constructor to handle multiple coordinate systems can lead to cluttered and complex code, breaking the Single Responsibility Principle.

#### Solution:
Instead of overloading or expanding the constructor, we can create factory methods:
- Factory methods are static methods that handle the creation of objects with different initialization requirements.
- These methods clearly indicate the type of initialization (Cartesian or Polar) and keep the initialization logic encapsulated.

```python
from enum import Enum
from math import *

class CoordinateSystem(Enum):
    CARTESIAN = 1
    POLAR = 2

class Point:
    def __init__(self, a, b, system=CoordinateSystem.CARTESIAN):
        if system == CoordinateSystem.CARTESIAN:
            self.x = a
            self.y = b
        elif system == CoordinateSystem.POLAR:
            self.x = a * sin(b)
            self.y = a * cos(b)

    def __str__(self):
        return f'x: {self.x}, y: {self.y}'

    @staticmethod
    def new_cartesian_point(x, y):
        return Point(x, y)

    @staticmethod
    def new_polar_point(rho, theta):
        return Point(rho * sin(theta), rho * cos(theta))
```
In this implementation: new_cartesian_point(x, y) and new_polar_point(rho, theta) are static factory methods that create a Point object based on the coordinate system.

### Factory Class Pattern
A Factory Class centralizes the creation logic of related objects. When a class starts accumulating too many factory methods, it's often a good idea to move these methods into a separate class.

#### Problem Statement
As the number of factory methods grows, the Point class might become cluttered. This violates the Single Responsibility Principle as the class is handling both the creation and representation of points.

#### Solution
Extract the factory methods into a separate PointFactory class:

- This class will be responsible for the creation of Point objects.
- This separation ensures that the Point class only focuses on representing a point, while the factory handles object creation.

```python
class PointFactory:
    @staticmethod
    def new_cartesian_point(x, y):
        return Point(x, y)

    @staticmethod
    def new_polar_point(rho, theta):
        return Point(rho * sin(theta), rho * cos(theta))
```

In the PointFactory class: new_cartesian_point(x, y) and new_polar_point(rho, theta) methods are moved here, separating concerns.

#### Usage

```python
if __name__ == '__main__':
    p1 = Point(2, 3, CoordinateSystem.CARTESIAN)
    p2 = PointFactory.new_cartesian_point(1, 2)
    p3 = Point.Factory.new_cartesian_point(5, 6)  # Using nested factory class
    p4 = Point.factory.new_cartesian_point(7, 8)  # Singleton factory instance
    print(p1, p2, p3, p4)
```

In this example:

- PointFactory.new_cartesian_point(1, 2) creates a Cartesian point using the factory class.
- Point.factory.new_cartesian_point(7, 8) demonstrates using a singleton factory instance.

## Abstract Factory Design Pattern Implementation

The abstract factory design pattern allows for the creation of families of related objects without specifying their concrete classes. This implementation demonstrates how to use abstract factories to create different types of drinks (e.g., tea and coffee) using a vending machine scenario.

### Step-by-Step Implementation

- **HotDrink Abstract Base Class**: An abstract base class HotDrink is defined with an abstract method consume. Concrete drink classes (e.g., Tea, Coffee) inherit from this base class and implement the consume method.
- **Concrete Drink Classes**: Two concrete classes, Tea and Coffee, inherit from HotDrink and provide specific implementations of the consume method.
- **HotDrinkFactory Abstract Base Class**: An abstract base class HotDrinkFactory is defined with a method prepare. This method is meant to be overridden by concrete factory classes to prepare specific types of drinks.
- **Concrete Factory Classes**: Two concrete factory classes, TeaFactory and CoffeeFactory, inherit from HotDrinkFactory. These classes implement the prepare method to create instances of Tea and Coffee, respectively.
- **HotDrinkMachine Class**: The HotDrinkMachine class simulates a vending machine that can prepare different types of drinks. It uses an enumeration AvailableDrink to list available drink types. The machine maintains a list of factory instances for each drink type.
- **Drink Preparation**: The make_drink method in HotDrinkMachine allows the user to select a drink type and specify the amount. The corresponding factory prepares the drink, and the consume method is called to simulate drinking it.

```python
from abc import ABC
from enum import Enum, auto

# Abstract base class for drinks
class HotDrink(ABC):
    def consume(self):
        pass

# Concrete drink classes
class Tea(HotDrink):
    def consume(self):
        print('This tea is nice but I\'d prefer it with milk')

class Coffee(HotDrink):
    def consume(self):
        print('This coffee is delicious')

# Abstract base class for drink factories
class HotDrinkFactory(ABC):
    def prepare(self, amount):
        pass

# Concrete factory classes
class TeaFactory(HotDrinkFactory):
    def prepare(self, amount):
        print(f'Put in tea bag, boil water, pour {amount}ml, enjoy!')
        return Tea()

class CoffeeFactory(HotDrinkFactory):
    def prepare(self, amount):
        print(f'Grind some beans, boil water, pour {amount}ml, enjoy!')
        return Coffee()

# Vending machine class for preparing drinks
class HotDrinkMachine:
    class AvailableDrink(Enum):  # Lists available drinks
        COFFEE = auto()
        TEA = auto()

    factories = []
    initialized = False

    def __init__(self):
        if not self.initialized:
            self.initialized = True
            for d in self.AvailableDrink:
                name = d.name[0] + d.name[1:].lower()
                factory_name = name + 'Factory'
                factory_instance = eval(factory_name)()
                self.factories.append((name, factory_instance))

    def make_drink(self):
        print('Available drinks:')
        for f in self.factories:
            print(f[0])

        s = input(f'Please pick drink (0-{len(self.factories)-1}): ')
        idx = int(s)
        s = input(f'Specify amount: ')
        amount = int(s)
        return self.factories[idx][1].prepare(amount)

# Standalone function to prepare drinks
def make_drink(type):
    if type == 'tea':
        return TeaFactory().prepare(200)
    elif type == 'coffee':
        return CoffeeFactory().prepare(50)
    else:
        return None

# Main function to demonstrate the vending machine
if __name__ == '__main__':
    hdm = HotDrinkMachine()
    drink = hdm.make_drink()
    drink.consume()
```

### Key Concepts
- Abstract Base Classes (ABC): Used to define a common interface for a group of related objects.
- Concrete Implementations: Specific classes that implement the abstract methods defined in the base classes.
- Factory Pattern: A design pattern used to create objects without specifying the exact class of the object that will be created.
- Duck Typing: Python's dynamic typing feature where the type of an object is determined by its behavior (methods and properties) rather than its inheritance from a specific class.

This pattern is particularly useful in scenarios where you need to create families of related objects and want to ensure that the objects are created in a consistent manner.