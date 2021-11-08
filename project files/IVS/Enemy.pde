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
    
    void reset()
    {
        for (Enemy _e: enemies)
        {
            _e.xpos = initxpos;
            _e.ypos = initypos;
            _e.isHit = false;
        }
    }

    Enemy display()
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
    
    void displayHitbox()
    {
        rect(xpos, ypos, sprite.width, sprite.height);
    }
    
    void move()
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

void loadEnemies()
{
    enemyTable = loadTable("csv/enemyPos.csv", "header");
    enemies = new Enemy[enemyTable.getRowCount()];
    String path = "sprites/";
    int e = 0;
    for (TableRow row : enemyTable.rows())
    {
        PImage _sprite = loadImage(path + row.getString("sprite"));
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

void enemyCollision(Character _player, Enemy _enemy)
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
