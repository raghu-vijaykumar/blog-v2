---
sidebar_title: Facade Pattern
---

# Facade Pattern
The Facade Design Pattern provides a simplified interface to a complex subsystem. It offers an easy-to-use API while hiding the complexities of underlying systems. In software systems, this allows clients to perform operations with minimal code, while still enabling power users to access lower-level functionality when needed.

In this example, a Console class is implemented as a facade to manage Buffer and Viewport components, which handle the complexity of text manipulation in a console-based system.

## Components
**Buffer Class**: Represents a low-level construct that holds characters in a 1D list (simulating a text buffer).
- **Attributes**:
  - width: Width of the buffer (default: 30).
  - height: Height of the buffer (default: 20).
  - buffer: A list of characters initialized to blank spaces (' '), with a total size of width * height.
- **Methods**:
  - __getitem__(item): Allows access to elements in the buffer using indexing.
  - write(text): Appends text to the buffer.

**Code**

```python
class Buffer:
    def __init__(self, width=30, height=20):
        self.width = width
        self.height = height
        self.buffer = [' '] * (width * height)

    def __getitem__(self, item):
        return self.buffer.__getitem__(item)

    def write(self, text):
        self.buffer += text
```
**Viewport Class**: Acts as a view into a section of the buffer, allowing partial content to be displayed or manipulated.
- **Attributes**:
  - buffer: Holds a reference to a Buffer object.
  - offset: Represents the starting point of the viewport within the buffer (default: 0).
- **Methods**:
  - get_char_at(index): Retrieves a character from the buffer at a specified index, adjusted by the viewport's offset.
  - append(text): Appends text to the buffer.

```python
class Viewport:
    def __init__(self, buffer=Buffer()):
        self.buffer = buffer
        self.offset = 0

    def get_char_at(self, index):
        return self.buffer[self.offset + index]

    def append(self, text):
        self.buffer += text
```

**Console Class (Facade)**: Provides a high-level interface for interacting with the system by internally managing a buffer and viewport. It also exposes some lower-level functionality for advanced users.
- **Attributes**:
  - buffers: A list to manage multiple Buffer objects.
  - viewports: A list to manage multiple Viewport objects.
  - current_viewport: The active Viewport being used by the console.
- **Methods**:
  - write(text): High-level method that writes text to the current buffer via the viewport.
  - get_char_at(index): Low-level method that allows the user to directly access characters in the buffer.
Code:

```python
class Console:
    def __init__(self):
        b = Buffer()
        self.current_viewport = Viewport(b)
        self.buffers = [b]
        self.viewports = [self.current_viewport]

    # High-level method
    def write(self, text):
        self.current_viewport.buffer.write(text)

    # Low-level method
    def get_char_at(self, index):
        return self.current_viewport.get_char_at(index)
```
## Usage Example

The console can be used for both high-level operations (e.g., writing to the buffer) and low-level operations (e.g., retrieving characters from the buffer).
**Code**

```python
if __name__ == '__main__':
    c = Console()
    c.write('hello')  # High-level write
    ch = c.get_char_at(0)  # Low-level character access
    print(ch)  # Output: 'h'
```
## Key Concepts
- **High-Level API**: Provides a simple interface for common tasks, such as writing to the console, without requiring knowledge of the underlying structure (write method in Console).
- **Low-Level API**: Exposes detailed functionality for advanced users, allowing direct manipulation of buffer and viewport objects (get_char_at method in Console).
- **Buffer and Viewport**: Handle the complexity of managing text and presenting it in specific areas of the console.
- **Multifunctional Facade**: The Console class serves as both a simplified and powerful interface, providing flexibility for different user requirements.
