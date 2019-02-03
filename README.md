# SpringInterpolator (Facebook Rebound Rebuilt)

SpringInterpolator is an interpolater for animations. It is "reversed-engineered"  from facebook's [rebound library](https://github.com/facebook/rebound) (I did not look at their code 
🙈), to practise working with an ordinary differential equation (ODE) in software. At its core Runge-Kutta-4 is used to solve an ODE of second order.

```java
SpringInterpolator interpolator = new SpringInterpolator();

interpolator.addListener(this); // to receive the update events

interpolator.setFinalPosition(true); // causes the system to oscillate
```
