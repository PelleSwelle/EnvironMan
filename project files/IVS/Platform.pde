class Platform
{
    PImage sprite; //placeholder graphic  
    color platformColor = #AB7922;
    int xpos, ypos;
    int initxpos, initypos;
    String debugText; // string used for debugging.
    float radius;
    
    
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
    
    void displayHitbox()
    {
        rect(xpos, ypos, sprite.width, sprite.height);
    }

    void display() // runs every frame.
    {
        image(sprite, xpos, ypos);
    }
    
    void debug() // only for debugging
    {
        textSize(16);
        debugText = ('(' + str(int(xpos)) + " , " + str(int(ypos)) + ')');
        fill(0);
        textAlign(CENTER, CENTER);
        text(debugText, xpos + gridSize/2, ypos + gridSize/2, 30);
    }
    
}
void loadLevel()
{
    levelTable = loadTable("csv/platformPos.csv", "header");
    platforms = new Platform[levelTable.getRowCount()];
    int i = 0;
    String levelPath = "sprites/platforms/";
    for (TableRow row : levelTable.rows())
    {
        PImage _sprite = loadImage(levelPath + row.getString("sprite"));
        int _x = row.getInt("xpos")*gridSize - gridSize;
        int _y = height - row.getInt("ypos")*gridSize;
        
        platforms[i] = new Platform(_sprite, _x, _y);
        
        i++;
    }
    println("Level loaded");
}

void pfCollision(Character _player, Platform _pf)
{
    float magicNumber = 2;
    // distance from center of player to center of platform
    float xDist = (_player.xpos + _player.xRadius) - (_pf.xpos + _pf.radius) + 20;
    float yDist = (_player.ypos + _player.yRadius) - (_pf.ypos + _pf.radius) + 20; // add 20 because coding is dumb...


    // combined radii of player and platform
    float xRadiiCombined = _player.xRadius + _pf.radius + magicNumber;
    float yRadiiCombined = _player.yRadius + _pf.radius + magicNumber;

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
                }
                else
                {
                    _player.collisionFace = "right";
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

    
