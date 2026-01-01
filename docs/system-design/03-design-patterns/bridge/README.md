---
sidebar_title: Bridge Pattern
---
# Bridge Pattern
The Bridge design pattern is used to decouple an abstraction from its implementation, allowing both to vary independently. This pattern is particularly useful when dealing with a potential "Cartesian product complexity explosion," which occurs when multiple abstractions and implementations multiply the number of classes needed.

## Code Structure
- **Renderer Interface**: 
  - **Renderer**: An abstract base class that defines the method render_circle(radius). This method serves as the interface for rendering shapes.
- Concrete Implementations of Renderer:
  - **VectorRenderer**: A concrete implementation of Renderer. It provides a specific implementation for rendering circles in a vector form.
  - **RasterRenderer**: Another concrete implementation of Renderer. It renders circles in a raster (pixel-based) form.
- **Shape Base Class**:
  - **Shape**: An abstract base class that requires a Renderer to be passed during initialization. This class defines the methods draw() and resize(factor), which are intended to be overridden by derived classes.
- **Concrete Shape Implementation**:
  - **Circle**: A concrete implementation of the Shape class. It includes a radius attribute and provides specific implementations for the draw() and resize(factor) methods.
    - `draw()`: Uses the associated Renderer to render the circle based on its radius.
    - `resize(factor)`: Resizes the circle by multiplying its radius by the given factor.
- **Main Execution**: The script demonstrates the usage of the Bridge pattern by creating instances of VectorRenderer and RasterRenderer, and then using these renderers to draw and resize a Circle.

## Code Example
```python
class Renderer:
    def render_circle(self, radius):
        pass

class VectorRenderer(Renderer):
    def render_circle(self, radius):
        print(f'Drawing a circle of radius {radius}')

class RasterRenderer(Renderer):
    def render_circle(self, radius):
        print(f'Drawing pixels for circle of radius {radius}')

class Shape:
    def __init__(self, renderer):
        self.renderer = renderer

    def draw(self): 
        pass

    def resize(self, factor): 
        pass

class Circle(Shape):
    def __init__(self, renderer, radius):
        super().__init__(renderer)
        self.radius = radius

    def draw(self):
        self.renderer.render_circle(self.radius)

    def resize(self, factor):
        self.radius *= factor

if __name__ == '__main__':
    raster = RasterRenderer()
    vector = VectorRenderer()
    circle = Circle(vector, 5)
    circle.draw()
    circle.resize(2)
    circle.draw()
```
## Usage
- **Renderers**: The VectorRenderer and RasterRenderer provide different ways to render the Circle.
- **Shapes**: The Circle class can be extended or modified without altering the renderer classes, showcasing the flexibility provided by the Bridge pattern.
## Advantages
- **Decoupling**: The Bridge pattern decouples abstraction (Shape) from implementation (Renderer), enabling both to evolve independently.
- **Reduced Complexity**: By separating concerns, the pattern avoids a complexity explosion that could result from combining multiple abstractions and implementations.
## Limitations
- **Open/Closed Principle**: The pattern requires modifications to existing classes (e.g., adding new methods to Renderer and its subclasses) when new shapes or renderers are introduced, which might violate the open/closed principle.