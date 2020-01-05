package de.osanj.springinterpolator;

/**
 * Representation of a mechanical model (mass, 2x springs, 2x dampers). Uses Runge-Kutta 4 to solve the associated ODE of 2nd order.
 * Controlled from a SpringInterpolator object, which uses its output to provide an interpolated value.
 * Interpolator refers here to an algorithm controlling the course of an animation (step, linear, accelerating, ...).
 * For more information and details look up:
 * <a href="https://osanj.github.io/post/spring-dynamics-interpolation/">https://osanj.github.io/post/spring-dynamics-interpolation/</a>
 */
public class SpringSystem {
	/*
	 * simulink schema
	 * 
	 * input
	 * by user
	 * +-----+    +------+    +---+        a   +-------+  v   +-------+  x
	 * |  u  |-+->|  ku  |--->| + |----------->|  1/s  |--+-->|  1/s  |--+-->
	 * +-----+ |  +------+    |   |            +-------+  |   +-------+  |
	 *         |              | - |<-----+                |              |
	 *         |              |   |      |  +----------+  |              |
	 *         |              | - |<--+  +--| (df+d)/m |<-+              |
	 *         |              +---+   |     +----------+                 |
	 *         |                      |                                  |
	 *         |                      |     +----------+                 |
	 *         |                      +-----| (kf+k)/m |<----------------+
	 *         |                            +----------+
	 *         |
	 *         |
	 *         |
	 *         |  +--------------+                                       xe
	 *         +->| du*kf/(kf+k) |------------------------------------------>
	 *            +--------------+
	 * 
	 */
	
	private static final float m = 1f;
	private static final float df = 0.2f;   // fixed dampening of the 2nd damper
	private static final float kf = 2f;     // fixed stiffness of the 2nd spring
	private static final float du = 1f;     // u1 - u0 = du
	private static final float ku = kf / m; // factor for u
	
	private float x;                        // position of the mass
	private float v;                        // velocity
	private float xe;                       // final position of x (depends on k)
	private float d = 1f;                   // dampening of the 1st damper (customizable)
	private float k = 4.25f;                // stiffness of the 1st spring (customizable)
	private boolean u = false;              // position of bottom board (false -> A, true -> B), x will approach xe / 0 for for u == true / u == false  
	
	
	/**
	 * Initializes a SpringSystem in the respective position.
	 * @param initialState	whether x starts from 0 or xe
	 */
	public SpringSystem(boolean initialState){
		this.u = initialState;
		updateXe();
		
		// setting respective initial conditions
		v = 0;
		x = u ? xe : 0;
	}
	
	private void updateXe(){
		xe = kf / (k + kf) * du;
	}
	
	/**
	 * Calculates one iteration for the ODE of the system.
	 * @param u			u at t-1
	 * @param v			v at t-1
	 * @param x			x at t-1
	 * @return			v at t
	 */
	private float ode(float u, float v, float x){
		// ODE: v' = -v * [(df + d) / m)] - x * [(kf + k) / m] + u * kf/m
		// -> returns acceleration
		// -> no t argument necessary, because f(t) = u * kf/m depends on current status of u
		return -v * (df + d) / m - x * (kf + k) / m + u * ku;
	}
	
	/**
	 * Updates System using Runge-Kutta 4 for given timestep h.
	 * @param h			timestep
	 * @return			x at t
	 */
	public float updateSystem(float h){
		float tu = u ? du  : 0 ;
		
		// explicit-Runge-Kutta-4 coefficients for 2nd-order ode
		float kx1, kx2, kx3, kx4;
		float kv1, kv2, kv3, kv4;
		float h2 = h / 2;
		
		kx1 = v;
		kv1 = ode(tu, v, x);
		
		kx2 = v + kv1 * h2;
		kv2 = ode(tu, v + kv1 * h2, x + kx1 * h2);
	
		kx3 = v + kv2 * h2;
		kv3 = ode(tu, v + kv2 * h2, x + kx2 * h2);
	
		kx4 = v + kv3 * h;
		kv4 = ode(tu, v + kv3 * h, x + kx3 * h);
	
		x = x + h * (kx1 + 2 * kx2 + 2 * kx3 + kx4) / 6;
		v = v + h * (kv1 + 2 * kv2 + 2 * kv3 + kv4) / 6;

		// raw x value (not normalized yet)
		return x;
	}
	
	/**
	 * @return			current velocity v
	 */
	public float getV(){
		return v;
	}
	
	/**
	 * @return			current position x
	 */
	public float getX(){
		return x;
	}
	
	/**
	 * @return			current final position for x (depends on k)
	 */
	public float getXe(){
		return xe;
	}
	
	/**
	 * Set the dampening of the variable damper.
	 * @param d			dampening
	 */
	public void setD(float d){
		this.d = d;
	}
	
	public float getD(){
		return d;
	}
	
	/**
	 * Set the stiffness of the variable spring.
	 * @param k			stiffness
	 */
	public void setK(float k){
		this.k = k;
		
		// important: final position depends on k
		updateXe();
	}
	
	public float getK(){
		return k;
	}
	
	/**
	 * Set the final position for the system. This initiates a motion which results in the system
	 * reaching the final position (0 for u == false or xe for u == true), if it is not already there.
	 * @param u			timestep
	 */
	public void setU(boolean u){
		setU(u, false);
	}
	
	/**
	 * Set the final position for the system. This initiates a motion which results in the system
	 * reaching the final position (0 for u == false or xe for u == true), if it is not already there.
	 * @param u						timestep
	 * @param skipDynamicBehaviour	set the system instantly to its final position (-> no motion)
	 */
	public void setU(boolean u, boolean skipDynamicBehaviour){
		this.u = u;
		
		// means transient oscillations are over -> system instantly is in a steady-state
		if(skipDynamicBehaviour){
			// setting values respectively to avoid transient dynamics
			v = 0;
			x = u ? xe : 0;
		}
	}
	
	public boolean getU(){
		return u;
	}
}
