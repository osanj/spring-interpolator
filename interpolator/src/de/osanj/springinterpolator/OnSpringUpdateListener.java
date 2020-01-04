package de.osanj.springinterpolator;

/**
 * The Listener to receive the events from an associated {@link SpringInterpolator}.
 * 
 * @version	1.0
 * 
 */
public interface OnSpringUpdateListener {
	
	/**
	 * Provides receiver with the current value of the interpolation (usually something between/around 0 and 1).
	 * Here is the place where the actual updates (e.g. increasing scale) for the animation need to be done!
	 * Also, don't forget to call the respective repaint-method.
	 * 
	 * @param interpolator			reference to the interpolator
	 * @param interpolatedValue		current interpolated value
	 */
	public void onSpringUpdate(SpringInterpolator interpolator, float interpolatedValue);
	
	/**
	 * Notifies receiver that the model has reached an idle state. This means there will be no further motion/updates
	 * without stimulation via {@link SpringInterpolator#setFinalPosition(boolean) setFinalPosition}. Also, from this point on
	 * {@link SpringInterpolator#reachedFinalPositionPermanently() reachedFinalPositionPermanently} will return
	 * <code>true</code>.
	 * 
	 * @param interpolator				reference to the interpolator
	 * @param finalInterpolatedValue	interpolated value (0 or 1)
	 * @param finalPosition				reached position (false ~ "bottom", true ~ "top")
	 */
	public void onSpringFinalPosition(SpringInterpolator interpolator, float finalInterpolatedValue, boolean finalPosition);
}
