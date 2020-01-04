

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.text.DecimalFormat;

import de.osanj.springinterpolator.SpringInterpolator;
import de.osanj.springinterpolator.SpringSystem;

public class Test {
	
	/*
	public static void main(String[] args) {
		SpringInterpolatorTest();
	}
	*/
	
	private static void SpringInterpolatorTest(){
		//testing mapping
		
		SpringInterpolator ip = new SpringInterpolator(60);
		
		long h = 16;
		long hMax = 3024;
		int steps = (int) (hMax / h);
		
		//getting values
		float[] iHistory = new float[steps];
		
		ip.setFinalPosition(true);

		System.out.println("start: " + System.currentTimeMillis());
		/*
		for(int i = 0; i < steps; i++){
			ip.onUpdate(h);
			
			//iHistory[i] = ip.isWithinTolerance() ? 1 : 0;			
			iHistory[i] = ip.getCurrentInterpolatedValue();
			
			if(i == 80)
				ip.setFinalPosition(false);
			
			if(i == 170)
				ip.setFinalPosition(true, true);
		}
		*/
		System.out.println("end: " + System.currentTimeMillis());
		
		
		DecimalFormat formatter = new DecimalFormat("0.0000");
		String clipboardOutput = "";
		
		for(int i = 0; i < steps; i++){
			clipboardOutput += formatter.format(iHistory[i]) + "\n";			
		}
		
		
		StringSelection stringSelection = new StringSelection(clipboardOutput);
		Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
		

		System.out.println("clipboard: " + System.currentTimeMillis());
		
	}
	
	private static void SpringSystemTest(){
		SpringSystem sys = new SpringSystem(false);
		
		float h = 0.1f;
		float hMax = 10f;
		int steps = (int) (hMax / h);
		
		float[] xHistory = new float[steps];

		System.out.println("xe: " + sys.getXe());
		System.out.println("start: " + System.currentTimeMillis());
		
		//getting values
		for(int i = 0; i < steps; i++){
			xHistory[i] = sys.updateSystem(h);
			
			if(i == 10)
				sys.setU(true); //setting active after 1 second
		}
		
		System.out.println("end: " + System.currentTimeMillis());
		
		
		
		DecimalFormat formatter = new DecimalFormat("0.0000");
		String clipboardOutput = "";
		
		for(int i = 0; i < steps; i++){
			clipboardOutput += formatter.format(xHistory[i]) + "\n";			
		}
		
		
		StringSelection stringSelection = new StringSelection(clipboardOutput);
		Clipboard clpbrd = Toolkit.getDefaultToolkit ().getSystemClipboard();
		clpbrd.setContents(stringSelection, null);
		

		System.out.println("clipboard: " + System.currentTimeMillis());
		
	}

}