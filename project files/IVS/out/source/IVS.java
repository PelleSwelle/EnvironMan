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

//import gifAnimation.*;


/*    UTIL    */
int backgroundColor = 0xff6A6767;
int gridSize;
int halfgrid = gridSize / 2;
boolean[] keyHeldDown = new boolean[255]; // array for holding keys currently being held down

/*    PLATFORMS    */
Platform[] platforms;
Table levelTable;

//static int floorHeight; // y value to be used for checking collision

/*    ENVIROMAN    */
Character enviroman; // instance of the class Character called enviroman.

/*    ENEMIES    */
Enemy[] enemies;
String enemyFile = "enemyPos";
Table enemyTable;
Enemy testEnemy;
Enemy anotherEnemy;
Timer enemyTimer;

/*    BACKGROUND    */
BackgroundLayer layer1;
BackgroundLayer layer2;
BackgroundLayer layer3;
BackgroundLayer[] bgLayers;
boolean _focusConfirmed= false;

/* SOUND */

SoundFile woodTheme;
SoundFile deathScreen;

public void setup()
{
    /*    UTIL    */
    
    //fullScreen();
    
    gridSize = height / 20;
    
    /*    LOAD / INSTANTIATE THINGS INTO GAME    */ 
    layer1 = new BackgroundLayer(loadImage("backgrounds/treesDark0.png"), 0, 1);
    layer2 = new BackgroundLayer(loadImage("backgrounds/treesDark1.png"), 300, 2);
    layer3 = new BackgroundLayer(loadImage("backgrounds/treesDark2.png"), 600, 3);
    // background layers
    bgLayers = new BackgroundLayer[3];
    bgLayers[0] = layer3;
    bgLayers[1] = layer2;
    bgLayers[2] = layer1;
    
    loadEnemies();
    loadLevel();
    enviroman = new Character(width / 5, height / 2); // instantiate Enviroman
    
    // SOUND IMPORT
    woodTheme = new SoundFile(this,"Sounds/ENVIRONMAN_WOODS_LOOP.mp3");
    deathScreen = new SoundFile(this,"Sounds/ENVIRONMAN_DEATH_SCREEN.mp3");
    
    enemyTimer = new Timer(4);
    

    /*    MAKE SURE THAT WINDOW HAS FOCUS AT THE START    */
    if (millis() < 20000 && !_focusConfirmed )
    {
        if( !focused )
        {
            println("not focused", millis(),"mSec");
            ((java.awt.Canvas) surface.getNative()).requestFocus();
        }
        else
        {
            println("screen has focus", millis(),"mSec");
            _focusConfirmed= true;
        }
    }
    //woodTheme.play();
}

public void draw()
{  
    if (!enviroman.isDead)
    {
        background(backgroundColor);

        for (BackgroundLayer l: bgLayers) l.display(); // dette skal laves til et grid

        for (Platform _platform : platforms)
        {
            pfCollision(enviroman, _platform);
            // procedurally loads platforms as to not take too much ram
            if (_platform.xpos > 0 && _platform.xpos < width-gridSize*4)
            {
                _platform.display();
            }
        }    

        enviroman.move().display(); // all methods in character class

        /*    DEBUG    */
        for (Platform _platform : platforms) _platform.debug();
        enviroman.debug();
    
        for (Enemy _enemy: enemies) 
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
        //enemyTimer.start();
        
        
        if (enviroman.grabber.isExtended)
        {
            for (Enemy _enemy: enemies)
            {
                grabberCollision(enviroman.grabber, _enemy);
            }
        }
        for (Enemy _enemy: enemies)
        {
            enemyCollision(enviroman, _enemy);
        }
        
        enviroman.displayPointBar();
        text("fps: " + round(frameRate), width - 200, 100);
        ellipse(enviroman.xpos + enviroman.xRadius, enviroman.ypos + enviroman.yRadius, enviroman.xRadius, enviroman.xRadius);

        /*    DISPLAY COORDINATES ON PLATFORMS    */
        int i = 0;
        for (TableRow row : levelTable.rows())
        {
            textMode(CENTER);
            //text("(" + row.getInt("x") + ", " + row.getInt("y") +")", platforms[i].xpos, platforms[i].ypos + platforms[i].radius);
            i++;
        }
    }
    else /*    DEATH SCREEN    */
    {
        fill(0);
        rect(0, 0, width, height);
        fill(255);
        textMode(CENTER);
        text("You died, asshole", width/2, height/2);
         woodTheme.stop();
         deathScreen.play();
         deathScreen.loop();
         if (keyHeldDown['R'])
         {
             deathScreen.stop();
             enviroman.isDead = false;
             enviroman.resetPosition();
         }
    }
    
}

public void loadEnemies()
{
    enemyTable = loadTable("enemyPos.csv", "header");
    enemies = new Enemy[enemyTable.getRowCount()];
    
    int e = 0;
    for (TableRow row : enemyTable.rows())
    {
        PImage _sprite = loadImage(row.getString("sprite"));
        int _x = row.getInt("enemyX")*gridSize;
        int _y = height-row.getInt("enemyY")*gridSize;
        //int _xAxis = row.getInt("xAxis");
        //int _yAxis = row.getInt("yAxis");

        enemies[e] = new Enemy(_sprite, _x, _y);
        e++;
    }
    println("Enemies loaded");
}


public void loadLevel()
{
    levelTable = loadTable("levelPos.csv", "header");
    platforms = new Platform[levelTable.getRowCount()];
    int i = 0;
    for (TableRow row : levelTable.rows())
    {
        platforms[i] = new Platform(row.getInt("x")*gridSize, height - row.getInt("y")*gridSize);
        text("(" + row.getInt("x") + ", " + row.getInt("y") +")", platforms[i].xpos+platforms[i].radius, platforms[i].ypos + platforms[i].radius);
        i++;
        }
    println("Level loaded");
}

public void grabberCollision(Grabber _grabber, Enemy _enemy)
{
    float xDist = abs((_grabber.headX-_grabber.headRadius) - (_enemy.xpos + _enemy.xRadius));
    float yDist = abs((_grabber.headY+_grabber.headRadius) - (_enemy.ypos + _enemy.yRadius));
    
    float xRadiiCombined = _grabber.headRadius + _enemy.xRadius;
    float yRadiiCombined = _grabber.headRadius + _enemy.yRadius;
    text("xdist: " + xDist, 500, 200);
    text("yDist: " + yDist, 500, 250);
    text("xRadiiCombined: " + xRadiiCombined, 500, 300);
    text("yRadiiCombined: " + yRadiiCombined, 500, 350);
    
    /*    DRAW HITBOX DEBUG    */
    noFill();
    rect(_grabber.headX, _grabber.headY, _grabber.headDiam, _grabber.headDiam);
    //line(_grabber.xpos, _grabber.ypos, _grabber.xpos + _grabber.currentSprite.width, _grabber.ypos);
    
    
    if (xDist < xRadiiCombined)
    {
        if (yDist < yRadiiCombined)
        {
          _grabber.hit(_enemy);
        }
    }
    
    
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
            if (!_enemy.isHit)
            {
                _player.isDead = true;
        
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


public void pfCollision(Character _player, Platform _platform)
{
    // distance from center of player to center of platform
    float xDist = (_player.xpos + _player.xRadius) - (_platform.xpos + _platform.radius);
    float yDist = (_player.ypos + _player.yRadius) - (_platform.ypos + _platform.radius);
    

    // combined radii of player and platform
    float xRadiiCombined = _player.xRadius + _platform.radius;
    float yRadiiCombined = _player.yRadius + _platform.radius;

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
                else if (yDist < 0)
                {
                    _player.collisionFace = "bottom";
                    _player.isStanding = true;
                    _player.ypos -= yOverlap;
                    
                }
            }
            else
            {
                if (xDist > 0)
                {
                    _player.collisionFace = "left";
                    _player.xpos += xOverlap;
                }
                else
                {
                  _player.collisionFace = "right";
                  _player.xpos -= xOverlap;
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


public void keyPressed() // every time a key is pressed...
{
    keyHeldDown[keyCode] = true; // set that key to pressed in the keyHeldDown array
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
    
    BackgroundLayer(PImage _image, int _xpos, int _layer)
    {
        image = _image;
        //image.resize(0, height/2);
        xpos = _xpos;
        layer = _layer;
        ypos = height / 2;
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
    float jumpVelocity = -50;
    float xRadius, yRadius;
    String collisionFace;
    boolean isDead;
    int points;
    int barWidth = width / 2, barHeight = 50;
    int initX, initY;

    int xpos, ypos;

    float runSpeed = 0.2f; // number of pixels to move per frame
    float topSpeed = 8;

    // directions
    float xAcceleration, yAcceleration, xVelocity, yVelocity, friction;


    //sprites
    PImage spriteIdle = loadImage("sprites/enviroman/idle1.png");
    PImage spriteRunL = loadImage("sprites/enviroman/run1_L.png");
    PImage spriteRunR = loadImage("sprites/enviroman/run1_R.png"); 
    

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
        spriteRunL.resize(0, spriteIdle.height);
        spriteRunR.resize(0, spriteIdle.height);
        xpos = _xpos;
        ypos = _ypos;
        //xpos = _xpos; // set the x position to the entered values.
        //ypos = _ypos; // set the y position to the entered values.
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
        rect(xpos, ypos, currentSprite.width, currentSprite.height);

        grabber.display();
        
        return this;
    }
    
    public void displayPointBar()
    {
        fill(255);
        rectMode(CENTER);
        rect(width / 2, 50, barWidth, barHeight); // background bar
        rectMode(CORNER);
        fill(0, 255, 0);
        rect(width / 2, 50, map(points, 0, 100, 0, barWidth) ,barHeight); // foreground bar        
    }

    public Character move() // method for moving the character.
    {
        /*    INPUTS    */
        if (keyHeldDown['A']) //   LEFT
        {
            xAcceleration =  -runSpeed; // acceleration goes into velocity
            currentSprite = spriteRunL;
            friction = 1;
        }
        if (keyHeldDown['D']) //   RIGHT
        {
            xAcceleration = runSpeed;
            currentSprite = spriteRunR;
            friction = 1;
        }
        else if (!keyHeldDown['A'] && !keyHeldDown['D']) // STANDING STILL
        {
            xAcceleration = 0;
            friction = 0.96f;
            currentSprite = spriteIdle;
        }
        if (keyHeldDown[' '] && isStanding) //   JUMP
        {
            yVelocity += jumpVelocity;
            isStanding = false;
            friction = 1;
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
            println("is dead: " + isDead);
            isDead = false;
            resetPosition();
        } 

        // add acceleration to velocity
        xVelocity += xAcceleration;
        yVelocity += yAcceleration;


        yVelocity += gravity; //add gravity

        if (isStanding)
        {
            xVelocity *= friction; // add friction
            textSize(30);
            fill(0);
            textSize(30);
            text("is standing", width / 2, height / 2);
        }


        /*    APPLY TOP SPEED  */
        if (xVelocity > topSpeed)  xVelocity = topSpeed;
        if (xVelocity < -topSpeed) xVelocity = -topSpeed;
        if (yVelocity > topSpeed*2) yVelocity = topSpeed * 2;

        /*    MOVE PLATFORMS    */
        for (Platform _p : platforms)
        {
            _p.xpos -= xVelocity;
        }

            /*    MOVE BACKGROUND LAYERS    */
        for (BackgroundLayer _layer : bgLayers)
        {
            _layer.xpos -= xVelocity/_layer.layer;
        }

        for (Enemy _enemy : enemies)
        {
            _enemy.xpos -= xVelocity;
        }

        /*    FALL FROM TOP IF DISAPPEAR AT BOTTOM, TO BE DELETED    */
        if (ypos > height + currentSprite.height)
        {
            ypos = 0;
            //isDead = true;
        }

        ypos += yVelocity; // add velocity to position

        /*  DEBUG   */
        textSize(30);
        fill(0);
        text("x acceleration: " + xAcceleration, 0, 100);
        text("y acceleration: " + yAcceleration, 0, 150);
        text("x Velocity: " + round(xVelocity), 0, 200);
        text("y Velocity: " + round(yVelocity), 0, 250);
        
        return this;
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


    public void debug() // only for debugging
    {
        debugText = ('(' + str(PApplet.parseInt(xpos)) + " , " + str(PApplet.parseInt(xpos)) + ')'); // what to write?

        fill(0);
        textAlign(CORNER);
        //text("enviroman position: " + debugText, 20, 50, 30); // displays position coordinates on screen.

    }

    public void resetPosition()
    {
        xpos = width /4;
        ypos = height /2;
    }
}
class Enemy
{
    int xpos;
    int ypos;
    float xRadius;
    float yRadius;
    
    boolean isHit;
    
    PImage sprite;
    
    Enemy(PImage _sprite, int _xpos, int _ypos)
    {
        xpos = _xpos;
        ypos = _ypos;
        sprite = _sprite; 
        
        xRadius = sprite.width / 2;
        yRadius = sprite.height / 2;
    }
    
    public Enemy display()
    {
        sprite.resize(gridSize, 0);
        rectMode(CORNER);
        if (isHit)
        {
            fill(0);
            textSize(40);
            text("Motherfucker!!!", width / 2 , height / 2);
        }
        else
        {
            image(sprite, xpos, ypos);
        }
        return this;
    }
    public void move()
    {
        //switch(enemyTimer.cycle)
        //{
        //    case 1:
        //    xpos += 1;
        //    case 2:
        //    xpos -= 1;
        //}
        
        //if (enemyTimer.cycle == 1)
        //{
            
        //}
        //else if (enemyTimer.cycle == 2)
        //{
            
        //};
    }
}
class Grabber
{
    PShape debugSquare;
    /*PLACEHOLDER GRAPHICS*/
    PImage spriteIdle;
    PImage spriteExtended;

    PImage currentSprite;

    float xpos;
    float ypos;
    float headX;
    float headY;
    float headDiam;
    float headRadius = headDiam / 2;
    float xOffset;
    float yOffset;
    
    
    boolean isExtended;

    Grabber()
    {
        spriteIdle = loadImage("sprites/grabber/frame0003.png");
        spriteExtended = loadImage("sprites/grabber/frame0000.png");
        spriteIdle.resize(0, gridSize/3);
        spriteExtended.resize(0, gridSize/3);
        currentSprite = spriteIdle;
        
        currentSprite = spriteIdle; // initial state
    }

    public void display()
    {
        if (isExtended)
        {
            currentSprite = spriteExtended;
        }
        else
        {
            currentSprite = spriteIdle;
        }
        /*    POSITION IS ALWAYS TIED TO ENVIROMAN, BUT OFFSET SLIGHTLY*/
        xOffset = enviroman.currentSprite.width;
        yOffset = enviroman.currentSprite.height / 2;
        
        xpos = enviroman.xpos + xOffset;
        ypos = enviroman.ypos + yOffset;
        image(currentSprite, xpos, ypos);
        headDiam = currentSprite.width - currentSprite.width / 8*2;
        headX = xpos + currentSprite.width - headDiam;
        headY = ypos;
    }


    public void hit(Enemy _enemy)
    {
        _enemy.isHit = true;
        enviroman.points += 10;
    }
}
class Platform
{
    PImage sprite; //placeholder graphic  
    int platformColor = 0xffAB7922;
    int xpos, ypos;
    String debugText; // string used for debugging.
    float radius;
    
    
    Platform(int _xpos, int _ypos) // constructor (used when instantiating)
    {
        xpos = _xpos;
        ypos = _ypos;
        sprite = loadImage("sprites/platforms/groundGrass.png");
        sprite.resize(gridSize, 0);
        radius = sprite.width/2;
    }

    public void display() // runs every frame.
    {
        stroke(0);
        strokeWeight(2);
        fill(platformColor);
        rectMode(CORNER); 
        image(sprite, xpos, ypos); // create the shape
        
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

    
class Timer
{
    int startTime;
    int targetTime;
    int elapsedTime;
    
    int cycle;
    int noOfCycles = 2;
    
    Timer(int _targetTime)
    {
        cycle = 1;
        targetTime = _targetTime;
    }
    
    public void start()
    {
        startTime = millis();
    }
    
    public boolean isFinished()
    {
        elapsedTime = millis() - startTime;
        if (elapsedTime > targetTime)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    //void resetTimer() // only used for enemies
    //{`
    //    if (cycle == noOfCycles)
    //    {
    //        cycle = 1;
    //    }
    //    else
    //    {
    //        cycle ++;
    //    }
    //}
}
  public void settings() {  size(1920, 1080); }
  static public void main(String[] passedArgs) {
    String[] appletArgs = new String[] { "IVS" };
    if (passedArgs != null) {
      PApplet.main(concat(appletArgs, passedArgs));
    } else {
      PApplet.main(appletArgs);
    }
  }
}
