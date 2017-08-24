package com.brackeen.javagamebook.tilegame;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.Iterator;
import java.util.Scanner;

import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.sampled.AudioFormat;

import com.brackeen.javagamebook.graphics.*;
import com.brackeen.javagamebook.sound.*;
import com.brackeen.javagamebook.input.*;
import com.brackeen.javagamebook.test.GameCore;
import com.brackeen.javagamebook.tilegame.sprites.*;

/**
    GameManager manages all parts of the game.
*/
public class GameManager extends GameCore {
	static String filename;
	static boolean input_act = false;
    public static void main(String[] args) {
    	Scanner input = new Scanner(System.in);    	
    	System.out.println("Enter the name of the map you would like to play.");
    	while(true){
    	System.out.println("A. Play the default map");
    	System.out.println("B.Choose your own map");
    	String option = input.nextLine();
    	if(option.equals("A")){
    		input_act = false;
    		break;
    	}else if(option.equals("B")){
    		input_act = true;
    		break;
    	}else{
    		System.out.println("Please enter a correct option.");
    	}
    	}
       new GameManager().run();   
    }

    // uncompressed, 44100Hz, 16-bit, mono, signed, little-endian
    private static final AudioFormat PLAYBACK_FORMAT =
        new AudioFormat(44100, 16, 1, true, false);

    private static final int DRUM_TRACK = 1;

    public static final float GRAVITY = 0.002f;


    private Point pointCache = new Point();
    private TileMap map;
    private MidiPlayer midiPlayer;
    private SoundManager soundManager;
    private ResourceManager resourceManager;
    private Sound prizeSound;
    private Sound boopSound;
    private InputManager inputManager;
    private TileMapRenderer renderer;


    private GameAction moveLeft;
    private GameAction moveRight;
    private GameAction down;
    private GameAction jump;
    private GameAction exit;
    private GameAction shoot;

    //shooting initializations
    private long ct = System.currentTimeMillis();

    private int Num = 0;
    private boolean shoot1 = false;
    
    //powerUp initializations
    private long invincibleTime = System.currentTimeMillis();
    private long gassedTime = System.currentTimeMillis();

 
    private Sound shootingSound;
    private Sound deadSound;
    private Sound mushroomSound;
    private Sound gasSound;
    private Sound explodeSound;
    
    public void init() {
        super.init();

        // set up input manager
        initInput();

        // start resource manager
        resourceManager = new ResourceManager(
        screen.getFullScreenWindow().getGraphicsConfiguration());

        // load resources
        renderer = new TileMapRenderer();
        renderer.setBackground(
            resourceManager.loadImage("background.png"));

        // load first map
        if (input_act) {
			map = resourceManager.inputMap();
		} else {
			map = resourceManager.loadNextMap();
		}

        // load sounds
        soundManager = new SoundManager(PLAYBACK_FORMAT);
        prizeSound = soundManager.getSound("sounds/prize.wav");
        boopSound = soundManager.getSound("sounds/boop2.wav");
        
        
        
  
        
        shootingSound = soundManager.getSound("sounds/shoot.wav");
        deadSound = soundManager.getSound("sounds/Die.wav");
        mushroomSound = soundManager.getSound("sounds/mushroom.wav");
        gasSound = soundManager.getSound("sounds/gas.wav");
        explodeSound = soundManager.getSound("sounds/explode.wav");
        // start music
        midiPlayer = new MidiPlayer();
        Sequence sequence =
            midiPlayer.getSequence("sounds/music.mid");
        midiPlayer.play(sequence, true);
        toggleDrumPlayback();
    }


    /**
        Closes any resources used by the GameManager.
    */
    public void stop() {
        super.stop();
        midiPlayer.close();
        soundManager.close();
    }


    private void initInput() {
        moveLeft = new GameAction("moveLeft");
        moveRight = new GameAction("moveRight");
        jump = new GameAction("jump",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        exit = new GameAction("exit",
            GameAction.DETECT_INITAL_PRESS_ONLY);
        shoot = new GameAction("shoot");
        down = new GameAction("down",GameAction.DETECT_INITAL_PRESS_ONLY);
        inputManager = new InputManager(
            screen.getFullScreenWindow());
        inputManager.setCursor(InputManager.INVISIBLE_CURSOR);

        inputManager.mapToKey(moveLeft, KeyEvent.VK_LEFT);
        inputManager.mapToKey(moveRight, KeyEvent.VK_RIGHT);
        inputManager.mapToKey(down, KeyEvent.VK_DOWN);
        inputManager.mapToKey(jump, KeyEvent.VK_UP);
        inputManager.mapToKey(jump, KeyEvent.VK_SPACE);
        inputManager.mapToKey(exit, KeyEvent.VK_ESCAPE);
        inputManager.mapToKey(shoot, KeyEvent.VK_S);
    }

    private long getMilliseconds(){
    	return System.currentTimeMillis();
    }
    
    
    private long invincibleEnd = 0;
    private long gassedEnd = 0;
    private void checkInput(long elapsedTime) {
        if (exit.isPressed()) {
            stop();
        }
        Player player = (Player)map.getPlayer();
        if (player.isAlive()) {
            float velocityX = 0;
           // float velocityY = 0;
            if (moveLeft.isPressed()) {
                velocityX-=player.getMaxSpeed();
            }
            if (moveRight.isPressed()) {
                velocityX+=player.getMaxSpeed();
            }
            if (jump.isPressed()) {
            		player.jump(false);
            
            }
            if (down.isPressed()) {
                player.down(true);
            }
            if (shoot.isPressed()){
            	//the Bullet is made here
            	if(!player.getGassed() || player.getInvincible()) {
            	if(Num == 0){
            		
            			if(getMilliseconds() - ct >= 1000){
            				Num = Num+1;
            				
            				shoot1 = true;
            				
            				ct = getMilliseconds();
            			}
            
            	}
            	
            //	else if(Num == 1){
            //		if(System.currentTimeMillis() - ct >= 1000){
            //			shoot1 = true;
            	//		Num++;
            	//		ct = System.currentTimeMillis();
            		//}
            	//}
            
            else if(Num <= 10){
            		if(getMilliseconds() - ct >= 400){
            			shoot1 = true;
            			Num++;
            			ct = System.currentTimeMillis();
            		}
            	}else{

            		if(System.currentTimeMillis() - ct >= 1000){
            			shoot1 = true;
            			Num = 2;
            			ct = System.currentTimeMillis();
            		}
            	}
            }
            	else{
            	Num = 0;
            }
            }
            
            player.setVelocityX(velocityX);   
        }
        

    }

    public void draw(Graphics2D g) {
        renderer.draw(g, map,
            screen.getWidth(), screen.getHeight());
        // draw health
        Player player = (Player)map.getPlayer();
        g.setColor(Color.red);
        g.setFont(new Font("Times New Roman",0,50));
        g.drawString("Health:" + player.getHp() ,50,50);
        
        if(player.getInvincible()){
        	g.setColor(Color.red);
            g.setFont(new Font("Arial",0,20));
            g.drawString("Now I'm invincible!", 500, 100);
        }
        if(player.getGassed() && !player.getInvincible()){
        	g.setColor(Color.blue);
            g.setFont(new Font("Times New Roman",0,50));
            g.drawString("Gassed!", 500, 150);
            shoot1 = false;
        }

    }


    /**
        Gets the current map.
    */
    public TileMap getMap() {
        return map;
    }


    /**
        Turns on/off drum playback in the midi music (track 1).
    */
    public void toggleDrumPlayback() {
        Sequencer sequencer = midiPlayer.getSequencer();
        if (sequencer != null) {
            sequencer.setTrackMute(DRUM_TRACK,
                !sequencer.getTrackMute(DRUM_TRACK));
        }
    }


    /**
        Gets the tile that a Sprites collides with. Only the
        Sprite's X or Y should be changed, not both. Returns null
        if no collision is detected.
    */
    public Point getTileCollision(Sprite sprite,
        float newX, float newY)
    {
        float fromX = Math.min(sprite.getX(), newX);
        float fromY = Math.min(sprite.getY(), newY);
        float toX = Math.max(sprite.getX(), newX);
        float toY = Math.max(sprite.getY(), newY);

        // get the tile locations
        int fromTileX = TileMapRenderer.pixelsToTiles(fromX);
        int fromTileY = TileMapRenderer.pixelsToTiles(fromY);
        int toTileX = TileMapRenderer.pixelsToTiles(
            toX + sprite.getWidth() - 1);
        int toTileY = TileMapRenderer.pixelsToTiles(
            toY + sprite.getHeight() - 1);

        // check each tile for a collision
        for (int x=fromTileX; x<=toTileX; x++) {
            for (int y=fromTileY; y<=toTileY; y++) {
                if (x < 0 || x >= map.getWidth() ||
                    map.getTile(x, y) != null)
                {
                    // collision found, return the tile
                    pointCache.setLocation(x, y);
                    return pointCache;
                }
            }
        }

        // no collision found
        return null;
    }


    /**
        Checks if two Sprites collide with one another. Returns
        false if the two Sprites are the same. Returns false if
        one of the Sprites is a Creature that is not alive.
    */
    public boolean isCollision(Sprite s1, Sprite s2) {
        // if the Sprites are the same, return false
        if (s1 == s2) {
            return false;
        }

        // if one of the Sprites is a dead Creature, return false
        if (s1 instanceof Creature && !((Creature)s1).isAlive()) {
            return false;
        }
        if (s2 instanceof Creature && !((Creature)s2).isAlive()) {
            return false;
        }

        // get the pixel location of the Sprites
        int s1x = Math.round(s1.getX());
        int s1y = Math.round(s1.getY());
        int s2x = Math.round(s2.getX());
        int s2y = Math.round(s2.getY());

        // check if the two sprites' boundaries intersect
        
        return (s1x < s2x + s2.getWidth() &&
            s2x < s1x + s1.getWidth() &&
            s1y < s2y + s2.getHeight() &&
            s2y < s1y + s1.getHeight());
    }


    /**
        Gets the Sprite that collides with the specified Sprite,
        or null if no Sprite collides with the specified Sprite.
    */
    public Sprite getSpriteCollision(Sprite sprite) {

        // run through the list of Sprites
        Iterator i = map.getSprites();
        while (i.hasNext()) {
            Sprite otherSprite = (Sprite)i.next();
            if (isCollision(sprite, otherSprite)) {
                // collision found, return the Sprite
                return otherSprite;
            }
        }

        // no collision found
        return null;
    }
    /**
        Updates Animation, position, and velocity of all Sprites
        in the current map.
    */
    public void update(long elapsedTime) {
        Creature player = (Creature)map.getPlayer();
        Sprite player_bullet = (Sprite) resourceManager.getBullet().clone();
        Player realPlayer = (Player)player;
        Sprite grubEnemy;
        // player is dead! start map over
        if (player.getState() == Creature.STATE_DEAD) {
            map = resourceManager.reloadMap();
            return;
        }

        // get keyboard/mouse input
        checkInput(elapsedTime);

        // update player
        updateCreature(player, elapsedTime);
        player.update(elapsedTime);
        
        // update bullet
        if(shoot1 && !realPlayer.getGassed()){
        	shoot1 = false;
        	player_bullet.setY(player.getY());
        	if(player.face_right){
        		player_bullet.setVelocityX(1.0f);
        		player_bullet.setX(player.getX());
        	}else{
        		player_bullet.setVelocityX(-1.0f);
        		player_bullet.setX(player.getX());
        	}
        	player_bullet.setVelocityY(0);
        	map.addSprite(player_bullet);
        	soundManager.play(shootingSound);
        }
        if(realPlayer.getInvincible()) {
        	this.invincibleEnd = System.currentTimeMillis();
        	if(this.invincibleEnd - this.invincibleTime > 10000) {
        		this.invincibleTime = System.currentTimeMillis();
        		realPlayer.setInvincible(false);
        	}
        }
        else if(realPlayer.getGassed()){
        	this.gassedEnd = System.currentTimeMillis();
        	if(this.gassedEnd - this.gassedTime > 10000){
        		this.gassedTime = System.currentTimeMillis();
        		realPlayer.setGassed(false);
        	}
        }
        // update other sprites
        Iterator i = map.getSprites();;
        while (i.hasNext()) {
            Sprite sprite = (Sprite)i.next();
            if (sprite instanceof Creature) {
                Creature creature = (Creature)sprite;
                if (creature.getState() == Creature.STATE_DEAD) {
                    i.remove();
                }
                else {
                	grubEnemy = updateCreature(creature, elapsedTime);
                	if(grubEnemy != null){
                		map.addGrubBullet(grubEnemy);
                	}
                }
            }
            // normal update
            sprite.update(elapsedTime);
        }
        map.enable_bullet();
    }

    /**
        Updates the creature, applying gravity for creatures that
        aren't flying, and checks collisions.
    */ 
    @SuppressWarnings("unused")
	private GrubBullet Enemystuff(Creature creature, GrubBullet grubEnemy){
    	grubEnemy.setX(creature.getX() + 10);
			grubEnemy.setY(creature.getY() + 10);
 		grubEnemy.setVelocityX(0.7f);
		return grubEnemy;
    	
    	
    }
    
    private GrubBullet updateCreature(Creature creature,
        long elapsedTime)
    {

        // apply gravity
        if (!creature.isFlying()) {
        	if(creature instanceof Bullet 
        			|| creature instanceof GrubBullet){
        		//do nothing
        	}else{
        		creature.setVelocityY(creature.getVelocityY() +
        				GRAVITY * elapsedTime);
        	}
        }

        // change x
        float dx = creature.getVelocityX();
        float oldX = creature.getX();
        float newX = oldX + dx * elapsedTime;
        Point tileCollision = getTileCollision(creature, newX, creature.getY());
        Sprite collisionSprite;
        if(creature instanceof Bullet){
        	collisionSprite = getSpriteCollision(creature);
        	if(collisionSprite instanceof PowerUp.Gas || collisionSprite instanceof PowerUp.Explode) {
        		creature.setState(Creature.STATE_DEAD);
        	}
        	if(creature.TravelPlayer(Math.abs(dx)) < creature.range){
        		creature.setX(newX);
        	}else{
        		creature.setState(Creature.STATE_DEAD);
        		creature.travel_length = 0;
        	}
        	if(tileCollision != null){
        		creature.setState(Creature.STATE_DEAD);
        	}
        	checkBeingShot((Bullet)creature, (Player)map.getPlayer());
        	return null;//Bullet will not bounce back if hit the edge of the map
        }
        if(creature instanceof GrubBullet){
        	collisionSprite = getSpriteCollision(creature);
        	if(collisionSprite instanceof PowerUp.Gas || collisionSprite instanceof PowerUp.Explode) {
        		creature.setState(Creature.STATE_DEAD);
        	}
        	if(creature.TravelBug(Math.abs(dx)) < creature.bug_range){
        		creature.setX(newX);
        	}else{
        		creature.setState(Creature.STATE_DEAD);
        		creature.travel_length = 0;
        	}
        	if(tileCollision != null){
        		creature.setState(Creature.STATE_DEAD);
        	}
        	return null;//No bouncing
        }
        Point tile =
            getTileCollision(creature, newX, creature.getY());
        if (tile == null) {
            creature.setX(newX);
        }
        else {
            // line up with the tile boundary
            if (dx > 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x) -
                    creature.getWidth());
            }
            else if (dx < 0) {
                creature.setX(
                    TileMapRenderer.tilesToPixels(tile.x + 1));
            }
            creature.collideHorizontal();
        }
        if (creature instanceof Player) {
            checkPlayerCollision((Player)creature, false);
        }
        
        // change y
        float dy = creature.getVelocityY();
        float oldY = creature.getY();
        float newY = oldY + dy * elapsedTime;
        tile = getTileCollision(creature, creature.getX(), newY);
        if (tile == null) {
            creature.setY(newY);
        }
        else {
            // line up with the tile boundary
            if (dy > 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y) -
                    creature.getHeight());
            }
            else if (dy < 0) {
                creature.setY(
                    TileMapRenderer.tilesToPixels(tile.y + 1));
            }
            creature.collideVertical();
        }
        if (creature instanceof Player) {
            boolean canKill = (oldY < creature.getY());
            checkPlayerCollision((Player)creature, canKill);
        }
        
        if(creature instanceof Grub){
        	collisionSprite = getSpriteCollision(creature);
        	if(collisionSprite instanceof PowerUp.Gas || collisionSprite instanceof PowerUp.Explode) {
        		creature.collideHorizontal();
        	}
        	if(
        			System.currentTimeMillis() - creature.bug_count > 800)
        	if(creature.getVelocityX() != 0f){
        		if(creature.grub_count > 0)
        				 {
        			
        			GrubBullet grubEnemy = 
        				(GrubBullet) resourceManager.getGrubBullet().clone();
        			if(!creature.face_left){
         			grubEnemy.setX(creature.getX() + 10);
          			grubEnemy.setY(creature.getY() + 10);
        			grubEnemy.setVelocityX(0.7f);
        			}else{
        			grubEnemy.setX(creature.getX() - 10);
        			grubEnemy.setY(creature.getY() + 10);
        			grubEnemy.setVelocityX(-0.7f);
        			}
        			creature.bug_count = System.currentTimeMillis();
        			creature.grub_count++;
        			return grubEnemy;
        			
        			
        			
        		}
        		else if(map.getPlayer().getVelocityX()==0)
        			if(System.currentTimeMillis() - creature.bug_count > 2000)
        			if(creature.grub_count == 0)
        					 {
        			GrubBullet grubEnemy = 
        				(GrubBullet) resourceManager.getGrubBullet().clone();
        			if(!creature.face_left){
        			grubEnemy.setX(creature.getX() + 10);
        			grubEnemy.setY(creature.getY() + 10);
        			grubEnemy.setVelocityX(0.7f);
        			}else{
        			grubEnemy.setX(creature.getX() - 10);
        			grubEnemy.setY(creature.getY() + 10);
        			grubEnemy.setVelocityX(-0.7f);
        			}
        			creature.bug_count = System.currentTimeMillis();
        			creature.grub_count++;
        			return grubEnemy;
        			
        			
        		}
        					
        	   else if(
        			(map.getPlayer().getVelocityX()!=0))
        			if(System.currentTimeMillis() - creature.bug_count > 500){
        			GrubBullet grubEnemy = 
        				(GrubBullet) resourceManager.getGrubBullet().clone();
        			if(!creature.face_left){
        			grubEnemy.setX(creature.getX() + 10);
        			grubEnemy.setY(creature.getY() + 10);
        			grubEnemy.setVelocityX(0.7f);
        			}else{
        			grubEnemy.setX(creature.getX() - 10);
        			grubEnemy.setY(creature.getY() + 10);
        			grubEnemy.setVelocityX(-0.7f);
        			}
        			creature.bug_count = System.currentTimeMillis();
        			creature.grub_count++;
        			return grubEnemy;
        		}
        	}else{
        		creature.bug_count = System.currentTimeMillis();
        	}
        }
        return null;

    }
    public void checkBeingShot(Bullet bullet, Player player){
    	Sprite collisionSprite = getSpriteCollision(bullet);
    	if(collisionSprite != null){
    		bullet.setState(2);
    		if(collisionSprite instanceof Grub){
    			soundManager.play(deadSound);
    			Creature badguy = (Creature)collisionSprite;
    			badguy.setState(1);	
    			player.hp += 10;
    		}
    		if(collisionSprite instanceof GrubBullet){
    			((Creature) collisionSprite).setState(Creature.STATE_DEAD);
    		}
    	}
    }

    /**
        Checks for Player collision with other Sprites. If
        canKill is true, collisions with Creatures will kill
        them.
    */
    public void checkPlayerCollision(Player player,
        boolean canKill)
    {
        if (!player.isAlive()) {
        	time2 = 0;
            return;
        }

        // check for player collision with other sprites
        Sprite collisionSprite = getSpriteCollision(player);
        if (collisionSprite instanceof PowerUp) {
            acquirePowerUp((PowerUp)collisionSprite);
            
            if(collisionSprite instanceof PowerUp.Star){
            	player.setInvincible(true);
            }
            if(collisionSprite instanceof PowerUp.Mushroom){
            	player.hp += 5;
            }
            if(collisionSprite instanceof PowerUp.Explode){
            	if(!player.getInvincible()){
            		player.hp -= 10;
            	}
            }
            if(collisionSprite instanceof PowerUp.Gas){
            	if(!player.getGassed() && !player.getInvincible()){
            		soundManager.play(gasSound);
            	}
            	player.setGassed(true);
            	
            	if (Math.round(player.getVelocityY()) >= 0 &&
            			player.getY() < collisionSprite.getY()) {
                    player.setVelocityY(0);
                }
            	else if (player.getVelocityX() > 0 && 
            			player.getX() < collisionSprite.getX()) {
                    player.setX(collisionSprite.getX()-
                    		collisionSprite.getWidth());
                }
                else if (player.getVelocityX() < 0 &&
                		player.getX() > collisionSprite.getX()) {
                    player.setX(collisionSprite.getX()+
                    		collisionSprite.getWidth());
                }
            	
                
            }
            
        }
        else if (collisionSprite instanceof GrubBullet){
        	Creature grubEnemy = (Creature)collisionSprite;
        	grubEnemy.setState(Creature.STATE_DEAD);
        	if (!player.getInvincible()) {
        		player.hp -= 5;
        		if(player.getHp() == 0){
            		player.setState(Creature.STATE_DYING);
            	}
        	}
        	
        }
        else if (collisionSprite instanceof Bullet){
        	//do nothing
        }
        else if (collisionSprite instanceof Creature) {
            Creature badguy = (Creature)collisionSprite;
            if (canKill || player.getInvincible()) {
                soundManager.play(boopSound);
                badguy.setState(Creature.STATE_DYING);
                player.setY(badguy.getY() - player.getHeight());
                if(canKill) {
                	player.jump(true);
                }
                player.hp += 10;
            }
            else {
                // player dies!
                player.setState(Creature.STATE_DYING);
                this.invincibleTime = System.currentTimeMillis();
            }
        }
    }



    public void acquirePowerUp(PowerUp powerUp) {
        // remove it from the map
        map.removeSprite(powerUp);

        if (powerUp instanceof PowerUp.Star) {
            // do something here, like give the player points
            soundManager.play(prizeSound);
        }
        else if (powerUp instanceof PowerUp.Music) {
            // change the music
            soundManager.play(prizeSound);
            toggleDrumPlayback();
        }
        else if (powerUp instanceof PowerUp.Goal) {
            // advance to next map
            soundManager.play(prizeSound,
                new EchoFilter(2000, .7f), false);
            map = resourceManager.loadNextMap();
        }
        else if (powerUp instanceof PowerUp.Mushroom) {
            soundManager.play(mushroomSound);
        }
        else if (powerUp instanceof PowerUp.Explode) {
            soundManager.play(explodeSound);
        }
        else if (powerUp instanceof PowerUp.Gas) {
            soundManager.play(gasSound);
        }
    }

}
