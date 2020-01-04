package de.osanj.springinterpolator;

import java.util.ArrayList;
import java.util.List;

/**
 * The SpringInterpolator is a rebuilt of Facebook's Rebound library.
 * It is supposed to be used in animations, where a transition from a start-value to an end-value (e.g. scale 50% -> 100%)
 * has to be performed. SpringInterpolator implements the representation of an mechanical model and will provide animations
 * with its (natural) motion/oscillation. Scroll down for a exemplified source code.
 * <br>
 * <br>
 * <b>Setting The System In Motion</b>
 * <br>
 * Mentioned model has two potential idle positions ("bottom" and "top"). It moves between them if stimulated via 
 * {@link #setFinalPosition(boolean) setFinalPosition}.
 * <br>
 * <br>
 * <b>Customizing The Curve</b>
 * <br>
 * The stiffness of the spring and dampening of the damper used in the model will change the motion
 * of the system. Use {@link #setStiffness(float) setStiffness} and {@link #setDampening(float) setDampening}
 * to manipulate those values.
 * <br>
 * <br>
 * <b>Mapping The Duration</b>
 * <br>
 * Unlike other interpolators SpringInterpolator has no fixed duration. However, you can set a rough estimation
 * of the duration with {@link #setApproximateDuration(float) setApproximateDuration} which changes the amount
 * of steps that are calculated for each update-cycle.
 * <br>
 * <br>
 * <b>Obtaining The Interpolated Value</b>
 * <br>
 * You receive the value of each update-cycle as well as the event that a final stage is reached
 * by implementing {@link OnSpringUpdateListener}.
 * <br>
 * <br>
 * For more information and details look up:
 * <a href="https://osanj.github.io/post/spring-dynamics-interpolation/">https://osanj.github.io/post/spring-dynamics-interpolation/</a>
 * <br>
 * <br>
 * <br>
 * <u>How To Use:</u>
 * <br>
 * <pre>class ButtonAnimator implements OnSpringUpdateListener, OnClickListener{
 *
 *	private SpringInterpolator interpolator;
 *	private Button button;
 *
 *	public ButtonAnimator(Button button){
 *		this.button = button;
 *		
 *		// new interpolator with 30fps update-cycle
 *		interpolator = new SpringInterpolator(30);
 *		
 *		// customize curve and duration
 *		interpolator.setStiffness(5f);
 *		interpolator.setDampening(1f);
 *		interpolator.setApproximateDuration(500); // 500ms
 *		
 *		// setting interfaces
 *		button.addOnClickListener(this);
 *		interpolator.addListener(this);
 *	}
 *	
 *	{@literal @}Override
 *	public void onClick(){
 *		interpolator.setFinalPosition(true);
 *	}
 *	
 *	{@literal @}Override
 *	public void onSpringUpdate(SpringInterpolator interpolator, float interpolatedValue){
 *		// <b>do animating here!</b>
 *	}
 *	
 *	{@literal @}Override
 *	public void onSpringFinalPosition(SpringInterpolator interpolator, float finalInterpolatedValue, boolean finalPosition){
 *		// <b>do final stuff here!</b>
 *	} 
 * }
 * </pre>
 */
public class SpringInterpolator {
	
	public static final float MAX_D = 10f;              // maximal value for the dampening
	public static final float MIN_D = 0.1f;             // minimal value for the dampening
	public static final float MAX_K = 20f;              // maximal value for the stiffness
	public static final float MIN_K = 0.1f;             // minimal value for the stiffness
	public static final float MAX_REAL_DURATION = 5000; // maximal value for the real-time-mapping (in ms)
	public static final float MIN_REAL_DURATION = 100;  // minimal value for the real-time-mapping (in ms)
	
	private static final float H = 0.02f;                // step-size
	private static final float SIM_DUR = 5f;             // in s, for transforming from realtime (1000ms) to simulationtime (5s)
	private static final float OBS_TOL = 0.01f;          // tolerance for determining if end position is (permanently) reached
	private static final int OBS_COUNT = (int) (2 / H);  // how many values consecutively have to be within the tolerance
	private float duration = 1000f;                      // in ms, realtime which the simulation is mapped to
	
	private SpringSystem sys;
	private boolean steadyState;
	private boolean[] tolerances;
	private int tolerancesPos;	
	
	private int updateRateFps;
	private UpdateLoop looper;
	private Thread looperThread;
	private List<OnSpringUpdateListener> listeners;
	
	
	/**
	 * Standard SpringInterpolator from start position "bottom" with an update-rate of 60fps.
	 */
	public SpringInterpolator() {
		this(60, false);
	}
	
	/**
	 * SpringInterpolator from start position "bottom".
	 * @param updateRateFps	update-period in FramesPerSecond
	 */
	public SpringInterpolator(int updateRateFps) {
		this(updateRateFps, false);
	}
	
	/**
	 * Set both start position and update-rate.
	 * @param updateRateFps		update-period in FramesPerSecond
	 * @param currentPosition	starting position of the system
	 */
	public SpringInterpolator(int updateRateFps, boolean currentPosition) {
		this.updateRateFps = updateRateFps;
		
		sys = new SpringSystem(currentPosition);
		listeners = new ArrayList<OnSpringUpdateListener>();
		steadyState = false;
		
		tolerances = new boolean[OBS_COUNT];
		resetToleranceObservation();
		tolerancesPos = 0;
		
		// starting thread with runnable that calls onUpdate all 1000/updateRateFps milliseconds
		startLooper();
	}
	
	private void onUpdate(long pauseMillis){
		// physical model/setup is "moving" between 1 to 6 seconds
		// a usual duration for an animation is 1000ms
		// -> mapping curve from 5s to 1000ms (standard)
		
		if(!steadyState){
			
			// pauseMillis is the time since the last computation
			// mapping from real-time to simulation-time, e.g. 16ms (realtime) -> 0.08s (simtime for spring system)
			float mappedTimeStep = pauseMillis / duration * SIM_DUR;
			
			
			synchronized(sys){
				// computing spring-system with step-size H
				while(mappedTimeStep > H){
					updateToleranceObservation(sys.updateSystem(H));
					mappedTimeStep -= H;
				}
				
				// computing spring-system with remaining step-size
				updateToleranceObservation(sys.updateSystem(mappedTimeStep));
			}
			
			
			if(!isWithinTolerance()){
				dispatchUpdate(getCurrentInterpolatedValue());
				
			}else{
				// stop updates if steady-state is reached
				steadyState = true;
				dispatchFinalUpdate();
			}
		}
	}
	
	private void startLooper(){
		resetToleranceObservation();
		
		looper = new UpdateLoop(this, updateRateFps);
		looperThread = new Thread(looper);
		looperThread.start();
	}

	private void dispatchUpdate(float interpolatedValue){
		for(OnSpringUpdateListener listener : listeners)
			listener.onSpringUpdate(this, interpolatedValue);
	}
	
	private void dispatchFinalUpdate(){
		boolean finalPosition = sys.getU();
		float finalInterpolatedValue = getCurrentInterpolatedValue();
		
		for(OnSpringUpdateListener listener : listeners){
			listener.onSpringFinalPosition(this, finalInterpolatedValue, finalPosition);
		}
	}
	
	private void updateToleranceObservation(float x){
		float dest = sys.getU() ? 1 : 0;
		float diff = Math.abs(dest - x / sys.getXe());
		
		if(diff > OBS_TOL){
			tolerances[tolerancesPos] = false;
		}else{
			tolerances[tolerancesPos] = true;
		}
		
		tolerancesPos++;
		
		if(tolerancesPos >= OBS_COUNT){
			tolerancesPos = 0;
		}
	}
	
	private void resetToleranceObservation(){
		for(int i = 0 ; i < OBS_COUNT; i++){
			tolerances[i] = false;
		}
	}
	
	private boolean isWithinTolerance(){
		for(int i = 0 ; i < OBS_COUNT; i++){
			if(!tolerances[i]){
				return false;
			}
		}
		
		return true;
	}
	
	public void addListener(OnSpringUpdateListener listener){
		listeners.add(listener);
	}
	
	public void removeListener(OnSpringUpdateListener listener){
		listeners.remove(listener);
	}
	
	public void removeAllListeners(){
		for(int i = 0, len = listeners.size(); i < len; i++) {
			listeners.remove(i);
		}
	}

	/**
	 * Current normed value of the model (usually something between/around 0 and 1).
	 * @return current value of the interpolation
	 */
	public float getCurrentInterpolatedValue(){
		if(!reachedFinalPositionPermanently()){
			return sys.getX() / sys.getXe(); // normalize x
		
		}else{
			if(sys.getU()){
				return 1;
			}else{
				return 0;
			}
		}
	}
	
	/**
	 * If the velocity and deviation is really small, it is determined that the final position is reached
	 * permanently. That means there will no be further motion/updates without stimulation (idle state).
	 * This returns true after {@link OnSpringUpdateListener#onSpringFinalPosition} has been fired.
	 * <br>
	 * <b>Note:</b> Of course it can be stimulated again. For example by using
	 * {@link #setFinalPosition(boolean) setFinalPosition} and the inverse current position
	 * as argument ({@link #getFinalPosition() !getFinalPosition}).
	 * 
	 * @return true if the model is idle
	 */
	public boolean reachedFinalPositionPermanently(){
		return steadyState;
	}
	
	/**
	 * Currently set final position.
	 * <br>
	 * <b>Note:</b> This does not indicate, that there will be no further motion/updates.
	 * Use {@link #reachedFinalPositionPermanently() reachedFinalPositionPermanently}
	 * to determine if motion can still be expected
	 * @return current final position (false ~ "bottom", true ~ "top")
	 */
	public boolean getFinalPosition(){
		return sys.getU();
	}
	
	/**
	 * Sets final position. This causes the system to oscillate <b>if</b> it has not (permanently) reached the given
	 * end position yet (idle state).
	 * That can be checked with {@link #reachedFinalPositionPermanently() reachedFinalPositionPermanently}.
	 * @param top	final position (false ~ "bottom", true ~ "top")
	 */
	public void setFinalPosition(boolean top){
		setFinalPosition(top, false);
	}
	
	/**
	 * Sets final position. This causes the system to oscillate <b>if</b> it has not (permanently) reached the given
	 * end position yet (idle state).
	 * That can be checked with {@link #reachedFinalPositionPermanently() reachedFinalPositionPermanently}.
	 * <br>
	 * With <code>skipMotion</code> idle state is instantly reached which means there will be no updates
	 * and {@link #reachedFinalPositionPermanently() reachedFinalPositionPermanently} returns <code>true</code>.
	 * @param top			final position (false ~ "bottom", true ~ "top")
	 * @param skipMotion	to instantly reach idle-state
	 */
	public void setFinalPosition(boolean top, boolean skipMotion){
		if(top != sys.getU()){
			
			synchronized(sys){
				sys.setU(top, skipMotion);
			}
			
			if(skipMotion){
				if(!reachedFinalPositionPermanently()){
					steadyState = true;
				}
				
			}else{
				if(reachedFinalPositionPermanently()){
					steadyState = false;
				}
			}
		}
	}

	/**
	 * The "real" duration of the simulation (using the standard values) is about 5 seconds.
	 * Since the simulation-time is independent of the real-time (<i>you could calculate the first value today,
	 * the second tomorrow, the third in a week, ...</i>) it can be mapped. The argument <code>duration</code>
	 * basically determines in which time each value within a simulation-time of 5 seconds has to be computed.
	 * <br>
	 * <b>Note:</b> It is just an approximation. It is especially unreliable for extreme configurations,
	 * e.g. d = {@value #MIN_D} (min) and k = {@value #MAX_K} (max).
	 * 
	 * @param duration		in milliseconds (must be between {@value #MIN_REAL_DURATION} and {@value #MAX_REAL_DURATION})
	 */
	public void setApproximateDuration(float duration){
		if(duration >= MIN_REAL_DURATION && duration <= MAX_REAL_DURATION) {
			this.duration = duration;
		}
	}
	
	public float getApproximateDuration(){
		return duration;
	}
	
	/**
	 * Sets the stiffness (k) of a spring in the model. Here are the tendencies:
	 * <br>
	 * <ul>
	 * <li><b>increase</b> k: longer oscillation, higher amplitude</li>
	 * <li><b>decrease</b> k: shorter oscillation, lower/no amplitude</li>
	 * </ul>
	 * @param k		stiffness (must be between {@value #MIN_K} and {@value #MAX_K})
	 */
	public void setStiffness(float k){
		if(k > MIN_K && k < MAX_K){
			synchronized(sys){
				sys.setK(k);
			}
		}
	}
	
	public float getStiffness(){
		return sys.getK();
	}
	
	/**
	 * Sets the dampening (d) of a damper in the model. Here are the tendencies:
	 * <br>
	 * <ul>
	 * <li><b>increase</b> d: shorter oscillation, lower/no amplitude</li>
	 * <li><b>decrease</b> d: longer oscillation, higher amplitude</li>
	 * </ul>
	 * @param d		dampening (must be between {@value #MIN_D} and {@value #MAX_D})
	 */
	public void setDampening(float d){
		if(d > MIN_D && d < MAX_D){
			synchronized(sys){
				sys.setD(d);
			}
		}
	}

	public float getDampening(){
		return sys.getD();
	}
	
	
	public class UpdateLoop implements Runnable {
		
		private SpringInterpolator interpolator;
		private long pauseMillis;
		private boolean run;
		
		
		public UpdateLoop(SpringInterpolator interpolator, int updateRateFps) {
			this.interpolator = interpolator;
			pauseMillis = (long) (1000 / updateRateFps);
			
			run = true;
		}
		
		public void end(){
			run = false;
		}
		
		@Override
		public void run(){
			long prevMillis = System.currentTimeMillis() - pauseMillis;
			long sleptMillis;
			long tempMillis;
			
			/*
			 * calculating the actually slept time to correctly compute next value, consider
			 * 
			 * 		x---0->x
			 * 
			 * with x being a time step and 0 the correct one for the first x. That means:
			 * The thread slept longer than he should have had. So not the ideal (pauseMillis), but the real (sleptMillis)
			 * timestep is passed back for the next update...
			 */
			
			while(run){
				tempMillis =  System.currentTimeMillis();
				sleptMillis = tempMillis - prevMillis;
				prevMillis = tempMillis;
				
				try {
					Thread.sleep(pauseMillis);
				} catch (InterruptedException e) {}
				
				interpolator.onUpdate(sleptMillis);
			}
		}
	}
}
