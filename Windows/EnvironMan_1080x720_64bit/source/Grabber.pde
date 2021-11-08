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
    
    void reload()
    {
    }

    void display()
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
    
    void displayHitbox()
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
    
    void switchSprite(PImage _sprite)
    {
        currentSprite = _sprite;
    }
    
    void hit(Enemy _enemy)
    {
        if (!_enemy.isHit)
        {
            _enemy.isHit = true;
            enviroman.points += 10;
        }
    }
}

void grabberCollision(Grabber _grabber, Enemy _enemy)
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
