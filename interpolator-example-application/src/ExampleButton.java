import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import de.anotherblogger.rebuilt.OnSpringUpdateListener;
import de.anotherblogger.rebuilt.SpringInterpolator;


public class ExampleButton extends JPanel implements MouseListener, OnSpringUpdateListener{
	
	private Color fg1 = Color.orange;
	private Color fg2 = Color.red;
	private Color fg = fg1;
	private Color bg = Color.lightGray;
	private int squareSize, squareSizeMin, squareSizeMax, squareX, squareY;
	private SpringInterpolator interpolator;
	
	
	public ExampleButton(SpringInterpolator interpolator){
		this.interpolator = interpolator;
		
		addMouseListener(this);
	}
	
	public void setSize(int squareSizeMin, int squareSizeMax){
		this.squareSizeMin = squareSizeMin;
		this.squareSizeMax = squareSizeMax;
		this.squareSize = squareSizeMin;
		
		updateCoors();
	}
	
	private void updateCoors(){
		this.squareX = (getWidth() - squareSize) / 2;
		this.squareY = (getHeight() - squareSize) / 2;
		
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		//redrawing bg
		g.setColor(bg);
		g.fillRect(0, 0, getWidth(), getHeight());
		
		//draw
		g.setColor(fg);
		g.fillRect(squareX, squareY, squareSize, squareSize);
	}
	
	
	
	
	@Override
	public void onSpringUpdate(SpringInterpolator interpolator, float interpolatedValue) {
		squareSize = squareSizeMin + (int) (interpolatedValue * (squareSizeMax - squareSizeMin));
		
		updateCoors();
		repaint();
	}

	@Override
	public void onSpringFinalPosition(SpringInterpolator interpolator, float finalInterpolatedValue, boolean finalPosition) {
		System.out.println(">>> onSpringFinalPosition");
	}

	@Override
	public void mouseClicked(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseEntered(MouseEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseExited(MouseEvent arg0) {
		if(interpolator.getFinalPosition())
			interpolator.setFinalPosition(false);
	}

	@Override
	public void mousePressed(MouseEvent arg0) {
		System.out.println("mousePressed");
		
		//fg = fg2;
		interpolator.setFinalPosition(true);
	}
	
	@Override
	public void mouseReleased(MouseEvent arg0) {
		System.out.println("mouseReleased");

		//fg = fg1;
		interpolator.setFinalPosition(false);
	}
	

}
