---
sidebar_title: Strategy Pattern
---

# Strategy Pattern
The Strategy Design Pattern is a behavioral design pattern that enables selecting an algorithm's behavior at runtime. It separates the definition of an algorithm from the specifics of its implementation, allowing the algorithm's high-level structure to remain constant while the low-level implementation details are changeable based on different strategies.

## Decomposition of Algorithms
In many systems, algorithms can be decomposed into two parts:

- High-Level Part: This defines the general structure of the algorithm and is often reusable across various scenarios.
- Low-Level Part: These are the specific implementation details of the algorithm, which can be varied depending on the use case.

For example, making a hot beverage (tea, coffee, etc.) involves:

- High-Level Steps: Boiling water, pouring into a cup.
- Low-Level Steps: Specific to the beverage, such as steeping tea or adding coffee grounds.

## Purpose of the Strategy Design Pattern
The purpose of the strategy pattern is to encapsulate these low-level parts into interchangeable strategies while maintaining a consistent high-level structure. This pattern allows the behavior of a system to be selected at runtime by injecting the appropriate low-level strategy into the system's high-level process.

## Use Case Example: Text Processor
This example illustrates a text processor that can render lists into either Markdown or HTML format using different strategies.

### Key Components:
- **List Strategy (Abstract Class)**: Defines the interface that all list strategies must implement. This includes methods for starting a list, ending a list, and adding a list item.
- **MarkdownListStrategy**: Implements the list strategy to render lists in Markdown format. For Markdown, no start or end tags are required, and list items are prefixed with a star (*).
- **HtmlListStrategy**: Implements the list strategy to render lists in HTML format. HTML lists require opening and closing `<ul>` and `<li>` tags for unordered lists.
- **TextProcessor**: Handles the list rendering based on the strategy provided. It can dynamically switch between Markdown and HTML strategies during runtime and render the lists accordingly.

```python
from abc import ABC
from enum import Enum, auto


class OutputFormat(Enum):
    MARKDOWN = auto()
    HTML = auto()


# not required but a good idea
class ListStrategy(ABC):
    def start(self, buffer): pass

    def end(self, buffer): pass

    def add_list_item(self, buffer, item): pass


class MarkdownListStrategy(ListStrategy):

    def add_list_item(self, buffer, item):
        buffer.append(f' * {item}\n')

class HtmlListStrategy(ListStrategy):

    def start(self, buffer):
        buffer.append('<ul>\n')

    def end(self, buffer):
        buffer.append('</ul>\n')

    def add_list_item(self, buffer, item):
        buffer.append(f'  <li>{item}</li>\n')

class TextProcessor:
    def __init__(self, list_strategy=HtmlListStrategy()):
        self.buffer = []
        self.list_strategy = list_strategy

    def append_list(self, items):
        self.list_strategy.start(self.buffer)
        for item in items:
            self.list_strategy.add_list_item(self.buffer, item)
        self.list_strategy.end(self.buffer)

    def set_output_format(self, format):
        if format == OutputFormat.MARKDOWN:
            self.list_strategy = MarkdownListStrategy()
        elif format == OutputFormat.HTML:
            self.list_strategy = HtmlListStrategy()

    def clear(self):
        self.buffer.clear()

    def __str__(self):
        return ''.join(self.buffer)


if __name__ == '__main__':
    items = ['foo', 'bar', 'baz']

    tp = TextProcessor()
    tp.set_output_format(OutputFormat.MARKDOWN)
    tp.append_list(items)
    print(tp)

    tp.set_output_format(OutputFormat.HTML)
    tp.clear()
    tp.append_list(items)
    print(tp)
```
**Explanation of Code**
- **List Strategy Interface**: The ListStrategy class defines the interface with three methods: start, end, and add_list_item. Any class inheriting from ListStrategy must provide implementations for these methods, which handle how lists are rendered.
- **MarkdownListStrategy**: This class implements the add_list_item method, which formats a list item for Markdown by prefixing it with a *.
- **HtmlListStrategy**: This class implements all three methods to handle the specific rendering for HTML lists, including adding the necessary `<ul>`, `<li>`, and closing tags.
- **TextProcessor**: The TextProcessor class handles rendering a list using the provided strategy. It can switch between Markdown and HTML strategies dynamically at runtime.

## Runtime Flexibility
One of the core benefits of the strategy pattern is its runtime flexibility. In the TextProcessor example, the output format can be changed from Markdown to HTML dynamically, allowing the same list to be rendered in both formats by simply switching the strategy.

## Conclusion
The Strategy Pattern is ideal for situations where you need to dynamically select and apply different algorithms or behaviors at runtime without altering the underlying system structure. By decoupling the high-level algorithm from the low-level details, it promotes flexibility, scalability, and ease of testing.