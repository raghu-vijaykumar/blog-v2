---
sidebar_title: Decorator Pattern
---

# Decorator Pattern

The Decorator Pattern is a structural design pattern used to add additional behaviors or responsibilities to objects dynamically. Unlike subclassing, where additional functionality is achieved through inheritance, the decorator pattern allows the augmentation of an object’s behavior without modifying its structure or the need for inheritance. This promotes adherence to the Open/Closed Principle and Single Responsibility Principle in software design.

## Motivation
The main motivation for using the decorator pattern is to add features or functionality to existing objects in a flexible and reusable way. Instead of altering the original object or creating numerous subclasses for every possible combination of features, decorators allow you to "wrap" an object with new functionality dynamically.

## Implementation
General Approach
- Inheritance: One way to augment a class is by inheriting from it and adding new features. However, this approach can become cumbersome if multiple combinations of features are needed, leading to an explosion of subclasses.
- Decorators: A more flexible approach is to use decorators, which are objects that wrap the original object and add new behaviors to it. Decorators can be stacked, allowing for dynamic and combinatorial behavior modification.

### Python-Specific Implementation
In Python, the decorator pattern can be implemented using higher-order functions, where a function wraps another function to extend its behavior. Python also provides special syntax for applying decorators to functions, using the @decorator_name syntax.

```python
import time

def time_it(func):
    def wrapper():
        start = time.time()  # Measure start time
        result = func()  # Call the original function
        end = time.time()  # Measure end time
        print(f'{func.__name__} took {int((end-start)*1000)}ms')  # Print execution time
        return result
    return wrapper

@time_it  # Applying the time_it decorator
def some_op():
    print('Starting op')
    time.sleep(1)  # Simulate a time-consuming operation
    print('We are done')
    return 123

if __name__ == '__main__':
    some_op()
```
#### Explanation
**Decorator Function (time_it)**:
- Purpose: Measures the execution time of the function it decorates.
- How It Works:
  - It defines an inner function (wrapper) that wraps the original function.
  - The wrapper function records the start time, executes the original function, records the end time, and then prints the time taken to execute the function.
  - Finally, it returns the result of the original function.

**Decorated Function (some_op)**:

- A simple function that simulates a time-consuming operation using time.sleep(1).
- The @time_it decorator is applied to this function, meaning that every time some_op is called, it will be wrapped in the timing logic defined in time_it.

**Execution**:
- When some_op is called, the execution time is measured and printed due to the decorator, illustrating how the decorator pattern can be used to augment behavior without modifying the original function.

### Key Points
- Flexible Augmentation: Decorators allow the addition of functionality in a flexible manner without altering the original object or function.
- Reusability: Decorators can be reused across different functions or objects, promoting code reusability.
- Adherence to Design Principles: The decorator pattern supports the Open/Closed Principle by allowing the extension of an object's behavior without modifying its code.

## Classic Implementation of Decorator Pattern in Object-Oriented Programming

The Decorator Pattern is a structural design pattern used to dynamically add behavior to individual objects without affecting the behavior of other objects from the same class. This is achieved by creating decorator classes that wrap the original class, augmenting its functionality. This approach adheres to the Open/Closed Principle by allowing functionality to be extended without modifying existing code.

### Motivation
In object-oriented programming, there often arises a need to extend the functionality of a class without modifying its code. Traditional inheritance can lead to an explosion of subclasses for every combination of features, making the system complex and hard to maintain. The decorator pattern offers a more flexible and reusable solution by allowing the dynamic composition of behaviors.

### Implementation
**Core Components**
Abstract Base Class (Shape):
- The abstract base class provides a common interface for all shapes, ensuring that concrete classes and decorators can be treated uniformly.
Concrete Classes (Circle, Square):
- These classes represent the basic shapes. They implement the core functionality, such as resizing or defining specific properties like radius or side length.
Decorator Classes (ColoredShape, TransparentShape):
- These classes are decorators that add additional functionality, such as color or transparency, to the basic shapes. They wrap a shape object and extend its behavior.
```python
from abc import ABC

class Shape(ABC):
    def __str__(self):
        return ''

class Circle(Shape):
    def __init__(self, radius=0.0):
        self.radius = radius

    def resize(self, factor):
        self.radius *= factor

    def __str__(self):
        return f'A circle of radius {self.radius}'

class Square(Shape):
    def __init__(self, side):
        self.side = side

    def __str__(self):
        return f'A square with side {self.side}'

class ColoredShape(Shape):
    def __init__(self, shape, color):
        if isinstance(shape, ColoredShape):
            raise Exception('Cannot apply ColoredDecorator twice')
        self.shape = shape
        self.color = color

    def __str__(self):
        return f'{self.shape} has the color {self.color}'

class TransparentShape(Shape):
    def __init__(self, shape, transparency):
        self.shape = shape
        self.transparency = transparency

    def __str__(self):
        return f'{self.shape} has {self.transparency * 100.0}% transparency'

if __name__ == '__main__':
    circle = Circle(2)
    print(circle)

    red_circle = ColoredShape(circle, "red")
    print(red_circle)

    red_half_transparent_square = TransparentShape(red_circle, 0.5)
    print(red_half_transparent_square)

    # Example of double decoration
    mixed = ColoredShape(ColoredShape(Circle(3), 'red'), 'blue')
    print(mixed)
```
### Explanation
**Abstract Base Class (Shape)**:

- Provides a base class for all shapes, enforcing a uniform interface. In this case, it only defines a __str__ method, which is overridden by concrete shapes and decorators.
**Concrete Shapes (Circle, Square)**:

These classes inherit from Shape and provide specific implementations for geometric shapes.
- Circle: Implements radius-related functionality and includes a resize method.
- Square: Represents a square and stores the length of its side.
**Decorator Classes (ColoredShape, TransparentShape)**:

- ColoredShape: Adds color to a shape. It checks if the shape has already been colored to prevent applying the same decorator twice.
- TransparentShape: Adds transparency to a shape and converts the transparency value into a percentage for display.

**Combining Decorators**:

Decorators can be stacked, allowing for multiple extensions of the original shape's functionality. For example, a circle can be both colored and transparent.
The code demonstrates how to combine these decorators to create complex shapes with multiple features.


### Key Points
- Dynamic Behavior Extension: The decorator pattern allows you to dynamically add functionality to objects at runtime, promoting flexibility and reusability.
- Adherence to Design Principles: The pattern adheres to the Open/Closed Principle by allowing objects to be extended without modifying their code.
- Preventing Redundant Decoration: The implementation includes checks to prevent the same decorator from being applied multiple times, avoiding redundant or conflicting behaviors.

### Limitations
- Access to Original Methods: Decorators do not automatically provide access to the original object's methods unless explicitly programmed. For example, the resize method of Circle is not accessible once the shape is decorated.
- Complexity: When multiple decorators are stacked, it can become difficult to manage the interactions between them, particularly if they modify the same aspect of the object.


## File Decorator Implementation with Logging
This documentation outlines the implementation of a class decorator that wraps around a file object, allowing additional functionality such as logging while maintaining access to the file’s original methods. The approach leverages dynamic programming to proxy all operations from the decorator to the underlying file, minimizing the need to manually override each file method.

### Class: FileWithLogging
The FileWithLogging class is a decorator for file objects, allowing additional operations (like logging) to be performed whenever certain methods (e.g., writelines) are called on the file. It also ensures that all other file operations behave as if they were directly performed on the original file.

```python
class FileWithLogging:
  def __init__(self, file):
    self.file = file

  def writelines(self, strings):
    self.file.writelines(strings)
    print(f'wrote {len(strings)} lines')

  def __iter__(self):
    return self.file.__iter__()

  def __next__(self):
    return self.file.__next__()

  def __getattr__(self, item):
    return getattr(self.__dict__['file'], item)

  def __setattr__(self, key, value):
    if key == 'file':
      self.__dict__[key] = value
    else:
      setattr(self.__dict__['file'], key)

  def __delattr__(self, item):
    delattr(self.__dict__['file'], item)
```

**Constructor: __init__(self, file)**: Initialize the decorator with the file to be wrapped.
**Method: writelines(self, strings)**: Write multiple lines to the file and log how many lines were written.
**Method: __iter__(self)**: Allow the decorated file to be used in a loop, forwarding iteration to the original file.
**Method: __next__(self)**: Support fetching the next line in a loop, just like the original 
**Method: __getattr__(self, item)**: Forward all method and attribute calls to the original file, ensuring the decorator acts just like the file.
**Method: __setattr__(self, key, value)**: Set attributes either on the decorator or the original file, depending on the key.
**Method: __delattr__(self, item)**: Delete attributes from the original file.

### Example Usage:
```python
file = FileWithLogging(open('hello.txt', 'w'))
file.writelines(['hello', 'world'])  # Logs: wrote 2 lines
file.write('testing')
file.close()
```

The FileWithLogging class decorates a file to add logging and ensure it retains all original file behaviors. It demonstrates how to wrap an object and dynamically forward calls, making it an effective example of enhancing functionality without modifying the original class.
