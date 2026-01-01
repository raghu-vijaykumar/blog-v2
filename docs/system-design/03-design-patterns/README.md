---
sidebar_title: Design Patterns
---

# Design Patterns

Typically, in design pattern literature, regardless of which language of design patterns we are talking about, we split all the design patterns into several different categories. These categories are often called Gamma categorization, named after Erich Gamma, one of the authors of the original Gang of Four book that uses C++ and Smalltalk.

## Gamma Categorization

1. **Creational Patterns**
    - These patterns deal with the creation or construction of objects.
    - Object construction can be more complicated than simply invoking a constructor.
    - **Explicit Creation**: Directly calling the constructor with arguments to initialize an object.
    - **Implicit Creation**: Using mechanisms like dependency injection frameworks or reflection to create objects behind the scenes.
    - **Wholesale Creation**: A single statement or constructor call is sufficient to initialize the object.
    - **Piecewise Initialization**: Several steps or statements are needed to fully initialize an object.

2. **Structural Patterns**
    - Concerned with the structure of classes and their members.
    - Focus on how classes and objects are composed to form larger structures.
    - Examples include wrappers that mimic the underlying interface they wrap.
    - Emphasize good API design, making interfaces convenient and usable for others.
    - Structural patterns often involve replicating or enhancing interfaces for better usability.

3. **Behavioral Patterns**
    - Unlike creational and structural patterns, behavioral patterns don't follow a central theme.
    - They address different problems and solve them in unique ways using various object-oriented mechanisms.
    - There may be some overlap (e.g., Strategy and Template Method patterns), but each pattern generally solves a specific problem with its own approach.
    - Behavioral patterns focus on the interaction and responsibility of objects.

## Patterns Cheatsheet

| Name                                                           | Brief Explanation                                                                                                                                        | Use Case                                                                                                                     |
| -------------------------------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------- |
| [**Singleton**](./singleton/)                            | Ensures a class has only one instance and provides a global point of access to that instance.                                                            | Used for managing shared resources like database connections or configuration settings.                                      |
| [**Factory**](./factory/)                                | Provides an interface for creating objects without specifying their exact class. It can produce families of related or dependent objects.                | Useful when a class can't anticipate the type of objects it needs to create or when the creation process must be abstracted. |
| [**Builder**](./builder/)                                | Separates the construction of a complex object from its representation so that the same construction process can create different representations.       | Useful for constructing complex objects with many optional components.                                                       |
| [**Prototype**](./prototype/)                            | Creates new objects by copying an existing object, known as the prototype.                                                                               | Suitable for cases where creating a new instance from scratch is costly.                                                     |
| [**Adapter**](./adapter/)                                | Allows an interface to be used by a class that would not normally be compatible by wrapping its interface.                                               | Useful for integrating new features or systems with existing systems without modifying their code.                           |
| [**Bridge**](./bridge/)                                  | Decouples an abstraction from its implementation so that the two can vary independently.                                                                 | Useful for developing scalable and extensible code, such as UI frameworks with varying platforms.                            |
| [**Composite**](./composite/)                            | Composes objects into tree-like structures to represent part-whole hierarchies, allowing clients to treat individual objects and compositions uniformly. | Ideal for working with hierarchical structures such as file systems or organizational charts.                                |
| [**Decorator**](./decorator/)                            | Adds responsibilities to objects dynamically without modifying their code.                                                                               | Ideal for extending the functionalities of objects in a flexible and reusable way.                                           |
| [**Facade**](./facade/)                                  | Provides a simplified interface to a complex subsystem.                                                                                                  | Used to reduce complexity in large systems by providing a higher-level interface.                                            |
| [**Flyweight**](./flyweight/)                            | Reduces memory usage by sharing as much data as possible with similar objects.                                                                           | Useful in applications where many small objects are created, such as in graphical applications.                              |
| [**Proxy**](./proxy/)                                    | Provides a surrogate or placeholder for another object to control access to it.                                                                          | Used for controlling access to an object, such as in lazy loading or access control.                                         |
| [**Observer**](./observer/)                              | Defines a dependency between objects so that when one object changes state, all its dependents are notified and updated automatically.                   | Commonly used in event handling systems, such as UI updates when data changes.                                               |
| [**Strategy**](./strategy/)                              | Defines a family of algorithms, encapsulates each one, and makes them interchangeable.                                                                   | Useful when a class has multiple algorithms for a specific operation and needs to switch between them.                       |
| [**Command**](./command/)                                | Encapsulates a request as an object, thereby allowing parameterization of clients with queues, requests, and operations.                                 | Helpful for implementing undo functionality or queuing operations.                                                           |
| [**Chain of Responsibility**](./chain-of-responsiblity/) | Passes a request along a chain of handlers, where each handler can either handle the request or pass it along the chain.                                 | Used for handling requests with multiple possible handlers, such as in middleware systems.                                   |
| [**Mediator**](./mediator/)                              | Defines an object that encapsulates how a set of objects interact, promoting loose coupling between them.                                                | Useful in complex systems where components need to interact with each other without tight coupling.                          |
| [**Memento**](./memento/)                                | Captures and externalizes an object's internal state without violating encapsulation, allowing the object to be restored to this state later.            | Ideal for implementing undo functionality or storing the state of an object.                                                 |
| [**State**](./state/)                                    | Allows an object to alter its behavior when its internal state changes.                                                                                  | Useful for objects that need to change behavior based on their state, such as a document editor.                             |
| [**Template Method**](./template-method/)                | Defines the skeleton of an algorithm in a base class but lets subclasses override specific steps of the algorithm without changing its structure.        | Useful for defining a sequence of operations where some steps can be customized.                                             |
| [**Visitor**](./visitor/)                                | Defines a new operation to a set of objects without changing the objects themselves.                                                                     | Useful for operations that need to be performed on different types of objects in a collection.                               |
| [**Interpreter**](./interpreter/)                        | Defines a way to evaluate grammar or expression languages, particularly useful in parsing.                                                               | Suitable for implementing language interpreters or compilers.                                                                |
| [**Iterator**](./iterator/)                              | Provides a way to access elements of an aggregate object sequentially without exposing its underlying representation.                                    | Useful for traversing collections, such as lists or arrays.                                                                  |
| [**Null Object**](./null-object/)                        | Provides a default behavior for a non-existent object, avoiding null checks throughout the code.                                                         | Ideal for avoiding null reference errors and simplifying conditional logic.                                                  |
