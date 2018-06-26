package com.mygdx.game;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;

public class Airplane extends Sprite {
	/*
	 * States for airplane:
	 * APPROACHING = airplane is heading for runway
	 * LANDING = airplane is landing on runway, can't be selected
	 * WAITING = airplane is waiting on air, does a 360 spin
	 * QUEUING = airplane is on a queue for takeoff
	 * TAKINGOFF = airplane is turning towards runway for departure
	 * LEAVING = airplane is departing the runway
	 * DONE = airplane has finished actions and left
	 * PARKING = airplane is parking on a hangar
	 */
	public enum State{
		APPROACHING, LANDING, WAITING, QUEUING, TAKINGOFF, LEAVING, DONE, PARKING
	};
	
	private State airplaneState;			// state for airplane
	private Runway targetRunway;			// runway airplane has selected as target
	
	private Vector2 direction;				//	vector for target direction
	private Vector2 velocity;				// vector for target velocity
	
	static final float speed = 32; 			// speed for airplane
	static float rotationSpeed = 12;		// speed for airplane rotation
	static float maxRotationSpeed = 8;		// max speed for airplane rotation
	
	private boolean selected = false;		// airplane select
	private Vector2 circlingTarget;			// target coordinate for circling
	private float waitingTime;				// time how long airplane waits
	private float rotationDirection = 1;	// direction for rotation
	private boolean isCircling = false;		// boolean for checking if airplane is circling around
	
	static Texture plane = new Texture("plane.png");
	static Texture planeSelected = new Texture("planeselected.png");
	
	//constructor
	public Airplane(Runway rw, float x, float y, float rotation) {
		super(plane);
		this.targetRunway = rw;
		airplaneState = State.APPROACHING;

		direction = new Vector2();
		velocity = new Vector2();
		setRotation(rotation);
		setX(x);
		setY(y);
	}
	
	public void setState(State s) {
		if(s != airplaneState) {
			if(airplaneState == State.QUEUING && (s != State.TAKINGOFF || (targetRunway.getPositionInQueue(this) != 0))) {
				return;
			}
			else if (s == State.TAKINGOFF && airplaneState != State.QUEUING) {
				return;
			}
			airplaneState = s;
			if(airplaneState == State.WAITING) {
				startWaiting();
			}
			if(airplaneState == State.TAKINGOFF) {
				this.targetRunway.planeIsTakingOff(this);
			}
		}
		assert airplaneState != null : airplaneState;
		
	}
	// update airplane states
	public void update(float dt) {
		if (getRotation() > 360) {
			setRotation(getRotation() - 360);
		}
		switch(airplaneState) {
			case APPROACHING:		// airplane approaching runway
				assert airplaneState == State.APPROACHING : airplaneState;
				turnTowards(targetRunway.getTargetX(), targetRunway.getTargetY(),dt);
				moveSprite(dt);
				assert dt > 0 : dt;
				// if airplane is close to runway, start landing
				if (distanceTo(targetRunway.getTargetX(), targetRunway.getTargetY())< 40) {
					airplaneState = State.LANDING;
					assert airplaneState == State.LANDING : airplaneState;
					targetRunway.planeIsLanding();
				}
				break;
			case LANDING:		// airplane has started landing
				assert airplaneState == State.LANDING : airplaneState;
				// turn towards target runway for landing
				turnTowards(targetRunway.getLandingTargetX(), targetRunway.getTargetY(),dt);
				moveSprite(dt);
				assert dt > 0 : dt;
				// if airplane is close to end of runway, start parking
				if (distanceTo(targetRunway.getLandingTargetX(), targetRunway.getTargetY()) < 40) {
					airplaneState = State.PARKING;
					assert airplaneState == State.PARKING : airplaneState;
				}
				break;
			case WAITING:		// airplane is waiting for landing
				assert airplaneState == State.WAITING : airplaneState;
				waitingTime += dt;
				assert dt > 0 : dt;
				if (!isCircling) {
					// go to a "safe area" to avoid going offscreen when circling
					turnTowards(circlingTarget.x, circlingTarget.y,dt);
					if (distanceTo(circlingTarget.x, circlingTarget.y) < 30) {
						isCircling = true;
					}
				} else {
					setRotation(getRotation()+rotationSpeed*dt*rotationDirection);
				}
				
				moveSprite(dt);
				// if airplane has finished circling, switch back to approaching
				if (waitingTime > 360 / rotationSpeed) {
					airplaneState = State.APPROACHING;
					assert airplaneState == State.APPROACHING: airplaneState;
				}
				break;
			case QUEUING:		// airplane is queuing
				assert airplaneState == State.QUEUING : airplaneState;				
				// Move towards correct spot in queue
				if (distanceTo(targetRunway.getQueueTargetX(this), targetRunway.getQueueTargetY(this)) > 15) {
					moveSprite(dt/5);
				}
				break;
			case TAKINGOFF:		// airplane is departing
				assert airplaneState == State.TAKINGOFF : airplaneState;
				// turn towards beginning of target runway
				turnTowards(targetRunway.getTargetX(), targetRunway.getTargetY(),dt);
				moveSprite(dt);
				// if distance is close enough to end of runway, start leaving from screen
				if ( distanceTo(targetRunway.getTargetX(), targetRunway.getTargetY())< 100) {
					airplaneState = State.LEAVING;
					assert airplaneState == State.LEAVING : airplaneState;
					targetRunway.planeHasTakenOff();
				}
				break;
			case PARKING:		// airplane is parking
				assert airplaneState == State.PARKING : airplaneState;
				// turn towards hangar
				turnTowards(targetRunway.getLandingTargetX(), targetRunway.getHangarY(), dt*2);
				moveSprite(dt/3);
				// if distance is close to hangar
				if (distanceTo(targetRunway.getLandingTargetX(), targetRunway.getHangarY()) < 10) {
					airplaneState = State.DONE;
					assert airplaneState == State.DONE : airplaneState;
					targetRunway.planeHasLanded();
				}
				break;
			case LEAVING:		// airplane is heading out of screen
				assert airplaneState == State.LEAVING : airplaneState;
				moveSprite(dt);
				assert dt > 0 : dt;
				if (getX() < -100) {
					airplaneState = State.DONE;
					assert airplaneState == State.DONE : airplaneState;
				}
				break;
			case DONE:		// airplane has finished actions and left screen
				assert airplaneState == State.DONE : airplaneState;
				break;
		}
		
	}
	
	public void draw(Batch b) {
		if (airplaneState != State.DONE) {
			super.draw(b);
		}
	}
	// move sprite
	private void moveSprite(float dt) {
		direction.x = MathUtils.cosDeg(getRotation());
		direction.y = MathUtils.sinDeg(getRotation());

		velocity.x = direction.x * speed;
		velocity.y = direction.y * speed;
		
		setX(getX()+velocity.x * dt);
		setY(getY()+velocity.y * dt);
	}
	// turn towards target
	private void turnTowards(float x, float y, float dt) {
		float targetAngle = (getRotation() - MathUtils.radiansToDegrees * MathUtils.atan2(y - this.getY(), x - this.getX()));
		float direction = -1;
		if (targetAngle > 360) {
			targetAngle = targetAngle % 360;
		}
		if (targetAngle < 0) {
			targetAngle += 360;
		}
		if (targetAngle > 180) {
			targetAngle -= 180;
			direction = 1;
		}
		rotate(Math.min(rotationSpeed, targetAngle)*direction*maxRotationSpeed*dt);
		
	}

	public void startWaiting() {
		waitingTime = 0;
		isCircling = false;
		circlingTarget = getClosestPointOnWaitingCircle();
		airplaneState = State.WAITING;
		assert airplaneState == State.WAITING : airplaneState;
	}
	// distance to x, y coordinates
	public float distanceTo(float x, float y) {
		return (float) Math.sqrt(Math.pow(y-getY(),2)+Math.pow(x-getX(),2));
	}

	public Runway getTargetRunway() {
		return this.targetRunway;
	}
	
	public void setTargetRunway(Runway r) {
		this.targetRunway = r;
		assert this.targetRunway == r : targetRunway;
	}
	
	private Vector2 getClosestPointOnWaitingCircle() {
		Vector2 target = new Vector2();
		float centerX = 320;
		float centerY = 240;
		if (distanceTo(centerX,centerY) < 200) {
			target.x = getX();
			target.y = getY();
		} else {
			float r = 130;
			target.x = (float) (centerX + r * (getX()-centerX) / (Math.sqrt(Math.pow(getX()-centerX, 2)+Math.sqrt(Math.pow(getY()-centerY, 2)))));
			target.y = (float) (centerY + r * (getY()-centerY) / (Math.sqrt(Math.pow(getX()-centerX, 2)+Math.sqrt(Math.pow(getY()-centerY, 2)))));	
		}
		assert target != null : target;
		return target;
	}
	// unselect airplane
	public void unselect() {
		this.selected = false;
		setTexture(plane);
		assert this.selected == false : selected;
	}
	// select airplane
	public boolean select() {
		if (this.airplaneState == State.APPROACHING || this.airplaneState == State.WAITING || this.airplaneState == State.QUEUING) {
			this.selected = true;
			setTexture(planeSelected);
			return true;
		}
		return false;
	}
	
	public State getState() {
		return airplaneState;
	}
	// checks if airplanes collide
	public boolean collidesWith(Airplane p) {
		if(!p.equals(this) && this.targetRunway == p.targetRunway && (p.canCollide() && this.canCollide()) && (distanceTo(p.getX(), p.getY()) < 30)) {
			return true;
		} else {
			return false;
		}
	}
	// checks if airplanes can collide
	// (assignment specified that airplanes collide on runway)
	// (in original game airplanes collide whenever they touch each other)
	public boolean canCollide() {
		if(this.airplaneState == State.LANDING || this.airplaneState == State.TAKINGOFF) {
			return true;
		} else {
			return false;
		}
	}
}
