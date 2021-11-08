class Character
{
    /*    UTIL    */
    String debugText; // variable to hold text to be used in debug method.
    float xRadius, yRadius;
    String collisionFace;
    boolean isDead;
    
    boolean facingRight;

    
    //float accelerationSpeed = gridSize / 100; // number of pixels to move per frame
    float topSpeed = gridSize / 10;

    /*    POSITIONS / DIRECTIONS / FORCES     */ 
    int xpos, ypos;
    int initxpos, initypos;
    float /*xAcceleration, yAcceleration, */xVelocity, yVelocity/*, friction*/;
    float gravity = 3;
    float jumpVelocity = -40;
    
    /*    POINT BAR    */
    PShape pointsBackground;
    PShape pointsForeground;
    int pointMultiplier = 3;
    int points;
    int barWidth = width / 2, barHeight = 50;
    
    /*    SPRITES    */
    PImage currentSprite; // variable to hold the current sprite.
    String path = "sprites/environman/";
    PImage spriteIdle = loadImage(path + "idle1.png");
    PImage spriteRun_L = loadImage(path + "run1_L.png");
    PImage spriteRun_R = loadImage(path + "run1_R.png"); 

    /*GRABBER*/
    Grabber grabber;

    boolean isStanding;

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
            run("left");
            currentSprite = spriteRun_L;
            facingRight = false;
        }
        if (keyHeldDown['D']) //   RIGHT
        {
            run("right");
            currentSprite = spriteRun_R;
            facingRight = true;
        }
        else if (!keyHeldDown['A'] && !keyHeldDown['D']) // STANDING STILL
        {
            currentSprite = spriteIdle;
        }
        if (keyHeldDown[' '] && isStanding) //   JUMP
        {
            {
                yVelocity += jumpVelocity;
                isStanding = false;
            }
        }
        if (keyHeldDown['P']) // ADD A POINT FOR DEBUGGING PURPOSES
        {
            points ++;
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

        yVelocity += gravity; //add gravity

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
        /*    IF AT BOSS ARENA, MOVE ENVIRONMAN AROUND ON SCREEN    */
        if (platforms[417].xpos <= 0) // 419 is start of boss arena
        {
            if (_dir == "left")
            {
                xpos -= topSpeed;
            }
            else if (_dir == "right")
            {
                xpos += topSpeed;
            }
        }
        /*    IF NOT AT BOSS ARENA, MOVE EVERYTHING AROUND ENVIROMNAN    */
        else // do this through the whole level
        {
            if (_dir == "left")
            {
                for (Platform _p : platforms) _p.xpos += topSpeed;
                for (Enemy _e : enemies) _e.xpos += topSpeed;
                for (TextBox _b: textBoxes) _b.xpos += topSpeed;
                background.xpos += topSpeed;
            }
            else if (_dir == "right")
            {
                for (Platform _p : platforms) _p.xpos -= topSpeed;
                for (Enemy _e : enemies) _e.xpos -= topSpeed;
                for (TextBox _b: textBoxes) _b.xpos -= topSpeed;
                background.xpos -= topSpeed;
            }
        }
    }

    void checkPlatforms()
    {
        if (collisionFace == "bottom" && yVelocity >= 0)
        {
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
    void displayHitbox()
    {
        rect(xpos, ypos, currentSprite.width, currentSprite.height);
        rect(xpos, ypos, 20, 20);
        grabber.displayHitbox();
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
