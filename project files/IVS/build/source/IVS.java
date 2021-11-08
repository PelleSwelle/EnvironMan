import processing.core.*; 
import processing.data.*; 
import processing.event.*; 
import processing.opengl.*; 

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
//import sound;

/*    UTIL    */
int backgroundColor = 0xff6A6767;
int gridSize = 100;
int halfgrid = gridSize / 2;
boolean[] keyHeldDown = new boolean[255]; // array for holding keys currently being held down

/*    PLATFORMS    */
Platform[] platforms;
Table levelTable;

static int floorHeight; // y value to be used for checking collision

/*    ENVIROMAN    */
Character enviroman; // instance of the class Character called enviroman.

/*    ENEMIES    */
Enemy[] enemies;
String enemyFile = "enemyPos";
Table enemyTable;
Enemy testEnemy;
Enemy anotherEnemy;



/*    BACKGROUND    */
BackgroundLayer layer1;
BackgroundLayer layer2;
BackgroundLayer layer3;
BackgroundLayer[] bgLayers;
boolean _focusConfirmed= false;
public void setup()
{
    //fullScreen();
    
    /*background layers*/
    layer1 = new BackgroundLayer(loadImage("backgrounds/treesDark0.png"), 0, 1);
    layer2 = new BackgroundLayer(loadImage("backgrounds/treesDark1.png"), 300, 2);
    layer3 = new BackgroundLayer(loadImage("backgrounds/treesDark2.png"), 600, 3);
    bgLayers = new BackgroundLayer[3];
    
    bgLayers[0] = layer3;
    bgLayers[1] = layer2;
    bgLayers[2] = layer1;
    
    
    /*    UTIL    */
    
    loadEnemies();
    loadLevel();
    /*    INSTANTION    */
    enviroman = new Character(width / 5, height / 2); // instantiate Enviroman
    
    
    
    /*    ARRAYS    */
    
    
    background(backgroundColor); // set background color
    
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
    
}

public void draw()
{   
    background(backgroundColor);
    
    for (BackgroundLayer l: bgLayers) l.display(); // dette skal laves til et grid
    
    for (Platform _platform : platforms) 
    {
        collision(enviroman, _platform);
        // procedurally loads platforms as to not take too much ram
        if (_platform.xpos > 0 && _platform.xpos < width)
        {
            _platform.display();
            
        }
        
    }
    
    enviroman.move().display(); // all methods in character class
    
    
    
    /*    DEBUG    */
    for (Platform _platform : platforms) _platform.debug();
    enviroman.debug();
    
    for (Enemy _enemy: enemies) _enemy.display().move();
    //for (Enemy _enemy: enemies) _enemy.timer.start();
      
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
        int _xAxis = row.getInt("xAxis");
        int _yAxis = row.getInt("yAxis");
        
        enemies[e] = new Enemy(_sprite, _x, _y, _xAxis, _yAxis);
        e++;
    }
    println("Enemies loaded");
}
public void loadLevel()
{
    levelTable = loadTable("level.csv", "header");
    platforms = new Platform[levelTable.getRowCount()];
    int i = 0;
    for (TableRow row : levelTable.rows()) 
    {
        platforms[i] = new Platform(row.getInt("x")*gridSize, height - row.getInt("y")*gridSize);
        i++;
    }
    println("Level loaded");
}

public void grabberCollision(Grabber _grabber, Enemy _enemy)
{
    
}

public void collision(Character _player, Platform _platform)
{
    // distance from edge of player to edge of platform
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
                else 
                {
                    _player.collisionFace = "bottom";
                    _player.ypos -= yOverlap;
                    _player.isStanding = true;
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
    float xRadius;
    float yRadius;
    String collisionFace;

    //PVector position = new PVector(0, height/2); // spawn position
    int xpos, ypos;

    float runSpeed = 0.2f; // number of pixels to move per frame
    float topSpeed = 5;

    // directions
    float xAcceleration, yAcceleration, xVelocity, yVelocity, friction;k


    //sprites
    PImage spriteIdle = loadImage("sprites/enviroman/idle1.png");
    PImage spriteRunL = loadImage("sprites/enviroman/200200 GIF/GIF left.gif");
    PImage spriteRunR = loadImage("sprites/enviroman/200200 GIF/GIF right.gif"); //run1_R.png

    /*GRABBER*/
    Grabber grabber;

    //states     Use for both sprite switching and collision
    boolean isJumping;
    boolean isStanding;
    boolean idle;
    boolean runningLeft;
    boolean runningRight;
    //Gif leftAnimation;

    float feetY;

    Character(int _xpos, int _ypos) // constructor (used when instantiation)
    {
        idle = true; // set initial state to idle
        xpos = _xpos;
        ypos = _ypos;
        //xpos = _xpos; // set the x position to the entered values.
        //ypos = _ypos; // set the y position to the entered values.
        grabber = new Grabber(xpos, ypos);
        currentSprite = spriteIdle;
        xAcceleration = 0;
        yAcceleration = 0;
        xVelocity = 0;
        yVelocity = 0;
        friction = 0.96f;

    }


    public Character display() // to be run every frame. could also be called update()
    {
        imageMode(CORNER); // A character gets drawn from the top left corner of the sprite.

    //    feetY = ypos + currentSprite.height;
        // display the chosen sprite
        image(currentSprite, xpos, ypos);

        xRadius = currentSprite.width / 2;
        yRadius = currentSprite.height / 2;

        grabber.display();
        return this;
    }

    public Character attack()
    {
        //grabber.isExtended = true;

        //for (Enemy e: enemies)
        //{
        //    if (grabber.isExtended == true)
        //    {
        //        e.isHit = true;
        //    }
        //    else
        //    {
        //        e.isHit = false;
        //    }
        //}
        //grabber.isExtended = false;
        return this;
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


        if (keyHeldDown['R']) resetPosition(); // RESET

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

        // for (Enemy _enemy : enemies)
        // {
        //     _enemy.xpos += xVelocity;
        // }

        /*    FALL FROM TOP IF DISAPPEAR AT BOTTOM, TO BE DELETED    */
        if (ypos > height + currentSprite.height)
        {
            ypos = 0;
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


    public Character debug() // only for debugging
    {
        debugText = ('(' + str(PApplet.parseInt(xpos)) + " , " + str(PApplet.parseInt(xpos)) + ')'); // what to write?

        fill(0);
        textAlign(CORNER);
        //text("enviroman position: " + debugText, 20, 50, 30); // displays position coordinates on screen.

        return this;
    }



    public void resetPosition()
    {
        xpos = width /4;
        ypos = height /2;
    }
}
class Enemy
{
    Timer timer;
    PVector moveAxis;
    PVector position;
    boolean isHit;
    
    int axis; // 0 = horizontal, 1 = vertical
    
    PImage sprite;
    
    Enemy(PImage _sprite, int _xpos, int _ypos, int _xAxis, int _yAxis)
    {
        sprite = _sprite; //
        position = new PVector(_xpos, _ypos);
        moveAxis = new PVector(_xAxis, _yAxis);
    }
    
    public Enemy display()
    {
        sprite.resize(150, 0);
        rectMode(CORNER);
        if (isHit)
        {
            //text("aw", position.x, position.y- 30);
            position.add(new PVector(20, 0));
            image(sprite, position.x, position.y);
            
        }
        else
        {
            image(sprite, position.x, position.y);
        }
        return this;
    }
    
    public Enemy move()
    {
        //switch(timer.cycle)
        //{
        //    case 1:
        //    position.add(moveAxis);
        //    case 2:
        //    position.sub(moveAxis);
        //}
        
        //if (timer.cycle == 1)
        //{
            
        //}
        //else if (timer.cycle == 2)
        //{
            
        //}
        //position.add(_axis);
        return this;
    }
}
class Grabber
{
    PShape debugSquare;
    /*PLACEHOLDER GRAPHICS*/
    PImage idleGraphic;
    PImage extendedGraphic;

    PImage currentGraphic;

    float xpos;
    float ypos;
    int idleLength = 50;
    int headSize = 30;

    boolean isExtended;

    Grabber(int _xpos, int _ypos)
    {
        xpos = _xpos;
        ypos = _ypos;
        idleGraphic = loadImage("sprites/grabber/frame0003.png");
        extendedGraphic = loadImage("sprites/grabber/frame0000.png");
        currentGraphic = idleGraphic; // initial state
        //idleEnd = new PVector(position.x + idleLength, position.y);
    }

    public Grabber display()
    {
        if (isExtended)
        {
            currentGraphic = extendedGraphic;
        }
        else
        {
            currentGraphic = idleGraphic;
        }
        xpos = enviroman.xpos;
        ypos = enviroman.ypos;
        image(currentGraphic, xpos + enviroman.xRadius, ypos);
        return this;
    }

    public void hit(Enemy _enemy)
    {
        _enemy.sprite = null;

        text("aw", _enemy.position.x, _enemy.position.y - 20);
    }
}
class Platform
{
    PImage body; //placeholder graphic  
    int platformColor = 0xffAB7922;
    int xpos, ypos;
    String debugText; // string used for debugging.
    float middlex;
    float middley;
    float radius;
    
    //boolean onTop = enviroman.ypos <= this.ypos-IVS.gridSize;

    Platform(int _xpos, int _ypos) // constructor (used when instantiating)
    {
        xpos = _xpos;
        ypos = _ypos;
        body = loadImage("sprites/platforms/groundGrass.png");
        radius = body.width/2;
    }

    public Platform display() // runs every frame.
    {
        stroke(0);
        strokeWeight(2);
        fill(platformColor);
        rectMode(CORNER); 
        image(body, xpos, ypos); // create the shape
        
        return this;
    }
    
    public Platform debug() // only for debugging
    {
        textSize(16);
        debugText = ('(' + str(PApplet.parseInt(xpos)) + " , " + str(PApplet.parseInt(ypos)) + ')');
        fill(0);
        textAlign(CENTER, CENTER);
        text(debugText, xpos + gridSize/2, ypos + gridSize/2, 30);
        
        return this;
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
