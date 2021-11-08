import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

import processing.sound.*; 

import java.util.HashMap; 
import java.util.ArrayList; 
import java.io.File; 
import java.io.BufferedReader; 
import java.io.PrintWriter; 
import java.io.InputStream; 
import java.io.OutputStream; 
import java.io.IOException; 

public class IVS extends PApplet {



/*    UTIL    */
int backgroundColor = 0xff6A6767;
int gridSize;
int halfgrid = gridSize / 2;
boolean[] keyHeldDown = new boolean[255]; // array for holding keys currently being held down
ReloadBar reloadBar;
boolean introScreen;
int pointsToWin = 120;

BackgroundLayer background;
/*    PLATFORMS    */
Platform[] platforms;
Table levelTable;

/*    ENVIROMAN    */
Character enviroman; // instance of the class Character called enviroman.

/*    ENEMIES    */
Enemy[] enemies;
//FinalBoss finalBoss;

/*    CSV'S    */
Table enemyTable;
Table textboxTable;

Timer enemyTimer;

boolean _focusConfirmed= false;

/*    MUSIC    */
SoundFile woodsTheme;
SoundFile factoryTheme;
SoundFile bossTheme;
SoundFile deathTheme;
/*    SOUND FX    */

SoundFile grabberSound;
SoundFile bagSound;
SoundFile bottleSound;
SoundFile currentlyPlaying;

/*    TEXT BOXES    */
TextBox introBox;
TextBox enemiesBox;
TextBox woodsBox;
TextBox woodsEndBox;
TextBox factoryIntroBox;
TextBox bossChamberBox;
TextBox goodEndingBox;
TextBox badEndingBox;

TextBox[] textBoxes;

PImage titleScreen;
PImage deathScreen;
PImage goodEnding;
PImage badEnding;


public void setup()
{
    /*    UTIL    */
    //fullScreen();
    
    gridSize = height / 17; // default height is 10

    /*    LOAD / INSTANTIATE THINGS INTO GAME    */

    background = new BackgroundLayer(loadImage("backgrounds/backgroundComplete.png"), 0);
    background.ypos = height - background.image.height;
    titleScreen = loadImage("backgrounds/introScreen.png");
    titleScreen.resize(width, height);
    
    deathScreen = loadImage("backgrounds/deathScreen.png");
    deathScreen.resize(width, height);    
    
    goodEnding = loadImage("backgrounds/goodEnding.png");
    badEnding = loadImage("backgrounds/badEnding.png");
    
    
    //   MUSIC IMPORT
    woodsTheme = new SoundFile(this,"sounds/woodsTheme.mp3");
    factoryTheme = new SoundFile(this, "sounds/industrialTheme.mp3");
    bossTheme = new SoundFile(this, "sounds/bossTheme.mp3");
    
    deathTheme = new SoundFile(this,"sounds/deathTheme.mp3");
    
    
    currentlyPlaying = woodsTheme;
    
    /*    SOUND FX IMPORT    */
    grabberSound = new SoundFile(this, "sounds/grabber.mp3");
    bagSound = new SoundFile(this, "sounds/bagFX.mp3");
    bottleSound = new SoundFile(this, "sounds/bottleFX.mp3");
    reloadBar = new ReloadBar();

 /*    UTIL    */
    loadEnemies();
    loadLevel();
    loadTextBoxes();
    enviroman = new Character(gridSize * 8, height / 2); // instantiate Enviroman
    //finalBoss = new FinalBoss();

    /*    TEXT BOXES    */

    enemyTimer = new Timer(100);
    introScreen = true;
    currentlyPlaying.loop();

    /*    MAKE SURE THAT WINDOW HAS FOCUS AT THE START    */
    if (millis() < 20000 && !_focusConfirmed )
    {
        if ( !focused )
        {
            println("not focused", millis(), "mSec");
            ((java.awt.Canvas) surface.getNative()).requestFocus();
        }
        else
        {
            println("screen has focus", millis(), "mSec");
            _focusConfirmed= true;
        }
    }
}

public void draw()
{
    if (!enviroman.isDead) // if alive
    {
        if (introScreen) // if just started
        {
           image(titleScreen, 0, 0); 
           if (keyHeldDown[' '])
           {
               introScreen = false;
           }
        }
        else
        {
            enemyTimer.count();
            background(backgroundColor);
            
            background.display();
    
            /*    TEXTBOXES    */
            for (TextBox _box: textBoxes)
            {
                imageMode(CENTER);
                _box.display();
            }
            
            /*    CHANGE MUSIC    */
            if ( textBoxes[3].xpos < width + 100 && textBoxes[3].xpos > width)
            {
                changeMusic(factoryTheme);
            }
            else if (textBoxes[5].xpos < width + 100 && textBoxes[5].xpos > width)
            {
                changeMusic(bossTheme);
            }
            
            
            /*    PLATFORMS    */
            for (Platform _platform : platforms)
            {
                imageMode(CORNER);
                pfCollision(enviroman, _platform);
                _platform.display();
            }
        
            /*    ENVIROMAN    */
            enviroman.move().display(); // all methods in character class
        
            /*    ENEMIES    */
            for (Enemy _enemy : enemies)
            {
                if (!_enemy.isHit) // only display if the enemy is not hit
                {
                    _enemy.display().move();
                }
                else
                {
                    _enemy = null; // if it is, delete it
                }
            }
            
            /*    COLLISION    */
            if (enviroman.grabber.isExtended)
            {
                for (Enemy _enemy : enemies)
                {
                    grabberCollision(enviroman.grabber, _enemy);
                }
            }
            for (Enemy _enemy : enemies)
            {
                enemyCollision(enviroman, _enemy);
            }

            enviroman.displayPointBar();
            
            if (enemies[enemies.length-1].isHit) // if finalBoss is hit
            {
                changeMusic(woodsTheme);
                if (enviroman.points < pointsToWin)
                {
                    endScreen(badEnding);
                }
                else
                {
                    endScreen(goodEnding);
                }
            }

            /*    DEBUG    */
            for (Platform _platform : platforms) _platform.debug();
            enviroman.debug();
            
            //displayHitboxes();
            //enemyTimer.displayTimer();
            //reloadBar.display();
        }
    }
    else if (enviroman.isDead)/*    DEATH SCREEN    */
    {
        if (currentlyPlaying != deathTheme)
        {
            changeMusic(deathTheme);
            image(deathScreen, 0, 0);
        }
        if (keyHeldDown['R'])
        {
            enviroman.resetPosition();
            resetLevel();
            //resetPosition();
        }
    }

    
}

public void die()
{
    println(enviroman.isDead);
    
    
}

public void resetLevel()
{
    enviroman.points = 0;
    enviroman.isDead = false;
    enviroman.xpos = enviroman.initxpos;
    enviroman.xVelocity = 0;
    println("reset");
    
    changeMusic(woodsTheme);
    for (Platform _p : platforms)
    {
        _p.xpos = _p.initxpos;
    }
    for (Enemy _e: enemies)
    {
        _e.xpos = _e.initxpos;
        _e.ypos = _e.initypos;
    }
    enemyTimer.cycle = 1;
    enemyTimer.elapsedTime = 0;
}

public void changeMusic(SoundFile _musicToPlay)
{
    currentlyPlaying.stop();
    currentlyPlaying = _musicToPlay;
    currentlyPlaying.loop();
    println(_musicToPlay);
}

public void displayHitboxes()
{
    rectMode(CORNER);
    noFill();
    stroke(255, 0, 0);
    enviroman.displayHitbox();

    for (Enemy _e : enemies)
    {
        _e.displayHitbox();
    }

    for (Platform _p: platforms)
    {
        _p.displayHitbox();
    }
}

public void endScreen(PImage _ending)
{
    _ending.resize(width, height);
    image(_ending, 0, 0);
}

public void keyPressed() // every time a key is pressed...
{
    keyHeldDown[keyCode] = true; // set that key to pressed in the keyHeldDown array

    if (keyPressed) 
    {
        if (key == 'k' || key == 'K') 
        {
            grabberSound.amp(0.5f);
            grabberSound.play();
            //enviroman.grabber.reloadTimer.recharge();
        }
    }
}
public void keyReleased() // once key is released...
{
    keyHeldDown[keyCode] = false; // set that key to released in the keyHeldDown array.
}
class BackgroundLayer
{
    PImage image;
    int xpos, ypos;
    int layer;
    int initxpos;
    
    BackgroundLayer(PImage _image, int _xpos)
    {
        image = _image;
        image.resize(216*gridSize, height);
        xpos = _xpos;
        initxpos = xpos;
        ypos = 0;
    }
    
    public void display()
    {
        image(image, xpos, ypos);
    }
}
class Character
{
    /*    UTIL    */
    PImage currentSprite; // variable to hold the current sprite.
    String debugText; // variable to hold text to be used in debug method.
    float gravity = 3;
    float jumpVelocity = -40;
    float xRadius, yRadius;
    String collisionFace;
    boolean isDead;
    int points;
    int barWidth = width / 2, barHeight = 50;
    int initxpos, initypos;
    int pointMultiplier = 3;
    boolean facingRight;

    int xpos, ypos;

    float accelerationSpeed = gridSize / 100; // number of pixels to move per frame
    float topSpeed = gridSize / 10;

    // directions
    float xAcceleration, yAcceleration, xVelocity, yVelocity, friction;
    PShape pointsBackground;
    PShape pointsForeground;

    //sprites
    PImage spriteIdle = loadImage("sprites/enviroman/idle1.png");
    PImage spriteRun_L = loadImage("sprites/enviroman/run1_L.png");
    PImage spriteRun_R = loadImage("sprites/enviroman/run1_R.png"); 
    

    /*GRABBER*/
    Grabber grabber;

    //states     Use for both sprite switching and collision
    boolean isJumping;
    boolean isStanding;
    boolean idle;
    boolean runningLeft;
    boolean runningRight;
    //Gif leftAnimation;

    //float feetY;

    Character(int _xpos, int _ypos) // constructor (used when instantiation)
    {
        spriteIdle.resize(gridSize, 0);
        spriteRun_L.resize(0, spriteIdle.height);
        spriteRun_R.resize(0, spriteIdle.height);
        xpos = _xpos;
        ypos = _ypos;
        initxpos = _xpos;
        initypos = _ypos;
        grabber = new Grabber();
        currentSprite = spriteIdle;
        xAcceleration = 0;
        yAcceleration = 0;
        xVelocity = 0;
        yVelocity = 0;
        friction = 0.96f;
        isDead = false;
        points = 0;
    }

    public Character display() // to be run every frame. could also be called update()
    {
        imageMode(CORNER); // A character gets drawn from the top left corner of the sprite.

        image(currentSprite, xpos, ypos);

        xRadius = currentSprite.width / 2;
        yRadius = currentSprite.height / 2;
        noFill();
        //rect(xpos, ypos, currentSprite.width, currentSprite.height); // show hitbox
        grabber.display();
        
        return this;
    }
    
    public void displayPointBar()
    {
        fill(255);
        rectMode(CORNER);
        pointsBackground = createShape(RECT, width / 2 - barWidth / 2, 50, barWidth, barHeight);
        shape(pointsBackground);
        fill(0, 255, 0);
        pointsForeground = createShape(RECT, width / 2 - barWidth / 2, 50, points * pointMultiplier, barHeight);
        shape(pointsForeground);
    }

    public Character move() // method for moving the character.
    {
        /*    INPUTS    */
        if (keyHeldDown['A']) //   LEFT
        {
            //xAcceleration = -accelerationSpeed; // acceleration goes into velocity
            run("left");
            currentSprite = spriteRun_L;
            facingRight = false;
        }
        if (keyHeldDown['D']) //   RIGHT
        {
            run("right");
            currentSprite = spriteRun_R;
            //friction = 1;
            facingRight = true;
        }
        else if (!keyHeldDown['A'] && !keyHeldDown['D']) // STANDING STILL
        {
            xAcceleration = 0;
            friction = 0.96f;
            currentSprite = spriteIdle;
        }
        if (keyHeldDown[' '] && isStanding) //   JUMP
        {
            {
                yVelocity += jumpVelocity;
                //run("jump");
                isStanding = false;
                friction = 1;
            }
        }
        if (keyHeldDown['P']) // ADD A POINT
        {
            enviroman.points ++;
        }
        if (keyHeldDown['K']) // ATTACK
        {
            grabber.isExtended = true;
        }
        else 
        {
            grabber.isExtended = false;
        }
        if (keyHeldDown['R'])
        {
            resetPosition();
        }

        // add acceleration to velocity
        xVelocity += xAcceleration;
        yVelocity += yAcceleration;


        yVelocity += gravity; //add gravity

        if (isStanding)
        {
            xVelocity *= friction; // add friction
            //textSize(30);
            //fill(0);
            //textSize(30);
            //text("is standing", width / 2, height / 2);
        }


        /*    APPLY TOP SPEED  */
        if (xVelocity > topSpeed)  xVelocity = topSpeed;
        if (xVelocity < -topSpeed) xVelocity = -topSpeed;
        if (yVelocity > topSpeed*2) yVelocity = topSpeed * 2;


        background.xpos -= xVelocity;

        if (ypos > height + currentSprite.height)
        {
            isDead = true;
        }

        ypos += yVelocity; // add velocity to position
        
        return this;
    }
    
    public void run(String _dir) // not moving the player, but moving everything else
    {
        if (platforms[417].xpos <= 0) // 419 is start of boss arena
        {
            if (_dir == "left")
            {
                enviroman.xpos -= topSpeed;
            }
            else if (_dir == "right")
            {
                enviroman.xpos += topSpeed;
            }
        }
        else // do this through the whole level
        {
            if (_dir == "left")
        {
            for (Platform _p : platforms) _p.xpos += topSpeed;
            for (Enemy _e : enemies) _e.xpos += topSpeed;
            for (TextBox _b: textBoxes) _b.xpos += topSpeed;
            background.xpos += topSpeed;
            //finalBoss.xpos += topSpeed;
            }
            else if (_dir == "right")
            {
                for (Platform _p : platforms) _p.xpos -= topSpeed;
                for (Enemy _e : enemies) _e.xpos -= topSpeed;
                for (TextBox _b: textBoxes) _b.xpos -= topSpeed;
                background.xpos -= topSpeed;
                //finalBoss.xpos -= topSpeed;
            }
            //else if (_dir == "jump")
            //{
            //    for (Platform _p : platforms) _p.ypos += yVelocity;
            //    for (Enemy _e : enemies) _e.ypos += yVelocity;
            //    for (TextBox _b: textBoxes) _b.ypos += yVelocity;
            //    background.xpos += yVelocity;
            //    finalBoss.xpos += yVelocity;
            //}
        }
        
    }

    public void checkPlatforms()
    {
        ////update for platform collisions
        if (collisionFace == "bottom" && yVelocity >= 0)
        {
            println("collisionFace = bottom");
            isStanding = true;
            ////flip gravity to neutralize gravity's effect
            yVelocity = -gravity;
        }
        else if (collisionFace == "top" && yVelocity <= 0)
        {
            yVelocity = 0;
        }
        else if (collisionFace == "right" && xVelocity >= 0)
        {
            xVelocity = 0;
        }
        else if (collisionFace == "left" && xVelocity <= 0)
        {
            xVelocity = 0;
        }
        if (collisionFace != "bottom" && yVelocity > 0)
        {
            isStanding = false;
        }
    }
    public void checkEnemies()
    {
        if (collisionFace == "bottom" && yVelocity >= 0)
        {
            println("collisionFace = bottom");
            
        }
        else if (collisionFace == "top" && yVelocity <= 0)
        {
            yVelocity = 0;
        }
        else if (collisionFace == "right" && xVelocity >= 0)
        {
            xVelocity = 0;
        }
        else if (collisionFace == "left" && xVelocity <= 0)
        {
            xVelocity = 0;
        }
        if (collisionFace != "bottom" && yVelocity > 0)
        {
            isStanding = false;
        }
    }
    public void displayHitbox()
    {
        rect(xpos, ypos, currentSprite.width, currentSprite.height);
        rect(xpos, ypos, 20, 20);
        //grabber.displayHitbox();
    }


    public void debug() // only for debugging
    {
        debugText = ('(' + str(PApplet.parseInt(xpos)) + " , " + str(PApplet.parseInt(xpos)) + ')'); // what to write?

        fill(0);
        textAlign(CORNER);
        //text("enviroman position: " + debugText, 20, 50, 30); // displays position coordinates on screen.

    }

    public void resetPosition() // resets everything
    {
        xpos = initxpos;
        ypos = initypos;
        background.xpos = background.initxpos;
        //finalBoss.xpos = initxpos;
        //finalBoss.ypos = initypos;
        /*    MOVE PLATFORMS    */
        for (Platform _p : platforms)
        {
            _p.xpos = _p.initxpos;
        }

        for (Enemy _enemy : enemies)
        {
            _enemy.xpos = _enemy.initxpos;
            _enemy.ypos = _enemy.initypos;
            _enemy.isHit = false;
            
        }
        for (TextBox _textBox: textBoxes)
        {
            _textBox.xpos = _textBox.initxpos;
            //_textBox.ypos -= ypos;
        }
    }
}
class Enemy
{
    int xpos;
    int ypos;
    int initxpos;
    int initypos;
    int xAxis;
    int yAxis;
    int walkSpeed = 2;
    
    float xRadius;
    float yRadius;

    boolean isHit;

    PImage sprite;

    Enemy(PImage _sprite, int _xpos, int _ypos, int _xAxis, int _yAxis)
    {
        xpos = _xpos;
        ypos = _ypos;
        initxpos = _xpos;
        initypos = _ypos;
        xAxis = _xAxis;
        yAxis = _yAxis;
        sprite = _sprite;

        xRadius = sprite.width / 2;
        yRadius = sprite.height / 2;
    }
    
    public void reset()
    {
        for (Enemy _e: enemies)
        {
            _e.xpos = initxpos;
            _e.ypos = initypos;
            _e.isHit = false;
        }
    }

    public Enemy display()
    {
        enemies[enemies.length-1].sprite.resize(gridSize*3, 0); //resize final boss
        rectMode(CORNER);
        if (isHit)
        {
            bagSound.play();
        }
        else
        {
            image(sprite, xpos, ypos);
        }
        return this;


    }
    
    public void displayHitbox()
    {
        rect(xpos, ypos, sprite.width, sprite.height);
    }
    
    public void move()
    {
        if (enemyTimer.cycle == 1) 
        {
            if (xAxis == 1)
            {
                xpos += 1 *walkSpeed;
            }
            else
            {
                ypos -= 1 * walkSpeed;
            }
            
        }
        else if (enemyTimer.cycle == 2)
        {
            if (xAxis == 1)
            {
                xpos -= 1 *walkSpeed;
            }
            else
            {
                ypos += 1 * walkSpeed;
            }
        }
    }
}

public void loadEnemies()
{
    enemyTable = loadTable("csv/enemyPos.csv", "header");
    enemies = new Enemy[enemyTable.getRowCount()];

    int e = 0;
    for (TableRow row : enemyTable.rows())
    {
        PImage _sprite = loadImage(row.getString("sprite"));
        _sprite.resize(gridSize, 0);
        int _x = row.getInt("xpos")*gridSize;
        int _y = height-row.getInt("ypos")*gridSize;
        int _xAxis = row.getInt("xAxis");
        int _yAxis = row.getInt("yAxis");
        
        enemies[e] = new Enemy(_sprite, _x, _y, _xAxis, _yAxis);
        e++;
    }
    println("Enemies loaded");
}

public void enemyCollision(Character _player, Enemy _enemy)
{
    // distance from edge of player to edge of platform
    float xDist = (_player.xpos + _player.xRadius) - (_enemy.xpos + _enemy.xRadius);
    float yDist = (_player.ypos + _player.yRadius) - (_enemy.ypos + _enemy.yRadius);

    // combined radii of player and platform
    float xRadiiCombined = _player.xRadius + _enemy.xRadius;
    float yRadiiCombined = _player.yRadius + _enemy.yRadius;

    // potential overlap of sprites
    float xOverlap;
    float yOverlap;

    if (abs(xDist) < xRadiiCombined) // doesn't matter if negative or positive, so absolute value
    {
        if (abs(yDist) < yRadiiCombined) // if overlapping vertically
        {
            xOverlap = xRadiiCombined - abs(xDist); // calculate how much overlap for both x and y
            yOverlap = yRadiiCombined - abs(yDist);
            if (!_enemy.isHit) // if enemy is alive
            {
                _player.isDead = true;
            }
        }   
    }
}
class FinalBoss
{
    PImage spriteFirst;
    PImage spriteSecond;
    PImage spriteThird;
    PImage spriteAura;
    int speed = 4;
    boolean isHit = false;
    float radius;
    int health;
    boolean isDead;
    
    float xpos, ypos;
    float initxpos, initypos;
    
    FinalBoss()
    {
        health = 3;
        String path = "sprites/finalBoss/";
        spriteFirst = loadImage(path+"boss1.png");
        spriteSecond = loadImage(path+"boss2.png");
        spriteThird = loadImage(path+"boss3.png");
        spriteAura = loadImage(path+"boss3Aura.png");
        isDead = false;
        //xpos = 203 * gridSize;
        //ypos = height - 13 * gridSize;
        
        // JUST FOR TESTING
        xpos = 4 * gridSize;
        ypos = height - 4 * gridSize;
        initxpos = xpos;
        initypos = ypos;
        radius = spriteFirst.width / 2;
    }
    
    public void move()
    {
        if (enemyTimer.cycle == 1) 
        {
            
                xpos += 1 * speed;

                ypos -= 1 * speed;
            
        }
        else if (enemyTimer.cycle == 2)
        {
            
            xpos -= 1 *speed;
        
            ypos += 1 * speed;
        }
            
    }
    
    public void display()
    {
        if (health == 3)
        {
            image(spriteFirst, xpos, ypos);
        }
        else if (health == 2)
        {
            image(spriteSecond, xpos, ypos);
        }
        else if (health == 1)
        {
            image(spriteThird, xpos, ypos);
        }
        else if (health == 0)
        {
            text("AAAAAWWWW", width / 2, height / 2);
        }
    }
}

//void bossCollision(Grabber _grabber, FinalBoss _boss)
//{
//    // distance from edge of player to edge of platform
//    float xDist = (_grabber.xpos + _grabber.xRadius) - (_boss.xpos + _boss.radius);
//    float yDist = (_grabber.ypos + _grabber.yRadius) - (_boss.ypos + _boss.radius);

//    // combined radii of player and platform
//    float xRadiiCombined = _grabber.xRadius + _boss.radius;
//    float yRadiiCombined = _grabber.yRadius + _boss.radius;

//    // potential overlap of sprites
//    float xOverlap;
//    float yOverlap;

//    if (abs(xDist) < xRadiiCombined) // doesn't matter if negative or positive, so absolute value
//    {
//        if (abs(yDist) < yRadiiCombined) // if overlapping vertically
//        {
//            xOverlap = xRadiiCombined - abs(xDist); // calculate how much overlap for both x and y
//            yOverlap = yRadiiCombined - abs(yDist);
//            if (xOverlap > 0 || yOverlap > 0)
//            {
//                enviroman.grabber.hitBoss(finalBoss);
//            }
//        }
//    }        
//}
class Grabber
{
    PShape debugSquare;
    /*    SPRITES    */
    PImage spriteIdle_L;
    PImage spriteExtended_L;
    PImage spriteIdle_R;
    PImage spriteExtended_R;
    float xRadius;
    float yRadius;
    
    PImage currentSprite;

    float xpos;
    float ypos;
    float xInset = 10;
       
       
    /*    RELOAD    */
    boolean reloading;

    Timer reloadTimer;
    int reloadTime = 50;
    
    boolean isExtended;

    Grabber()
    {
        spriteIdle_L = loadImage("sprites/grabber/idle_L.png");
        spriteIdle_L.resize(gridSize * 3, 0);
        spriteExtended_L = loadImage("sprites/grabber/extended_L.png");
        spriteExtended_L.resize(gridSize * 3, 0);
        spriteIdle_R = loadImage("sprites/grabber/idle_R.png");
        spriteIdle_R.resize(gridSize * 3, 0);
        spriteExtended_R = loadImage("sprites/grabber/extended_R.png");
        spriteExtended_R.resize(gridSize * 3, 0);
        currentSprite = spriteIdle_R; // initial state
        xRadius = currentSprite.width / 2;
        yRadius = currentSprite.height / 2;
        reloadTimer = new Timer(reloadTime);
        
    }
    
    public void reload()
    {
    }

    public void display()
    {
        if (!isExtended) // determined by input
        {
            if (enviroman.facingRight) // if facing right
            {
                switchSprite(spriteIdle_R);
                xpos = enviroman.xpos + enviroman.currentSprite.width - xInset;
            }
            else if (!enviroman.facingRight)
            {
                switchSprite(spriteIdle_L);
                xpos = enviroman.xpos - currentSprite.width + xInset;
            }
        }
        else if (isExtended)
        {
            if (enviroman.facingRight) // if facing right
            {
                switchSprite(spriteExtended_R);
                xpos = enviroman.xpos + enviroman.currentSprite.width - xInset;
            }
            else if (!enviroman.facingRight)
            {
                switchSprite(spriteExtended_L);
                xpos = enviroman.xpos - currentSprite.width + xInset;
            } 
        }
                
        ypos = enviroman.ypos + enviroman.currentSprite.height / 2;
        image(currentSprite, xpos, ypos);
    }
    
    public void displayHitbox()
    {
        noFill();
        if (enviroman.facingRight)
        {
            rect(xpos, ypos, currentSprite.width, currentSprite.height);
        }
        else if (!enviroman.facingRight)
        {
            rect(enviroman.xpos - currentSprite.width, ypos, currentSprite.width, currentSprite.height);
        }
        
    }
    
    public void switchSprite(PImage _sprite)
    {
        currentSprite = _sprite;
    }
    
    public void hit(Enemy _enemy)
    {
        if (!_enemy.isHit)
        {
            _enemy.isHit = true;
            enviroman.points += 10;
        }
    }
}

public void grabberCollision(Grabber _grabber, Enemy _enemy)
{
    // distance from edge of player to edge of platform
    float xDist = (_grabber.xpos + _grabber.xRadius) - (_enemy.xpos + _enemy.xRadius);
    float yDist = (_grabber.ypos + _grabber.yRadius) - (_enemy.ypos + _enemy.yRadius);

    // combined radii of player and platform
    float xRadiiCombined = _grabber.xRadius + _enemy.xRadius;
    float yRadiiCombined = _grabber.yRadius + _enemy.yRadius;

    // potential overlap of sprites
    float xOverlap = xRadiiCombined - abs(xDist);
    float yOverlap = yRadiiCombined - abs(yDist);
    
    if (xOverlap > 0 && yOverlap > 0)
    {
        _grabber.hit(_enemy);
    }
}
class Platform
{
    PImage sprite; //placeholder graphic  
    int platformColor = 0xffAB7922;
    int xpos, ypos;
    String debugText; // string used for debugging.
    float radius;
    int initxpos, initypos;
    
    
    Platform(PImage _sprite, int _xpos, int _ypos) // constructor (used when instantiating)
    {
        xpos = _xpos;
        ypos = _ypos;
        initxpos = _xpos;
        initypos = ypos;
        sprite = _sprite;
        sprite.resize(gridSize, 0);
        radius = sprite.width/2;
    }
    
    public void displayHitbox()
    {
        rect(xpos, ypos, sprite.width, sprite.height);
    }

    public void display() // runs every frame.
    {
        image(sprite, xpos, ypos);
        
    }
    
    public void debug() // only for debugging
    {
        //textSize(16);
        //debugText = ('(' + str(int(xpos)) + " , " + str(int(ypos)) + ')');
        //fill(0);
        //textAlign(CENTER, CENTER);
        //text(debugText, xpos + gridSize/2, ypos + gridSize/2, 30);
    }
    
}
public void loadLevel()
{
    levelTable = loadTable("csv/platformPos.csv", "header");
    platforms = new Platform[levelTable.getRowCount()];
    int i = 0;
    for (TableRow row : levelTable.rows())
    {
        PImage _sprite = loadImage(row.getString("sprite"));
        int _x = row.getInt("xpos")*gridSize - gridSize;
        int _y = height - row.getInt("ypos")*gridSize;
        
        platforms[i] = new Platform(_sprite, _x, _y);
        
        i++;
    }
    println("Level loaded");
}

public void pfCollision(Character _player, Platform _platform)
{
    float magicNumber = 2;
    // distance from center of player to center of platform
    float xDist = (_player.xpos + _player.xRadius) - (_platform.xpos + _platform.radius) + 20;
    float yDist = (_player.ypos + _player.yRadius) - (_platform.ypos + _platform.radius) + 20; // add 20 because coding is dumb...


    // combined radii of player and platform
    float xRadiiCombined = _player.xRadius + _platform.radius + magicNumber;
    float yRadiiCombined = _player.yRadius + _platform.radius + magicNumber;

    // potential overlap of sprites
    float xOverlap;
    float yOverlap;

    if (abs(xDist) < xRadiiCombined) // doesn't matter if negative or positive, so absolute value
    {
        if (abs(yDist) < yRadiiCombined) // if overlapping vertically
        {
            xOverlap = xRadiiCombined - abs(xDist); // calculate how much overlap for both x and y
            yOverlap = yRadiiCombined - abs(yDist);

            if (xOverlap >= yOverlap) // if overlapping with platform to the side
            {
                if (yDist > 0)
                {
                    _player.collisionFace = "top";

                    _player.ypos += yOverlap; // move the player to the boundary
                }
                else if (yDist < 0) // if overlapping at bottom
                {
                    _player.ypos -= yOverlap;
                    _player.collisionFace = "bottom";
                    _player.isStanding = true;
                }
            }
            else
            {
                if (xDist > 0)
                {
                    _player.collisionFace = "left";
                    //_player.xpos += xOverlap;
                }
                else
                {
                    _player.collisionFace = "right";
                    //_player.xpos -= xOverlap;
                }
            }
        }
        else
        {
            _player.collisionFace = "none";
        }
    }
    else
    {
        _player.collisionFace = "none";
    }
    if (_player.collisionFace!= "none")
    {

    }
}

    
class ReloadBar
{
    PShape background;
    PShape reloadBar;
    
    float xpos;
    float ypos;
    float barWidth;
    float reloadWidth;
    float barHeight;
    
    
    ReloadBar()
    {
        xpos = gridSize;
        ypos = gridSize*2;
        barWidth = gridSize * 4;
        barHeight = gridSize / 2;
        
        background = createShape(RECT, xpos, ypos, barWidth, barHeight);
    }
    
    public void display()
    {
        text(enviroman.grabber.reloadTimer.elapsedTime, xpos, ypos);    
        if (enviroman.grabber.reloading)
        {
            
        }
    }
}
class TextBox
{
    int xpos, ypos;
    int initxpos, initypos;
    PImage sprite;
    
    TextBox(PImage _sprite, int _xpos, int _ypos)
    {
        sprite = _sprite;
        _sprite.resize(width, height);
        xpos = _xpos;
        ypos = _ypos;
        initxpos = _xpos;
        initypos = ypos;
    }
    
    public void display()
    {
        image(sprite, xpos, ypos);
    }
}

public void loadTextBoxes()
{
    textboxTable = loadTable("csv/textboxPos.csv", "header");
    textBoxes = new TextBox[textboxTable.getRowCount()];

    int b = 0;
    for (TableRow row : textboxTable.rows())
    {
        PImage _sprite = loadImage(row.getString("sprite"));
        int _xpos = row.getInt("xpos")*gridSize;
        int _ypos = height - row.getInt("ypos")*gridSize;

        textBoxes[b] = new TextBox(_sprite, _xpos, _ypos);
        b++;
    }
    println("texboxes loaded");
}
class Timer
{
    int targetTime;
    int elapsedTime;
    
    int cycle;
    int noOfCycles = 2;
    
    Timer(int _targetTime)
    {
        cycle = 1;
        targetTime = _targetTime;
    }
    
    public void count()
    {
        elapsedTime += 1;
        if (elapsedTime >= targetTime)
        {
            reset();
        }
    }
    
    public void recharge()
    {
        while(elapsedTime < targetTime)
        {
            text(enviroman.grabber.reloadTimer.elapsedTime, reloadBar.xpos, reloadBar.ypos);
            elapsedTime += 1;
            
        }
        zeroOut();
    }
    public void displayTimer()
    {
        text("cycle: " + cycle + "  elapsedTime:  " + elapsedTime, width - 500, 200);
    }
    public void zeroOut()
    {
        elapsedTime = 0;
    }

    public void reset() // only used for enemies
    {
        if (cycle == noOfCycles)
        {
            cycle = 1;
        }
        else
        {
            cycle ++;
        }
        elapsedTime = 0;
    }
}
  public void settings() {  size(1080, 720); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "IVS" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
