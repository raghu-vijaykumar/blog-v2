---
sidebar_title: Builder Pattern
---

# Builder Pattern

The Builder Design Pattern is a creational pattern that provides a way to construct complex objects step by step. Unlike the abstract factory pattern, the builder pattern is more about constructing a single object, rather than families of objects.

## Why Use the Builder Pattern?
The Builder pattern is particularly useful when:

- **Complex Object Construction**: When an object requires multiple steps to construct, where each step may involve different parts of the object.
- **Consistent Object Creation**: Ensuring that the object is constructed in a valid state, by preventing issues such as missing or incorrectly ordered initialization steps.
- **Readable and Maintainable Code**: Breaking down the object construction into simpler, readable steps, rather than a large constructor or a sequence of setters.

## Example: Building HTML Elements
To illustrate the Builder pattern, consider constructing HTML elements, which may involve multiple steps such as opening tags, inserting text, adding child elements, and closing tags.

Basic Construction (Without Builder)
Initially, a simple HTML paragraph or list can be constructed using a basic approach:

```python
 Simple paragraph
hello = 'hello'
parts = ['<p>', hello, '</p>']
print(''.join(parts))

 Simple list with words
words = ['hello', 'world']
parts = ['<ul>']
for w in words:
    parts.append(f'  <li>{w}</li>')
parts.append('</ul>')
print('\n'.join(parts))
```

This approach works for simple cases but becomes cumbersome as complexity increases, such as when dealing with nested elements or ensuring proper tag closure.

### Introducing the Builder Pattern
To handle more complex scenarios, we introduce the `HtmlElement` and `HtmlBuilder` classes.

`HtmlElement` Class
This class models an HTML element and manages its name, text, and child elements:

```python 
class HtmlElement:
    indent_size = 2

    def __init__(self, name="", text=""):
        self.name = name
        self.text = text
        self.elements = []

    def __str(self):
        return self.__str(0)

    def __str(self, indent):
        lines = []
        i = ' ' * (indent * self.indent_size)
        lines.append(f'{i}<{self.name}>')

        if self.text:
            i1 = ' ' * ((indent + 1) * self.indent_size)
            lines.append(f'{i1}{self.text}')

        for e in self.elements:
            lines.append(e.__str(indent + 1))

        lines.append(f'{i}</{self.name}>')
        return '\n'.join(lines)

    @staticmethod
    def create(name):
        return HtmlBuilder(name)
```

`HtmlBuilder` Class
The HtmlBuilder class facilitates the construction of `HtmlElement` objects, handling the creation and management of child elements:

```python
class HtmlBuilder:
    __root = HtmlElement()

    def __init__(self, root_name):
        self.root_name = root_name
        self.__root.name = root_name

    def add_child(self, child_name, child_text):
        self.__root.elements.append(HtmlElement(child_name, child_text))

    def add_child_fluent(self, child_name, child_text):
        self.__root.elements.append(HtmlElement(child_name, child_text))
        return self

    def clear(self):
        self.__root = HtmlElement(name=self.root_name)

    def __str__(self):
        return str(self.__root)
```

The add_child_fluent method enables a fluent interface, allowing method chaining for a more readable construction process.

Using the Builder
Constructing an HTML unordered list (`<ul>`) with two list items (`<li>`) can be done as follows:

Ordinary Builder:

```python 
builder = HtmlElement.create('ul')
builder.add_child('li', 'hello')
builder.add_child('li', 'world')
print('Ordinary builder:')
print(builder)
```

Fluent Builder:
```python
builder.clear()
builder.add_child_fluent('li', 'hello') \
    .add_child_fluent('li', 'world')
print('Fluent builder:')
print(builder)
```
## Advanced Builder Pattern with Multiple Sub-Builders

This example demonstrates an advanced usage of the Builder design pattern, where multiple sub-builders are used to construct different parts of a complex object. Specifically, we use the Builder pattern to construct a Person object, which has two distinct aspects: address and employment information. The challenge is to allow the construction of this object using separate builders in a fluent and cohesive manner.

### Key Concepts
- Sub-Builders:
  - `PersonJobBuilder` and `PersonAddressBuilder` are sub-builders that inherit from PersonBuilder.
  - Each sub-builder is responsible for a specific aspect of the `Person` object.
- Fluent Interface:
  - The builder methods return self, allowing method chaining.
  - This enables a concise and readable way to build the object, e.g., `pb.lives.at(...).in_city(...).works.at(...).as_a(...).build()`.
- Base Builder:
  - `PersonBuilder` acts as a common interface that initializes a `Person` object and provides access to the sub-builders via the `lives` and `works` properties.
- Property-Driven Interface:
  - The lives and works properties in `PersonBuilder` return instances of `PersonAddressBuilder` and `PersonJobBuilder`, respectively.
  - This design allows seamless switching between the different aspects of the object being built.

```python
class Person:
    def __init__(self):
        print("Creating an instance of Person")
         address
        self.street_address = None
        self.postcode = None
        self.city = None
         employment info
        self.company_name = None
        self.position = None
        self.annual_income = None

    def __str__(self) -> str:
        return (
            f"Address: {self.street_address}, {self.postcode}, {self.city}\n"
            + f"Employed at {self.company_name} as a {self.position} earning {self.annual_income}"
        )

class PersonBuilder:   facade
    def __init__(self, person=None):
        if person is None:
            self.person = Person()
        else:
            self.person = person

    @property
    def lives(self):
        return PersonAddressBuilder(self.person)

    @property
    def works(self):
        return PersonJobBuilder(self.person)

    def build(self):
        return self.person

class PersonJobBuilder(PersonBuilder):
    def __init__(self, person):
        super().__init__(person)

    def at(self, company_name):
        self.person.company_name = company_name
        return self

    def as_a(self, position):
        self.person.position = position
        return self

    def earning(self, annual_income):
        self.person.annual_income = annual_income
        return self

class PersonAddressBuilder(PersonBuilder):
    def __init__(self, person):
        super().__init__(person)

    def at(self, street_address):
        self.person.street_address = street_address
        return self

    def with_postcode(self, postcode):
        self.person.postcode = postcode
        return self

    def in_city(self, city):
        self.person.city = city
        return self

if __name__ == "__main__":
    pb = PersonBuilder()
    p = (
        pb.lives.at("123 London Road")
        .in_city("London")
        .with_postcode("SW12BC")
        .works.at("Fabrikam")
        .as_a("Engineer")
        .earning(123000)
        .build()
    )
    print(p)
```
### Usage
The example demonstrates the usage of a PersonBuilder to build a Person object with both address and employment information. The fluent interface allows easy chaining of method calls, and the properties lives and works provide access to the sub-builders for address and employment, respectively.

This setup is particularly useful when constructing complex objects that require multiple, distinct building processes, while maintaining a clean and intuitive API for the end user.

## Builder Pattern Using Inheritance with Open/Closed Principle

This example demonstrates an alternative approach to the Builder pattern that leverages inheritance to extend functionality. This approach addresses the violation of the Open/Closed Principle observed in traditional builder patterns, where adding new features requires modifying the original builder class. By using inheritance, new builders can be created that extend the functionality of existing ones without altering the existing code, thereby adhering to the Open/Closed Principle.

### Key Concepts
- Inheritance-Based Builders:
  - Instead of having multiple sub-builders within a single builder class, each additional piece of functionality is added through inheritance.
  - This method allows you to extend the builder's capabilities by creating new classes that inherit from existing builders, adding only the necessary functionality.
- Open/Closed Principle:
  - The Open/Closed Principle states that software entities should be open for extension but closed for modification.
  - In this pattern, once a builder class is created, it does not need to be modified to add new features. Instead, new builder classes are created by inheriting from the existing ones.
- Fluent Interface:
  - The builder classes use a fluent interface, returning self from methods to allow method chaining.
  - This results in a clear and readable way to construct the Person object.


```python
class Person:
    def __init__(self):
        self.name = None
        self.position = None
        self.date_of_birth = None

    def __str__(self):
        return f'{self.name} born on {self.date_of_birth} works as a {self.position}'

    @staticmethod
    def new():
        return PersonBuilder()

class PersonBuilder:
    def __init__(self):
        self.person = Person()

    def build(self):
        return self.person


class PersonInfoBuilder(PersonBuilder):
    def called(self, name):
        self.person.name = name
        return self


class PersonJobBuilder(PersonInfoBuilder):
    def works_as_a(self, position):
        self.person.position = position
        return self


class PersonBirthDateBuilder(PersonJobBuilder):
    def born(self, date_of_birth):
        self.person.date_of_birth = date_of_birth
        return self


if __name__ == '__main__':
    pb = PersonBirthDateBuilder()
    me = pb\
        .called('Dmitri')\
        .works_as_a('quant')\
        .born('1/1/1980')\
        .build()
    print(me)
```

### Usage
In this example, the Person object is constructed by chaining method calls on a PersonBirthDateBuilder instance, which is the most derived builder in the hierarchy. The inheritance-based approach ensures that each builder in the hierarchy can add specific attributes to the Person object while still allowing all the previously added attributes to be set.

This design allows for a flexible and extendable building process. If new attributes or methods are needed in the future, they can be added by simply creating a new builder that inherits from the appropriate existing builder, ensuring that the Open/Closed Principle is maintained throughout the development process.