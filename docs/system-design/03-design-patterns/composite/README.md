---
sidebar_title: Composite Pattern
---
# Composite Pattern
The Composite Design Pattern is a structural design pattern that allows you to treat individual objects and compositions of objects uniformly. This pattern provides a way to group objects into tree structures to represent part-whole hierarchies. Composite lets clients treat individual objects and compositions of objects uniformly, thereby simplifying the client code.

## Motivation
In software development, there are scenarios where you need to work with both individual objects and collections of objects. For example, in a graphics application, you might have simple shapes like circles and squares, but you might also want to group these shapes together to form complex structures.

The Composite Design Pattern enables you to treat both individual objects (e.g., a circle or a square) and groups of objects (e.g., a group of shapes) uniformly. This pattern allows you to define a common interface for all objects in the composition, so clients can interact with both individual components and composites through the same interface.

## Implementation
### Base Class
The base class, GraphicObject, serves as the foundation for both individual components (e.g., Circle and Square) and composite objects (e.g., a group of shapes). It contains a list of children that represent the group of objects and a color property.
```py
class GraphicObject:
    def __init__(self, color=None):
        self.color = color
        self.children = []
        self._name = 'Group'

    @property
    def name(self):
        return self._name

    def _print(self, items, depth):
        items.append('*' * depth)
        if self.color:
            items.append(self.color)
        items.append(f'{self.name}\n')
        for child in self.children:
            child._print(items, depth + 1)

    def __str__(self):
        items = []
        self._print(items, 0)
        return ''.join(items)
```

### Concrete Classes

Concrete classes like Circle and Square inherit from GraphicObject and override the name property to return their specific type. These classes can be treated as both individual objects and as part of a composite object.

```py
class Circle(GraphicObject):
    @property
    def name(self):
        return 'Circle'

class Square(GraphicObject):
    @property
    def name(self):
        return 'Square'
```

### Example Usage
Below is an example of how you can use the Composite Design Pattern to create and manipulate both individual shapes and groups of shapes.

```py
if __name__ == '__main__':
    drawing = GraphicObject()
    drawing._name = 'My Drawing'
    drawing.children.append(Square('Red'))
    drawing.children.append(Circle('Yellow'))

    group = GraphicObject()  # no name
    group.children.append(Circle('Blue'))
    group.children.append(Square('Blue'))
    drawing.children.append(group)

    print(drawing)
```

### Output
The output of the above code will display a hierarchy of the shapes and groups within the drawing:

```sh
*My Drawing
**Red
**Square
**Yellow
**Circle
**Group
***Blue
***Circle
***Blue
***Square
```

## Neural Network Example
This documentation describes an implementation of the Composite Design Pattern in the context of modeling a neural network. The pattern is used to treat individual neurons and layers of neurons uniformly, allowing them to be connected seamlessly. This approach simplifies the handling of connections within a neural network by making scalar elements behave like collections.

### Key Components

1. Connectable Class
- Purpose: Provides a base class that allows any derived class to connect to other instances of Connectable. Implements the Iterable interface, enabling the uniform treatment of single elements and collections.
- Methods:
  - connect_to(self, other): Connects the current instance (self) to another Connectable instance (other). Iterates over elements of self and other, creating two-way connections between them.
  - __iter__(self): Defines iteration over instances, yielding self in the case of scalar objects.
2. Neuron Class
- Purpose: Represents a single neuron within a neural network. Inherits from Connectable to enable connections with other neurons or layers of neurons.
- Attributes:
  - name: The name of the neuron.
  - inputs: A list of neurons connected as inputs to this neuron.
  - outputs: A list of neurons connected as outputs from this neuron.
- Methods:
  - __init__(self, name): Initializes a neuron with a name, and empty input/output lists.
  - __str__(self): Returns a string representation of the neuron, including its name, the number of inputs, and the number of outputs.
3. NeuronLayer Class
- Purpose: Represents a layer of neurons. Inherits from both list and Connectable, allowing it to behave like a list of neurons while also supporting connections.
- Attributes:
  - name: The name of the neuron layer.
- Methods:
  - __init__(self, name, count): Initializes a neuron layer with a specified number of neurons. Each neuron in the layer is named according to the layer's name and its index.
  - __str__(self): Returns a string representation of the neuron layer, including its name and the number of neurons it contains.

### Example Usage
#### Connecting Neurons and Layers
The following code demonstrates how to create neurons and layers, and connect them using the connect_to method:

```py
if __name__ == '__main__':
    neuron1 = Neuron('n1')
    neuron2 = Neuron('n2')
    layer1 = NeuronLayer('L1', 3)
    layer2 = NeuronLayer('L2', 4)

    neuron1.connect_to(neuron2)
    neuron1.connect_to(layer1)
    layer1.connect_to(neuron2)
    layer1.connect_to(layer2)

    print(neuron1)
    print(neuron2)
    print(layer1)
    print(layer2)
```

#### Output
This example produces the following output:

```sh
n1, 0 inputs, 4 outputs
n2, 4 inputs, 0 outputs
L1 with 3 neurons
L2 with 4 neurons
```

#### Explanation
- Neuron 1 (n1) is connected to Neuron 2 (n2) and Layer 1 (L1). This results in n1 having 4 outputs (3 to L1 and 1 to n2), while n2 has 4 inputs (3 from L1 and 1 from n1).
- Layer 1 (L1) is connected to Layer 2 (L2), creating connections between all neurons in L1 and L2.
- The connect_to method in Connectable enables this flexibility, allowing neurons and layers to be interconnected regardless of their types.
