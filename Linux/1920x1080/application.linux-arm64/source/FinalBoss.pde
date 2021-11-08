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
    
    void move()
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
    
    void display()
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
