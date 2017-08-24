package com.brackeen.javagamebook.tilegame.sprites;

import com.brackeen.javagamebook.graphics.Animation;
import com.brackeen.javagamebook.graphics.Sprite;

/**
	bullet shot by grub
*/
public class GrubBullet extends Creature {
	 public GrubBullet(Animation left, Animation right,
		        Animation deadLeft, Animation deadRight)
		    {
		        super(left, right, deadLeft, deadRight);
		    }
    
    public float getMaxSpeed() {
        return 0.05f;
    }
}




