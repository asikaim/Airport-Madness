package com.mygdx.game;

import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;

//640x480 size of the screen
public class AirportMadness extends ApplicationAdapter {
    SpriteBatch batch;
    
    List<Airplane> planes = new ArrayList<Airplane>();	// list of airplanes
    
    Runway rw1;			// runway 1 object
    Runway rw2;			// runway 2 object
    Airplane plane;		// airplane object
    
    
    private float elapsed;					// time elapsed
    private float dt;						// delta t
    private Airplane selectedPlane = null;	// selected airplane
  //  private boolean gameOver = false;		// boolean for game over - unnecessary at this moment
    
    static int SCORE_PER_PLANE = 1; // how many scores you get from one airplane landing/departing
    private int score = 0;			// how many scores you got
    
    // creates new sprite batch and runways
    @Override
    public void create () {
        batch = new SpriteBatch();
        
        rw1 = new Runway(222,10);
        rw2 = new Runway(300, 200);
    }
    
    
    // main loop for game
    @Override
    public void render () {

        dt = Gdx.graphics.getDeltaTime();
        Gdx.gl.glClearColor(0, 1, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        
        assert dt > 0 : dt;
        // loops through airplanes and checks if airplane is selected
        for(Airplane p : planes) {
            p.update(dt);
            if(Gdx.input.isButtonPressed(Input.Buttons.LEFT)) {
            	if(p.distanceTo(Gdx.input.getX(), 480 - Gdx.input.getY()) < 35 ) {
                    if (selectedPlane != null) {
                    	selectedPlane.unselect();
                    }
                    if (p.select()) {
                    	selectedPlane = p;
                    	assert selectedPlane != null : selectedPlane;
                    }
                }
            }
        }
        /*
         * inputs
         * 1 = airplane starts heading for runway 1
         * 2 = airplane starts heading for runway 2
         * 3 = airplane starts waiting (circles around)
         * 4 = airplane starts departing from runway (only available if airplane is on a queue for take off)
         */
        if (selectedPlane != null) {
        	if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) {
        		selectedPlane.setState(Airplane.State.APPROACHING);
        		selectedPlane.setTargetRunway(rw1);
        		assert selectedPlane.getTargetRunway() == rw1 : rw1;
        	}
        	if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) {
        		selectedPlane.setState(Airplane.State.APPROACHING);
        		selectedPlane.setTargetRunway(rw2);
        		assert selectedPlane.getTargetRunway() == rw2 : rw2;
        	}
        	if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) {
        		selectedPlane.setState(Airplane.State.WAITING);
        	}
        	if(Gdx.input.isKeyJustPressed(Input.Keys.NUM_4)) {
        		selectedPlane.setState(Airplane.State.TAKINGOFF);
        	}
        }
        
        
        // spawns airplanes
    	if (MathUtils.random() > .9985 && rw1.getQueueLength() < 2) {
    		plane = new Airplane(rw1,0,0,0);
    		planes.add(plane);
    		rw1.addPlaneToQueue(plane);
    		assert plane.getTargetRunway() == rw1 : rw1;
    	}
    	if (MathUtils.random() > .9985 && rw2.getQueueLength() < 2) {
    		plane = new Airplane(rw2,0,0,0);
    		planes.add(plane);
    		rw2.addPlaneToQueue(plane);
    		assert selectedPlane.getTargetRunway() == rw2 : rw2;
    	}
    	
    	if (MathUtils.random() > .9985) {
    		plane = new Airplane(rw1,MathUtils.random(800)-100,MathUtils.random(100)+640,MathUtils.random(180)+180);
    		planes.add(plane);
    	}
    	
    	if (MathUtils.random() > .9985) {
    		plane = new Airplane(rw2,MathUtils.random(800)-100,0-MathUtils.random(100),MathUtils.random(180));
    		planes.add(plane);
    	}
    	
    	// iterates through airplanes and checks if airplane has left
    	// adds score
    	Iterator<Airplane> i = planes.iterator();
    	while (i.hasNext()) {
    	   Airplane p = i.next(); 
    	   if (p.getState() == Airplane.State.DONE) {
    		   score += SCORE_PER_PLANE;
    		   System.out.println("Your score is: " + score);
    		   if (selectedPlane == p) {
    			   selectedPlane = null;
    		   }
        	   i.remove();
    	   }
    	   // minus score if plane has been waiting for 15 seconds
    	   if(p.getState() == Airplane.State.QUEUING) {
    		   elapsed += dt;
    		   if (elapsed > 15.0) {
    			   score -= SCORE_PER_PLANE;
    			   System.out.println("Your score is: " + score);
    			   elapsed = 0;
    		   }
    	   }
    	   // checks for collision, gameover if collision has happened
    	   for(Airplane c : planes) {
    		   if(p.collidesWith(c)) {
    			  // gameOver = true;
    			   System.out.println("Game over. Your score was " + score);
    			   System.exit(0);
    		   }
    	   }
    	}
    	// draws sprite batch
        batch.begin();
        rw1.draw(batch);
        rw2.draw(batch);
        for(Airplane p : planes) {
            p.draw(batch);
        }
        batch.end();
        
    }
    
    //disposes sprite batch
    @Override
    public void dispose () {
        batch.dispose();
    }
}