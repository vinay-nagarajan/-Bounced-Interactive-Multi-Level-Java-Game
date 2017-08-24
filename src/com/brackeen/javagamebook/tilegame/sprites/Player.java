package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.GameManager;

/**
    The Player.
*/
public class Player extends Creature {

    private static final float JUMP_SPEED = -.95f;

    private final int maxhp = 40;
    private boolean onGround;
    public int hp = 20;
    private boolean invincible = false;
    private boolean gazzed = false;
    public Player(Animation left, Animation right,
        Animation deadLeft, Animation deadRight)
    {
        super(left, right, deadLeft, deadRight);
    }

   
    public void collideHorizontal() {
        setVelocityX(0);
    }


    public void collideVertical() {
        // check if collided with ground
        if (getVelocityY() > 0) {
            onGround = true;
        }
        setVelocityY(0);
    }


    public void setY(float y) {
        // check if falling
        if (Math.round(y) > Math.round(getY())) {
            onGround = false;
        }
        super.setY(y);
    }
    
    public void wakeUp() {
        // do nothing
    }


    /**
        Makes the player jump if the player is on the ground or
        if forceJump is true.
    */
    public void jump(boolean forceJump) {
        if (onGround || forceJump) {
            onGround = false;
            setVelocityY(JUMP_SPEED);
        }
    }
    
    public void down(boolean forceJump) {
        if (onGround || forceJump) {
            onGround = false;
            setVelocityY(-2*(JUMP_SPEED));
        }
    }

    public float getMaxSpeed() {
        return 0.5f;
    }
    



//getter function for player health
 public int getHp(){
        //dead, return 0
        if(!isAlive()){return 0;}
        //Moving or Still?
        int Xvelocity = Math.round(Math.abs(getVelocityX()));
        //Still
        if(Xvelocity == 0 && onGround == true){
            if(GameCore.time2 >= 1500){
                hp = hp + 5;
                GameCore.time2 = 0;
            }
        }
        //Moving
        else if(Xvelocity != 0 || onGround == false){
            if(GameCore.time2 >= 1500){
                hp+=1;
                GameCore.time2 = 0;
            }
        }
        //Shooting
        //never exceed 40
        if(hp > maxhp){hp = maxhp;}
        if(hp < 0){hp = 0;}
        return hp;
    }
 
 //getter function for invincible
 public boolean getInvincible() {
	 return this.invincible;
 }
 
 public boolean getGassed() {
	 return this.gazzed;
 }
 
  public void setInvincible(boolean inv) {
	 this.invincible = inv;
 }
 
 public void setGassed(boolean var) {
	 this.gazzed = var;
 }
 
}