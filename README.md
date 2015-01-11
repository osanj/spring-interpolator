#SpringInterpolator (Facebook Rebound Rebuilt)

SpringInterpolator is an interpolater for animations. It's "reversed-engineered" from facebook's [rebound library](https://github.com/facebook/rebound), to practise working with an ODE in software...</p>

For a detailed documentation of my approach look up [my blog](http://www.anotherblogger.de/blog/article/9). How to use it:

```java
SpringInterpolator interpolator = new SpringInterpolator();

interpolator.addListener(this);			// to receive the update events

interpolator.setFinalPosition(true);	// causes the system to oscillate
```
