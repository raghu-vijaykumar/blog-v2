---
sidebar_title: Proxy Pattern
---

# Proxy Pattern 
The Proxy Design Pattern is a structural design pattern that provides an interface to another object while controlling access to it. Proxies can serve various purposes, such as controlling access, managing resource allocation, or adding additional functionality to an object without altering its interface.

## Types of Proxies
- **Communication Proxy**: Facilitates communication between objects that may be in different processes or environments, abstracting the complexities involved in remote method invocations.
- **Logging Proxy**: Adds logging capabilities to track method calls and interactions with the underlying object.
- **Virtual Proxy**: Delays the initialization of an object until it's actually needed, thereby saving resources and improving performance.
- **Protection Proxy**: Controls access to the object based on certain criteria or conditions, often used for security and access control.


## Protection Proxy
Scenario: A Car class with a method drive should only allow drivers above a certain age to drive.

Implementation:

- Car: Represents the real object with a method drive.
- CarProxy: Acts as a proxy to the Car class, adding access control to the drive method based on the driver's age.

```python
class Car:
    def __init__(self, driver):
        self.driver = driver

    def drive(self):
        print(f'Car being driven by {self.driver.name}')

class CarProxy:
    def __init__(self, driver):
        self.driver = driver
        self.car = Car(driver)

    def drive(self):
        if self.driver.age >= 16:
            self.car.drive()
        else:
            print('Driver too young')

class Driver:
    def __init__(self, name, age):
        self.name = name
        self.age = age

if __name__ == '__main__':
    car = CarProxy(Driver('John', 12))
    car.drive()
```
**Explanation**: The CarProxy class restricts access to the Car's drive method based on the driver's age, ensuring that only eligible drivers can operate the car.

## Virtual Proxy
Scenario: A Bitmap class that loads an image from a file but should delay this loading until the image is actually needed.

Implementation:
- **Bitmap**: Represents the real object that performs expensive operations like loading an image from disk.
- **LazyBitmap**: A proxy that defers the creation of a Bitmap object until its draw method is called.

```python
class Bitmap:
    def __init__(self, filename):
        self.filename = filename
        print(f'Loading image from {filename}')

    def draw(self):
        print(f'Drawing image {self.filename}')

class LazyBitmap:
    def __init__(self, filename):
        self.filename = filename
        self.bitmap = None

    def draw(self):
        if not self.bitmap:
            self.bitmap = Bitmap(self.filename)
        self.bitmap.draw()

def draw_image(image):
    print('About to draw image')
    image.draw()
    print('Done drawing image')

if __name__ == '__main__':
    bmp = LazyBitmap('facepalm.jpg')
    draw_image(bmp)
```
**Explanation**: The LazyBitmap class only initializes the Bitmap object when it is needed (i.e., when draw is called), avoiding the overhead of loading the image until it is actually used.


## Proxy vs Decorator Design Patterns
Both the Proxy and Decorator Design Patterns are structural patterns that can appear similar but serve different purposes and exhibit distinct behaviors. 

### Summary of Differences
- **Interface Handling**: A proxy provides the same interface as the object it proxies, while a decorator may provide an enhanced or extended interface.
- **Object Handling**: A proxy can manage objects that are not yet constructed, implementing lazy initialization. A decorator, on the other hand, works with an already constructed object and adds new behavior.
- **Functionality**: The proxy's primary role is to control access or manage resource usage, whereas the decorator focuses on adding new functionalities or operations.

In essence, while both patterns involve wrapping or acting as intermediaries for objects, the Proxy Pattern is more about controlling access or managing resource use, and the Decorator Pattern is about dynamically extending an object's capabilities.
