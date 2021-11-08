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
        friction = 0.96;
        isDead = false;
        points = 0;
    }

    Character display() // to be run every frame. could also be called update()
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
    
    void displayPointBar()
    {
        fill(255);
        rectMode(CORNER);
        pointsBackground = createShape(RECT, width / 2 - barWidth / 2, 50, barWidth, barHeight);
        shape(pointsBackground);
        fill(0, 255, 0);
        pointsForeground = createShape(RECT, width / 2 - barWidth / 2, 50, points * pointMultiplier, barHeight);
        shape(pointsForeground);
    }

    Character move() // method for moving the character.
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
            friction = 0.96;
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
    
    void run(String _dir) // not moving the player, but moving everything else
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

    void checkPlatforms()
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
    void checkEnemies()
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
    void displayHitbox()
    {
        rect(xpos, ypos, currentSprite.width, currentSprite.height);
        rect(xpos, ypos, 20, 20);
        //grabber.displayHitbox();
    }


    void debug() // only for debugging
    {
        debugText = ('(' + str(int(xpos)) + " , " + str(int(xpos)) + ')'); // what to write?

        fill(0);
        textAlign(CORNER);
        //text("enviroman position: " + debugText, 20, 50, 30); // displays position coordinates on screen.

    }

    void resetPosition() // resets everything
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
