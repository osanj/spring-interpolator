import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

import de.osanj.springinterpolator.OnSpringUpdateListener;
import de.osanj.springinterpolator.SpringInterpolator;


public class ExampleChart extends JPanel implements OnSpringUpdateListener{
	
	private float[] yVals = null;
	private int yValsLast;
	private int rad = 1;
	private boolean highlightXAxis = true;
	private boolean interpolate = true;
	
	private float from, to;
	
	private BufferedImage image;
	private Color fg = Color.orange;
	private Color bg = Color.lightGray;
	private int fgRGB = fg.getRGB();
	
	public ExampleChart() {
		
	}
	
	public void setYRange(float from, float to){
		this.from = from;
		this.to = to;
	}
	
	public void highlightXAxis(boolean highlight){
		highlightXAxis = highlight;
	}
	
	public void interpolateValuesCrappily(boolean interpolate){
		this.interpolate = interpolate;
	}
	
	public void setup(){
		yVals = new float[getWidth()];
		yValsLast = 0;
		
		image = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
		drawBackground(image.getGraphics());
	}
	
	public void newYValue(float yValue){
		if(yValsLast == yVals.length)
			yValsLast = 0;
		
		yVals[yValsLast] = yValue;
		yValsLast++;
		
		repaint();
	}
	
	@Override
	protected void paintComponent(Graphics g){
		
		if(yVals == null){
			drawBackground(g);
			
		}else{
			// drawing bg
			drawBackground(image.getGraphics());
			
			int y, ybu = 0, dAmount;
			double d;
			
			if(highlightXAxis && from < 0){
				y = getHeight() - (int) (-from / (to - from) * getHeight());
				
				for(int i = 0, len = yVals.length; i < len; i++)
					image.setRGB(i, y, Color.BLACK.getRGB());
			}
			
			for(int i = 0; i < yValsLast; i++){
				y = getHeight() - (int) ((yVals[i] - from) / (to - from) * getHeight());
				
				if(interpolate && i > 0){
					d = Math.sqrt(Math.pow(y - ybu, 2.0) + 1.0); // pythagorean theorem
					dAmount = (int) (d / (2 * rad + 1));
					
					if(dAmount > 0){
						int len = dAmount/2;
						int deltaY = 2 * rad;
						
						for(int j = 0; j < len; j++){
							colorPoint(i - 1, y + deltaY, rad, fgRGB);
							deltaY += 2 * rad;
						}
						
						
						len = dAmount - len;
						
						for(int j = 0; j < len; j++){
							colorPoint(i, y + deltaY, 2, fgRGB);
							deltaY += 2 * rad;
						}
					}
				}
				
				colorPoint(i, y, rad, fgRGB);
				
				ybu = y;
			}
			
			g.drawImage(image, 0, 0, null);
		}
	}
	
	private void colorPoint(int x, int y, int rad, int color){
		for(int i = -rad, lenI = rad; i < lenI; i++){ // ~x
			for(int j = -rad, lenJ = rad; j < lenJ; j++) // ~y
				colorPixel(x + i, y + j, color);
		}
	}
	
	private void colorPixel(int x, int y, int color){
		if(x >= 0 && x < getWidth()){
			if(y >= 0 && y < getHeight()){
				image.setRGB(x, y, color);
			}
		}
	}
	
	private void drawBackground(Graphics g){
		g.setColor(bg);
		g.fillRect(0, 0, getWidth(), getHeight());
	}

	@Override
	public void onSpringUpdate(SpringInterpolator interpolator, float interpolatedValue) {
		newYValue(interpolatedValue);		
	}

	@Override
	public void onSpringFinalPosition(SpringInterpolator interpolator, float finalInterpolatedValue, boolean finalPosition) {
		// TODO Auto-generated method stub
		
	}
}
