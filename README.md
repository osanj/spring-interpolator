# Spring-Interpolator (Rebuilding Facebook Rebound)

SpringInterpolator is an interpolater for animations. It is "reversed-engineered" from facebook's [rebound library](https://github.com/facebook/rebound) (I did not look at their code!). The main component of this project is an ordinary differential equation of second order and a Runge-Kutta-4 solver. Details can be found in my [blog article](https://osanj.github.io/post/spring-dynamics-interpolation/).

To use this code implement the listener interface. Also take a look at the example application.

```java
SpringInterpolator interpolator = new SpringInterpolator();
interpolator.addListener(this); // to receive the update events
interpolator.setFinalPosition(true); // causes the system to oscillate
```
