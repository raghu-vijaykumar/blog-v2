---
sidebar_title: Adapter Pattern
---

# Adapter  Pattern

The Adapter Design Pattern allows objects with incompatible interfaces to work together. It acts as a bridge between two interfaces, adapting one interface to another that the client expects.

## Problem Statement
When building software systems, it is common to encounter situations where an existing class or component has an interface that does not match the interface required by a specific client. Modifying the existing class to meet the new interface could lead to breaking other parts of the system or complicating the code unnecessarily. In such cases, the Adapter Pattern provides a solution by creating a wrapper (adapter) around the existing class to adapt its interface to the expected one.

## Real-World Analogy
A travel adapter is a perfect analogy for the Adapter Pattern. Electrical devices have different plug types and voltage requirements based on the country they are designed for. Instead of modifying each device to fit into different outlets, a travel adapter converts the plug type to fit the outlet and allows the device to function properly.

Participants
- Target (Client Interface): The interface expected by the client.
- Adaptee: The existing interface that needs to be adapted.
- Adapter: The class that implements the Target interface and adapts the Adaptee to the Target.

## Example Implementation
Consider a scenario where you are given a legacy API that includes a Point class and a draw_point function for drawing points. However, you are working with a system where graphical objects are represented by lines and rectangles.

Given API
```python
class Point:
    def __init__(self, x, y):
        self.x = x
        self.y = y

def draw_point(p):
    print('.', end='')
```

Working Classes
```python
class Line:
    def __init__(self, start, end):
        self.start = start
        self.end = end

class Rectangle(list):
    def __init__(self, x, y, width, height):
        super().__init__()
        self.append(Line(Point(x, y), Point(x + width, y)))
        self.append(Line(Point(x + width, y), Point(x + width, y + height)))
        self.append(Line(Point(x, y), Point(x, y + height)))
        self.append(Line(Point(x, y + height), Point(x + width, y + height)))
```
**Adapter Implementation**
The `LineToPointAdapter` adapts the Line class to be compatible with the draw_point function by converting the line into a series of points.

```py
class LineToPointAdapter(list):
    count = 0

    def __init__(self, line):
        self.count += 1
        print(f'{self.count}: Generating points for line '
              f'[{line.start.x},{line.start.y}]→'
              f'[{line.end.x},{line.end.y}]')

        left = min(line.start.x, line.end.x)
        right = max(line.start.x, line.end.x)
        top = min(line.start.y, line.end.y)
        bottom = max(line.start.y, line.end.y)

        if right - left == 0:
            for y in range(top, bottom + 1):
                self.append(Point(left, y))
        elif line.end.y - line.start.y == 0:
            for x in range(left, right + 1):
                self.append(Point(x, top))
```
Usage Example
The `draw` function can now work with rectangles by using the `LineToPointAdapter` to convert lines into points.

```py
def draw(rcs):
    print("\n\n--- Drawing some stuff ---\n")
    for rc in rcs:
        for line in rc:
            adapter = LineToPointAdapter(line)
            for p in adapter:
                draw_point(p)

if __name__ == '__main__':
    rs = [
        Rectangle(1, 1, 10, 10),
        Rectangle(3, 3, 6, 6)
    ]
    draw(rs)
    draw(rs)
```

The Adapter Pattern is a useful design pattern when you need to integrate classes with incompatible interfaces. It allows you to reuse existing code without modifying it, thereby promoting flexibility and reusability in software design.

## Implementing a Cache
A cache is a data structure that stores the results of expensive function calls and reuses those results when the same inputs occur again. By implementing a cache in the adapter, we can avoid regenerating temporary objects, improving the efficiency of the code.

Modifying the Adapter to Use a Cache: The LineToPointAdapter class is modified to include a cache, which is a dictionary that maps hash values of Line objects to their corresponding points. This way, if a Line has already been converted to points, the adapter can retrieve the points from the cache instead of regenerating them.

### Steps in the Cache Implementation:
**Calculate Hash Value**: The adapter first calculates a unique hash value for the Line object. This hash value is used as the key in the cache dictionary.

```python
self.h = hash(line)
```
**Check Cache**: Before generating points, the adapter checks if the points for the given line are already present in the cache.

```python
if self.h in self.cache:
    return
```
**Generate Points if Not Cached**: If the points are not in the cache, the adapter generates them and stores them in the cache.

```python
points = []
# (Point generation logic)
self.cache[self.h] = points
```
**Iterate Over Cached Points**: The __iter__ method is overridden to return an iterator over the cached points.

```python
def __iter__(self):
    return iter(self.cache[self.h])
```

By incorporating caching into the adapter, we can significantly reduce the number of temporary objects generated, thereby improving the performance and efficiency of the system. This approach is especially beneficial when the adapter is repeatedly invoked with the same data.