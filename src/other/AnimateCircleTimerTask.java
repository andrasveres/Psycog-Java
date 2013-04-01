package other;

import java.util.Random;
import java.util.TimerTask;

import common.AnimateCircle;

public class AnimateCircleTimerTask extends TimerTask {

	AnimateCircle cc;
	int dx;
	int dy;

	int time;
	
	public AnimateCircleTimerTask(AnimateCircle c) {
		cc = c;
		dx = 1;
		dy = 1;
		time = 0;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
		int distx = cc.x+cc.size/2-cc.mx;
		int disty = cc.y+cc.size/2-cc.my;
		
		int d = (int) Math.sqrt(distx*distx + disty*disty);

		time ++;
		
		System.out.println("Distance: "+d);
		
		cc.series.add(time, d);

		if(d>cc.size) {
			return;
		}
		// System.out.println("run");
		
		
		
		int x, y;
		x=cc.x+dx;
		y=cc.y+dy;
		
		if(x>=800-cc.size || x<=0) {
			dx = -dx;
		}

		if(y>=600-cc.size || y<=0) {
			dy = -dy;
		}

		cc.moveCircle(x, y);
		cc.repaint();
	}
	

}
