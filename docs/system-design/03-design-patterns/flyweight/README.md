---
sidebar_title: Flyweight Pattern
---

# Flyweight Pattern

Flyweight Design Pattern, a space optimization technique used to minimize memory consumption in scenarios where multiple objects share similar data. By externalizing and reusing common data, the Flyweight Pattern ensures that only a small number of objects are instantiated, reducing redundancy and optimizing resource usage.

## Problem Statement
The goal is to prevent redundant memory usage when storing common or repetitive data across multiple objects. For example, in a massively multiplayer online game or a text editor, many users may have similar names, and it would be inefficient to store identical or similar strings repeatedly.

To address this, the Flyweight Pattern externalizes the common data and stores references to it, rather than storing the data directly in each object.

## Example Scenario
Consider a system where 10,000 users are created, and each user has a first and last name. Without optimization, the system would store each full name as a unique string, resulting in significant memory overhead. Even though there are only 200 unique first and last names (100 first names and 100 last names), the traditional approach leads to the storage of 10,000 full names.

The Flyweight Pattern reduces memory usage by storing only 200 unique strings and referencing them via indices, rather than duplicating the full names for each user.

### Implementation
The following implementation demonstrates the Flyweight Design Pattern using Python:

**User Class (Inefficient Implementation)**:
This class stores the full name of each user as a single string.
Memory consumption increases with each new user, even if the first and last names are identical.
```python
class User:
    def __init__(self, name):
        self.name = name
```
**User2 Class (Flyweight Implementation)**:
This class optimizes memory usage by storing first and last names externally in a static list (strings) and referencing them via indices.
When a user object is created, the first and last names are split, and their indices are stored instead of the actual strings.
- The get_or_add function checks if the name is already stored. If it is, it returns the index; otherwise, it adds the name to the list and returns the new index.
- The __str__ method reconstructs the full name by concatenating the indexed first and last names from the static list.
```python
class User2:
    strings = []

    def __init__(self, full_name):
        def get_or_add(s):
            if s in self.strings:
                return self.strings.index(s)
            else:
                self.strings.append(s)
                return len(self.strings)-1
        self.names = [get_or_add(x) for x in full_name.split(' ')]

    def __str__(self):
        return ' '.join([self.strings[x] for x in self.names])
```

**Random Name Generation**:
A function random_string is used to generate random first and last names of length 8, simulating user names for the purpose of the experiment.
```python
def random_string():
    chars = string.ascii_lowercase
    return ''.join([random.choice(chars) for x in range(8)])
```
**Main Program**:
The program creates 10,000 users using the inefficient User class and then optimizes memory usage by switching to the User2 class, which utilizes the Flyweight Pattern.

```python
if __name__ == '__main__':
    users = []

    first_names = [random_string() for x in range(100)]
    last_names = [random_string() for x in range(100)]

    for first in first_names:
        for last in last_names:
            users.append(User(f'{first} {last}'))

    u2 = User2('Jim Jones')
    u3 = User2('Frank Jones')
    print(u2.names)
    print(u3.names)
    print(User2.strings)

    users2 = []

    for first in first_names:
        for last in last_names:
            users2.append(User2(f'{first} {last}'))
```
### Benefits of Flyweight Pattern
- **Memory Optimization**: Instead of creating new strings for each user, the Flyweight Pattern reuses existing strings by referencing them via indices. This reduces memory consumption significantly.
- **Efficient Storage**: Only 200 unique strings are stored, regardless of the number of users. The full names are reconstructed dynamically when needed, saving memory and reducing overhead.

### Example Output
- The code first prints the indices corresponding to the first and last names for two users: 'Jim Jones' and 'Frank Jones'.
- It also prints the shared static list of unique strings (User2.strings), which stores the first and last names.
This approach demonstrates how the Flyweight Pattern effectively reduces redundant memory usage in scenarios with repetitive data.


## Text Formatting Example

When formatting large amounts of text, such as capitalizing certain letters or words, allocating resources for each character in the text can become inefficient, especially when only a small subset of the text needs formatting.

For example, in a naive implementation, formatting operations (e.g., capitalizing letters) are applied using an array of boolean values to track whether each character in the text should be capitalized. This approach consumes unnecessary memory when formatting operations are sparse across large texts.

The Flyweight pattern offers an optimized solution by sharing the formatting information across text segments, reducing memory usage.

### Naive Approach
**FormattedText Class**: A simple approach to formatting text, where a boolean array tracks which characters need to be capitalized.
- Initialization: Takes a string of plain text as input and creates an array of booleans (caps), initialized to False.
- Capitalization: Uses the capitalize() method to update the boolean array for characters within the specified range.
- String Representation: The __str__() method checks the boolean array when generating the final formatted string.

```python
class FormattedText:
    def __init__(self, plain_text):
        self.plain_text = plain_text
        self.caps = [False] * len(plain_text)

    def capitalize(self, start, end):
        for i in range(start, end):
            self.caps[i] = True

    def __str__(self):
        result = []
        for i in range(len(self.plain_text)):
            c = self.plain_text[i]
            result.append(c.upper() if self.caps[i] else c)
        return ''.join(result)
```
#### Example Usage:
```python
ft = FormattedText('This is a brave new world')
ft.capitalize(10, 15)
print(ft)
```
**Output:**

```sh
This is a BRAVE new world
```
### Optimized Approach with Flyweight Pattern
**BetterFormattedText Class**: Optimizes memory usage by applying formatting only to the specified ranges of text, using the Flyweight pattern.
- **TextRange (Flyweight Class)**: Defines a range of text positions and tracks whether characters in the range should be capitalized, bold, italicized, etc.
- **Flyweight Management**: The `get_range()` method creates and returns TextRange objects that define formatting operations on specific segments of the text, which are then stored in a formatting list.
- **String Representation**: The `__str__()` method processes each character and checks if it falls within a TextRange that requires formatting.

```python
class BetterFormattedText:
    def __init__(self, plain_text):
        self.plain_text = plain_text
        self.formatting = []

    class TextRange:
        def __init__(self, start, end, capitalize=False, bold=False, italic=False):
            self.start = start
            self.end = end
            self.capitalize = capitalize

        def covers(self, position):
            return self.start <= position <= self.end

    def get_range(self, start, end):
        range = self.TextRange(start, end)
        self.formatting.append(range)
        return range

    def __str__(self):
        result = []
        for i in range(len(self.plain_text)):
            c = self.plain_text[i]
            for r in self.formatting:
                if r.covers(i) and r.capitalize:
                    c = c.upper()
            result.append(c)
        return ''.join(result)
```
#### Example Usage:
```python
bft = BetterFormattedText('This is a brave new world')
bft.get_range(16, 19).capitalize = True
print(bft)
```
**Output**

```sh
This is a brave NEW world
```
#### Key Differences
- **Memory Usage**: The naive approach `FormattedText` allocates a boolean array with the same length as the text, which can be inefficient for large texts with sparse formatting. The optimized approach `BetterFormattedText` only stores formatting data for specific ranges, reducing memory consumption.
- **Flyweight Design**: By utilizing TextRange objects, the Flyweight pattern ensures that formatting information is shared across text segments and not unnecessarily duplicated.
