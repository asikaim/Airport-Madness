package com.mygdx.game;

import java.util.ArrayList;
import java.util.List;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Sprite;


public class Runway extends Sprite {
	static Texture runway = new Texture("runway.png");
	private List<Airplane> takeoffQueue = new ArrayList<Airplane>();	// queue for planes that are preparing for takeoff
	private int planesLanding = 0;				// number of landing airplanes
	private int planesTakingoff = 0; 			// number of airplanes that are heading out					
	
	// constructor
	public Runway(float x, float y){
		super(runway);
		setX(x);
		setY(y);
	}

	public float getTargetX() {
		return getX() - 100;
	}
	
	public float getTargetY() {
		return getY()+60;
	}
	
	public float getLandingTargetX() {
		return getX()+240;
	}
	
	public float getHangarY() {
		return getY()+120;
	}
	
	public void addPlaneToQueue(Airplane p) {
		p.setTargetRunway(this);
		p.setY(getY()+2);
		p.setRotation(0);
		p.setX(getX()+220 - takeoffQueue.size()*60);
		p.setState(Airplane.State.QUEUING);
		takeoffQueue.add(p);
	}
	
	public int getQueueLength() {
		return takeoffQueue.size();
	}

	public void planeHasTakenOff() {
		planesTakingoff -= 1;
	}
	
	public void planeIsTakingOff(Airplane p) {
		planesTakingoff += 1;
		takeoffQueue.remove(p);
	}
	
	public void planeHasLanded() {
		planesLanding -= 1;
	}
	
	public void planeIsLanding() {
		planesLanding += 1;
	}
	
	public int getPositionInQueue(Airplane p) {
		return takeoffQueue.indexOf(p);
	}
	
	public float getQueueTargetX(Airplane p) {
		return getX()+220 - takeoffQueue.indexOf(p)*60;
	}
	
	public float getQueueTargetY(Airplane p) {
		return getY()+2;
	}

}
