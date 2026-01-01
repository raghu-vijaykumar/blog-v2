---
title: "Solid Principles"
tags: ["system-design", "solid-principles"]
---

import Tabs from '@theme/Tabs';
import TabItem from '@theme/TabItem';

# Solid Principles

## Single Responsibility Principle (SRP)

The Single Responsibility Principle (SRP) is one of the five SOLID principles of object-oriented design and programming. It states that a class, module, or function should have only one reason to change, meaning it should have only one responsibility or job. This principle promotes the idea that a class should only have one reason to exist, encapsulating all the functionality related to that responsibility.

Key Points of Single Responsibility Principle

- **Separation of Concerns**: SRP encourages separating different concerns into different classes or modules, which makes the system more modular and easier to understand.
- **Maintainability**: By adhering to SRP, changes in the system are more isolated. If a change in a business requirement affects one responsibility, only the corresponding class or module needs to be modified.
- **Testability**: Classes with a single responsibility are easier to test because they have a clear and narrow focus.
- **Reusability**: When classes are responsible for only one thing, they can be more easily reused in different parts of the application or in different projects.

### Example

Consider a class Report that handles generating a report, formatting it, and sending it via email. This class has multiple responsibilities: generating data, formatting, and sending. According to SRP, these responsibilities should be split into separate classes:

`ReportGenerator`: Responsible for creating the report data.
`ReportFormatter`: Responsible for formatting the report data.
`EmailSender`: Responsible for sending the report via email.

<Tabs>
<TabItem value="java" label="Java">

```java
// Before applying SRP
class Report {
    public void generateReport() {
        // Logic to generate report
    }

    public void formatReport() {
        // Logic to format report
    }

    public void sendEmail() {
        // Logic to send report via email
    }
}

// After applying SRP
class ReportGenerator {
    public void generateReport() {
        // Logic to generate report
    }
}

class ReportFormatter {
    public void formatReport() {
        // Logic to format report
    }
}

class EmailSender {
    public void sendEmail() {
        // Logic to send report via email
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        ReportGenerator generator = new ReportGenerator();
        ReportFormatter formatter = new ReportFormatter();
        EmailSender sender = new EmailSender();

        generator.generateReport();
        formatter.formatReport();
        sender.sendEmail();
    }
}
```

</TabItem>
<TabItem value="python" label="Python">

```python
# Before applying SRP
class Report:
    def generate_report(self):
        # Logic to generate report
        pass

    def format_report(self):
        # Logic to format report
        pass

    def send_email(self):
        # Logic to send report via email
        pass

# After applying SRP
class ReportGenerator:
    def generate_report(self):
        # Logic to generate report
        pass

class ReportFormatter:
    def format_report(self):
        # Logic to format report
        pass

class EmailSender:
    def send_email(self):
        # Logic to send report via email
        pass

# Usage
if __name__ == "__main__":
    generator = ReportGenerator()
    formatter = ReportFormatter()
    sender = EmailSender()

    generator.generate_report()
    formatter.format_report()
    sender.send_email()
```

</TabItem>
</Tabs>

By separating the responsibilities into different classes, each class has a single responsibility, making the code more modular, maintainable, testable, and reusable.

## Open/Closed Principle (OCP)

The Open/Closed Principle (OCP) states that software entities (such as classes, modules, and functions) should be open for extension but closed for modification. This means that the behavior of a module can be extended without modifying its source code, thereby reducing the risk of introducing bugs and ensuring that existing functionality remains unchanged.

### Key Points of Open/Closed Principle

- **Extensibility**: New functionality can be added by extending existing classes or modules without altering their source code.
- **Maintainability**: By avoiding changes to existing code, you reduce the chances of introducing new bugs and make the system easier to maintain.
- **Encapsulation**: Keeps implementation details hidden and promotes the use of abstractions.
- **Reusability**: Makes it easier to reuse existing code because it is less likely to change

### Example

Consider a class AreaCalculator that calculates the area of different shapes. Initially, it might only handle rectangles and circles. If you want to add support for new shapes (e.g., triangles), you would need to modify the AreaCalculator class. According to OCP, this should be done by extending the functionality without modifying the existing code.

<Tabs>
<TabItem value="java" label="Java">

```java
// Before applying OCP
class AreaCalculator {
    public double calculateRectangleArea(Rectangle rectangle) {
        return rectangle.getWidth() * rectangle.getHeight();
    }

    public double calculateCircleArea(Circle circle) {
        return Math.PI * Math.pow(circle.getRadius(), 2);
    }
}

// After applying OCP
interface Shape {
    double calculateArea();
}

class Rectangle implements Shape {
    private double width;
    private double height;

    public Rectangle(double width, double height) {
        this.width = width;
        this.height = height;
    }

    @Override
    public double calculateArea() {
        return width * height;
    }
}

class Circle implements Shape {
    private double radius;

    public Circle(double radius) {
        this.radius = radius;
    }

    @Override
    public double calculateArea() {
        return Math.PI * Math.pow(radius, 2);
    }
}

class AreaCalculator {
    public double calculateArea(Shape shape) {
        return shape.calculateArea();
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Shape rectangle = new Rectangle(5, 10);
        Shape circle = new Circle(7);

        AreaCalculator calculator = new AreaCalculator();
        System.out.println("Rectangle Area: " + calculator.calculateArea(rectangle));
        System.out.println("Circle Area: " + calculator.calculateArea(circle));
    }
}
```

</TabItem>
<TabItem value="python" label="Python">

```python
# Before applying OCP
class AreaCalculator:
    def calculate_rectangle_area(self, rectangle):
        return rectangle.width * rectangle.height

    def calculate_circle_area(self, circle):
        return 3.14 * circle.radius ** 2

# After applying OCP
from abc import ABC, abstractmethod

class Shape(ABC):
    @abstractmethod
    def calculate_area(self):
        pass

class Rectangle(Shape):
    def __init__(self, width, height):
        self.width = width
        self.height = height

    def calculate_area(self):
        return self.width * self.height

class Circle(Shape):
    def __init__(self, radius):
        self.radius = radius

    def calculate_area(self):
        return 3.14 * self.radius ** 2

class AreaCalculator:
    def calculate_area(self, shape):
        return shape.calculate_area()

# Usage
if __name__ == "__main__":
    rectangle = Rectangle(5, 10)
    circle = Circle(7)

    calculator = AreaCalculator()
    print("Rectangle Area:", calculator.calculate_area(rectangle))
    print("Circle Area:", calculator.calculate_area(circle))
```

</TabItem>
</Tabs>

By following the Open/Closed Principle, new shapes can be added by creating new classes that implement the `Shape` interface, without modifying the existing `AreaCalculator` class. This approach makes the code more maintainable and extensible.

## Liskov Substitution Principle (LSP)

The Liskov Substitution Principle (LSP) states that objects of a superclass should be replaceable with objects of a subclass without affecting the correctness of the program. In other words, if class S is a subclass of class T, then objects of type T should be replaceable with objects of type S without altering any desirable properties of the program (correctness, task performed, etc.).

### Key Points of Liskov Substitution Principle

- Substitutability: Subtypes must be substitutable for their base types.
- Behavioral Consistency: The derived class must not only implement the methods of the base class but also maintain the expected behavior of the base class.
- Interface Adherence: Derived classes should adhere to the contracts of the base class.
- No Strengthening of Preconditions: Derived classes should not impose stronger preconditions than the base class.
- No Weakening of Postconditions: Derived classes should not weaken the postconditions of the base class.

### Example

Consider a class Bird with a method fly. If we create a subclass Penguin which cannot fly, substituting a Bird object with a Penguin object would violate the Liskov Substitution Principle.

<Tabs>
<TabItem value="java" label="Java">

```java
// Before applying LSP
class Bird {
    public void fly() {
        System.out.println("Flying");
    }
}

class Sparrow extends Bird {
}

class Penguin extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Penguins cannot fly");
    }
}

// After applying LSP
abstract class Bird {
    public abstract void move();
}

class Sparrow extends Bird {
    @Override
    public void move() {
        fly();
    }

    private void fly() {
        System.out.println("Flying");
    }
}

class Penguin extends Bird {
    @Override
    public void move() {
        swim();
    }

    private void swim() {
        System.out.println("Swimming");
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Bird sparrow = new Sparrow();
        Bird penguin = new Penguin();

        sparrow.move();  // Output: Flying
        penguin.move();  // Output: Swimming
    }
}
```

</TabItem>
<TabItem value="python" label="Python">

```python
# Before applying LSP
class Bird:
    def fly(self):
        print("Flying")

class Sparrow(Bird):
    pass

class Penguin(Bird):
    def fly(self):
        raise NotImplementedError("Penguins cannot fly")

# After applying LSP
from abc import ABC, abstractmethod

class Bird(ABC):
    @abstractmethod
    def move(self):
        pass

class Sparrow(Bird):
    def move(self):
        self.fly()

    def fly(self):
        print("Flying")

class Penguin(Bird):
    def move(self):
        self.swim()

    def swim(self):
        print("Swimming")

# Usage
if __name__ == "__main__":
    sparrow = Sparrow()
    penguin = Penguin()

    sparrow.move()  # Output: Flying
    penguin.move()  # Output: Swimming
```

</TabItem>
</Tabs>

## Interface Segregation Principle (ISP)

The Interface Segregation Principle (ISP) is one of the five SOLID principles of object-oriented design and programming. It states that no client should be forced to depend on methods it does not use. This means that interfaces should be small and specific to particular client needs rather than large and general.

### Key Points of Interface Segregation Principle

- Client-Specific Interfaces: Design interfaces that are specific to the needs of the clients using them.
- Avoid Fat Interfaces: Large, general-purpose interfaces should be avoided. Instead, break them down into smaller, more focused interfaces.
- High Cohesion: Ensure that interfaces have high cohesion, meaning they should group related methods that serve a single purpose.
- Decoupling: Reduce the dependency of clients on unnecessary methods by providing them with the specific interfaces they need.

### Example

Consider a Worker interface with methods work and eat. A Robot class would need to implement the work method but not the eat method, violating the ISP.


<Tabs>
<TabItem value="java" label="Java">

```java
// Before applying ISP
interface Worker {
    void work();
    void eat();
}

class HumanWorker implements Worker {
    public void work() {
        System.out.println("Human working");
    }

    public void eat() {
        System.out.println("Human eating");
    }
}

class RobotWorker implements Worker {
    public void work() {
        System.out.println("Robot working");
    }

    public void eat() {
        throw new UnsupportedOperationException("Robots don't eat");
    }
}

// After applying ISP
interface Workable {
    void work();
}

interface Eatable {
    void eat();
}

class HumanWorker implements Workable, Eatable {
    public void work() {
        System.out.println("Human working");
    }

    public void eat() {
        System.out.println("Human eating");
    }
}

class RobotWorker implements Workable {
    public void work() {
        System.out.println("Robot working");
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Workable humanWorker = new HumanWorker();
        Workable robotWorker = new RobotWorker();

        humanWorker.work();  // Output: Human working
        robotWorker.work();  // Output: Robot working

        Eatable human = (Eatable) humanWorker;
        human.eat();  // Output: Human eating
    }
}
```

</TabItem>
<TabItem value="python" label="Python">

```python
# Before applying ISP
class Worker:
    def work(self):
        raise NotImplementedError

    def eat(self):
        raise NotImplementedError

class HumanWorker(Worker):
    def work(self):
        print("Human working")

    def eat(self):
        print("Human eating")

class RobotWorker(Worker):
    def work(self):
        print("Robot working")

    def eat(self):
        raise NotImplementedError("Robots don't eat")

# After applying ISP
from abc import ABC, abstractmethod

class Workable(ABC):
    @abstractmethod
    def work(self):
        pass

class Eatable(ABC):
    @abstractmethod
    def eat(self):
        pass

class HumanWorker(Workable, Eatable):
    def work(self):
        print("Human working")

    def eat(self):
        print("Human eating")

class RobotWorker(Workable):
    def work(self):
        print("Robot working")

# Usage
if __name__ == "__main__":
    human_worker = HumanWorker()
    robot_worker = RobotWorker()

    human_worker.work()  # Output: Human working
    robot_worker.work()  # Output: Robot working

    human_worker.eat()  # Output: Human eating
```

</TabItem>
</Tabs>


## Dependency Inversion Principle

The Dependency Inversion Principle (DIP) suggests that high-level modules should not depend on low-level modules. Instead, both should depend on abstractions (interfaces or abstract classes). Additionally, abstractions should not depend on details; details should depend on abstractions.

### Key Points of Dependency Inversion Principle

- High-Level Modules: These are components that contain complex logic and depend on other components to perform their tasks.
- Low-Level Modules: These are components that handle more detailed, specific operations and are often directly used by high-level modules.
- Abstractions: High-level modules should rely on abstractions rather than concrete implementations of low-level modules.
- Decoupling: DIP promotes the decoupling of software components, making them more reusable, maintainable, and flexible.

### Example

Consider a situation where a UserService class depends on a MySQLDatabase class for database operations. If the database implementation needs to change, the UserService class would also need modification, violating the DIP.


<Tabs>
<TabItem value="java" label="Java">

```java
// Before applying DIP
class MySQLDatabase {
    public void connect() {
        System.out.println("Connected to MySQL Database");
    }

    public void disconnect() {
        System.out.println("Disconnected from MySQL Database");
    }
}

class UserService {
    private MySQLDatabase database;

    public UserService() {
        this.database = new MySQLDatabase();
    }

    public void process() {
        database.connect();
        System.out.println("Processing user data");
        database.disconnect();
    }
}

// After applying DIP
interface Database {
    void connect();
    void disconnect();
}

class MySQLDatabase implements Database {
    public void connect() {
        System.out.println("Connected to MySQL Database");
    }

    public void disconnect() {
        System.out.println("Disconnected from MySQL Database");
    }
}

class PostgreSQLDatabase implements Database {
    public void connect() {
        System.out.println("Connected to PostgreSQL Database");
    }

    public void disconnect() {
        System.out.println("Disconnected from PostgreSQL Database");
    }
}

class UserService {
    private Database database;

    public UserService(Database database) {
        this.database = database;
    }

    public void process() {
        database.connect();
        System.out.println("Processing user data");
        database.disconnect();
    }
}

// Usage
public class Main {
    public static void main(String[] args) {
        Database mySQLDatabase = new MySQLDatabase();
        UserService userService = new UserService(mySQLDatabase);
        userService.process();  // Output: Connected to MySQL Database, Processing user data, Disconnected from MySQL Database
    }
}
```

</TabItem>
<TabItem value="python" label="Python">

```python
# Before applying DIP
class MySQLDatabase:
    def connect(self):
        print("Connected to MySQL Database")

    def disconnect(self):
        print("Disconnected from MySQL Database")

class UserService:
    def __init__(self):
        self.database = MySQLDatabase()

    def process(self):
        self.database.connect()
        print("Processing user data")
        self.database.disconnect()

# After applying DIP
from abc import ABC, abstractmethod

class Database(ABC):
    @abstractmethod
    def connect(self):
        pass

    @abstractmethod
    def disconnect(self):
        pass

class MySQLDatabase(Database):
    def connect(self):
        print("Connected to MySQL Database")

    def disconnect(self):
        print("Disconnected from MySQL Database")

class PostgreSQLDatabase(Database):
    def connect(self):
        print("Connected to PostgreSQL Database")

    def disconnect(self):
        print("Disconnected from PostgreSQL Database")

class UserService:
    def __init__(self, database: Database):
        self.database = database

    def process(self):
        self.database.connect()
        print("Processing user data")
        self.database.disconnect()

# Usage
if __name__ == "__main__":
    my_sql_database = MySQLDatabase()
    user_service = UserService(my_sql_database)
    user_service.process()  # Output: Connected to MySQL Database, Processing user data, Disconnected from MySQL Database
```

</TabItem>
</Tabs>

- Before applying DIP: The UserService class directly depends on the MySQLDatabase class. If we need to switch to a different database (e.g., PostgreSQL), we would have to modify the UserService class, which violates the DIP.

- After applying DIP: The UserService class now depends on the Database interface (an abstraction), not on a specific implementation like MySQLDatabase or PostgreSQLDatabase. This allows us to change the database implementation without modifying the UserService class, adhering to the Dependency Inversion Principle.

By following DIP, you can achieve greater flexibility and maintainability in your codebase, as high-level modules are no longer tightly coupled to low-level details.
