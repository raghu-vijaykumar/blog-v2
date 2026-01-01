---
sidebar_title: Visitor Pattern
---

# Visitor Pattern
The Visitor Design Pattern is a behavioral design pattern that allows you to separate algorithms from the objects on which they operate. It helps define new operations on a set of objects without modifying their structure. The key use case is when you want to perform a series of operations across a hierarchy of related classes, and you need to extend functionality without altering the class definitions, adhering to the Open-Closed Principle.

## Problem Overview
Consider a situation where you need to define a new operation on a group of related classes, such as printing different representations of a document (e.g., HTML or Markdown). The document might consist of paragraphs, images, and other elements. The challenge is where to place the logic for this new operation.

Modifying every class to support a new operation violates the Open-Closed Principle, which states that software entities should be open for extension but closed for modification. You don’t want to add rendering methods directly to every class in the hierarchy (e.g., paragraphs, images, etc.). Instead, you want an external component that can perform operations like rendering without modifying the existing structure.

## Key Points
- The Visitor Pattern allows for adding new operations without modifying the object structure.
- The Visitor can traverse complex data structures composed of different types related to each other, such as a document hierarchy.
- The pattern avoids explicit type checks and type-dependent logic, making the code more maintainable and extensible.
## Example Implementation
In this example, we work with numeric expressions, such as `1 + (2 + 3)`. The goal is to build an expression using objects and then evaluate and print it using the visitor pattern.

**Class Definitions**
```python
class DoubleExpression:
    def __init__(self, value):
        self.value = value

    def print(self, buffer):
        buffer.append(str(self.value))

    def eval(self): 
        return self.value

class AdditionExpression:
    def __init__(self, left, right):
        self.right = right
        self.left = left

    def print(self, buffer):
        buffer.append('(')
        self.left.print(buffer)
        buffer.append('+')
        self.right.print(buffer)
        buffer.append(')')

    def eval(self):
        return self.left.eval() + self.right.eval()
```
## Intrusive Visitor Approach
The above implementation takes an intrusive approach to the visitor pattern by directly modifying the existing classes to handle new operations (e.g., printing and evaluating the expression). Each class contains its own `print()` and `eval()` methods.

Sample Expression
```python
if __name__ == '__main__':
    # represents 1+(2+3)
    e = AdditionExpression(
        DoubleExpression(1),
        AdditionExpression(
            DoubleExpression(2),
            DoubleExpression(3)
        )
    )
    buffer = []
    e.print(buffer)
    print(''.join(buffer), '=', e.eval())
```
This code builds an expression representing 1 + (2 + 3) and outputs both the printed expression and its evaluation.

**Evaluation**
The output of the code is:

```sh
(1+(2+3)) = 6
```
The intrusive approach violates the Open-Closed Principle because adding new operations (e.g., printing or evaluating) requires modifying all classes in the hierarchy. For small hierarchies, this is manageable, but for larger hierarchies, this becomes problematic.

## Reflective Printing Approach
In the reflective approach, printing logic is separated into a class called ExpressionPrinter. This class contains a static method to handle printing of different types of expressions (DoubleExpression, AdditionExpression).

### Implementation Details
Separation of Concerns: All printing functionality is handled in the ExpressionPrinter class, leaving the expression classes (DoubleExpression, AdditionExpression) clean from printing logic.
Reflection-Based Type Checking: The ExpressionPrinter.print method checks the type of the expression using isinstance and processes the expression accordingly.

```python
from abc import ABC

class Expression(ABC):
    pass

class DoubleExpression(Expression):
    def __init__(self, value):
        self.value = value

class AdditionExpression(Expression):
    def __init__(self, left, right):
        self.right = right
        self.left = left

class ExpressionPrinter:
    @staticmethod
    def print(e, buffer):
        """ Will fail silently on a missing case. """
        if isinstance(e, DoubleExpression):
            buffer.append(str(e.value))
        elif isinstance(e, AdditionExpression):
            buffer.append('(')
            ExpressionPrinter.print(e.left, buffer)
            buffer.append('+')
            ExpressionPrinter.print(e.right, buffer)
            buffer.append(')')
    
    Expression.print = lambda self, b: ExpressionPrinter.print(self, b)

if __name__ == '__main__':
    # represents 1+(2+3)
    e = AdditionExpression(
        DoubleExpression(1),
        AdditionExpression(
            DoubleExpression(2),
            DoubleExpression(3)
        )
    )
    buffer = []
    e.print(buffer)
    print(''.join(buffer))
```
**Pros**:
- Clean separation of concerns.
- Simple and functional for small examples.
**Cons**:
- Violates the Open/Closed Principle (OCP): Adding new expression types requires modifications to the ExpressionPrinter.
- Fails silently for unsupported expression types, which can lead to debugging challenges.
## Double Dispatch Visitor Pattern Approach

The Visitor Pattern separates operations from the object structure on which it operates. In this case, an ExpressionPrinter class visits expression objects and prints them. Double dispatch ensures that the correct visit method is invoked based on the type of expression.

Double dispatch involves two levels of method dispatch:

- An accept method is called on the expression object.
- The correct visit method in the `ExpressionPrinter` class is then called based on the expression type.
This allows a cleaner and more scalable approach compared to the reflective method.

### Implementation Details
- **Visitor Decorator**: A decorator (@visitor) is used to map different visit methods to their corresponding expression types (DoubleExpression, AdditionExpression).
- **Visitor Methods**: These methods handle specific expressions and append their string representations to the buffer.

```python
def visitor(arg_type):
    def decorator(fn):
        declaring_class = _declaring_class(fn)
        _methods[(declaring_class, arg_type)] = fn
        return _visitor_impl
    return decorator

class DoubleExpression:
    def __init__(self, value):
        self.value = value

    def accept(self, visitor):
        visitor.visit(self)

class AdditionExpression:
    def __init__(self, left, right):
        self.left = left
        self.right = right

    def accept(self, visitor):
        visitor.visit(self)

class ExpressionPrinter:
    def __init__(self):
        self.buffer = []

    @visitor(DoubleExpression)
    def visit(self, de):
        self.buffer.append(str(de.value))

    @visitor(AdditionExpression)
    def visit(self, ae):
        self.buffer.append('(')
        ae.left.accept(self)
        self.buffer.append('+')
        ae.right.accept(self)
        self.buffer.append(')')

    def __str__(self):
        return ''.join(self.buffer)

if __name__ == '__main__':
    # represents 1+(2+3)
    e = AdditionExpression(
        DoubleExpression(1),
        AdditionExpression(
            DoubleExpression(2),
            DoubleExpression(3)
        )
    )
    printer = ExpressionPrinter()
    printer.visit(e)
    print(printer)
```
**Pros**:
- Follows the Open/Closed Principle (OCP).
- Easily extendable to new types of expressions without modifying existing classes.
**Cons**:
- Requires more setup and boilerplate code (visitor decorator).
- Can be less intuitive for newcomers due to double dispatch mechanics.

## Dynamic Visitor Without accept Method
The visitor design pattern allows you to separate algorithms from the objects on which they operate. In Python, the pattern can be implemented without requiring the traditional accept methods, which are more common in statically-typed languages. This document explains how the visitor pattern can be implemented dynamically in Python using a decorator-based approach.

### Key Concepts and Implementation Details
- **Dynamic Visitor Without accept Method**
  - **Traditional Implementation**: In languages with static typing, each element must implement an accept method, which then calls the visitor’s visit method for double dispatch.
  - **Dynamic Python Approach**: In Python, the accept method is not necessary. Instead, we can invoke visit directly on the visitor class with the appropriate expression object as a parameter. This bypasses the need for double dispatch while still ensuring the correct overload of visit is called, thanks to the dynamic dispatch mechanism provided by a visitor decorator.
- **Visitor Decorator**
  - A decorator is used to map types to their corresponding visitor methods dynamically. This is implemented by the _visitor_impl function and the visitor decorator, which registers the appropriate methods for each class type.
  - The visitor methods are stored in a global _methods dictionary that maps the class type and argument type to the visitor function.
- **Expression Classes**: DoubleExpression and AdditionExpression classes represent basic expressions. They hold values and/or other expressions, and they are visited by the visitor classes.

```python
class DoubleExpression:
    def __init__(self, value):
        self.value = value
```

- **ExpressionPrinter**: This class prints the expression as a string by visiting each node in the expression tree and concatenating the values in a readable format.

```python
class ExpressionPrinter:
    @visitor(DoubleExpression)
    def visit(self, de):
        self.buffer.append(str(de.value))

    @visitor(AdditionExpression)
    def visit(self, ae):
        self.buffer.append('(')
        ae.left.accept(self)
        self.buffer.append('+')
        ae.right.accept(self)
        self.buffer.append(')')
```
- **ExpressionEvaluator**: This visitor evaluates the expression by visiting the nodes in the tree and calculating the result. It handles both DoubleExpression and AdditionExpression, caching intermediate results to prevent overwriting during recursive evaluations.

```python
class ExpressionEvaluator:
    @visitor(DoubleExpression)
    def visit(self, de):
        self.value = de.value

    @visitor(AdditionExpression)
    def visit(self, ae):
        self.visit(ae.left)
        temp = self.value
        self.visit(ae.right)
        self.value += temp
```
**Example Usage**
In this example, we construct an expression representing `1 + (2 + 3)` and use both the `ExpressionPrinter` and `ExpressionEvaluator` to print the expression and evaluate its result.

```python
if __name__ == '__main__':
    e = AdditionExpression(
        DoubleExpression(1),
        AdditionExpression(
            DoubleExpression(2),
            DoubleExpression(3)
        )
    )
    printer = ExpressionPrinter()
    printer.visit(e)
    evaluator = ExpressionEvaluator()
    evaluator.visit(e)
    print(f'{printer} = {evaluator.value}')
```
### Summary of Key Points
- **Visitor Without accept**: The need for accept methods is eliminated in Python due to dynamic typing. Instead, the visit method is called directly.
- **Decorator-Based Implementation**: The visitor methods are dynamically mapped to the appropriate class types using a decorator, ensuring that the correct method is called during execution.
- **Handling Stateful Visitors**: In stateful visitors, care must be taken to cache intermediate results to prevent overwriting during recursive evaluations.
This approach provides flexibility and leverages Python's dynamic features to simplify the implementation of the visitor pattern.