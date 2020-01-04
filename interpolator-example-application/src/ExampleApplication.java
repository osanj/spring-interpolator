import java.awt.EventQueue;

import javax.swing.JFrame;

import de.osanj.springinterpolator.SpringInterpolator;


public class ExampleApplication extends JFrame{

	private ExampleButton demoButton;
	private ExampleChart demoChart;
	private SpringInterpolator interpolator;
	
	public ExampleApplication(){
        initUI();
    }

    private void initUI(){
    	//basic setup
    	setLayout(null);
        setSize(800, 425);
        setTitle(this.getClass().getName());
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
		
		interpolator = new SpringInterpolator();
		//interpolator.setStiffness(5f);
		//interpolator.setDampening(0.5f);
        
		demoChart = new ExampleChart();
		demoChart.setBounds(400, 0, 400, 400);
		demoChart.setYRange(-1f, 2f);
		demoChart.setup();
		add(demoChart);
		
        demoButton = new ExampleButton(interpolator);
        demoButton.setBounds(0, 0, 400, 400);
        demoButton.setSize(200, 300);
        add(demoButton);
        
		
		interpolator.addListener(demoChart);
		interpolator.addListener(demoButton);
    }   
    
    public static void main(String[] args) {
        
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
            	ExampleApplication ex = new ExampleApplication();
                ex.setVisible(true);
            }
        });
    }
}
