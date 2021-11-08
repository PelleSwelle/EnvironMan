import processing.sound.*;

/*    UTIL    */
color backgroundColor = #6A6767;
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


void setup()
{
    /*    UTIL    */
    //fullScreen();
    size(1920, 1080);
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

void draw()
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

void die()
{
    println(enviroman.isDead);
    
    
}

void resetLevel()
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

void changeMusic(SoundFile _musicToPlay)
{
    currentlyPlaying.stop();
    currentlyPlaying = _musicToPlay;
    currentlyPlaying.loop();
    println(_musicToPlay);
}

void displayHitboxes()
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

void endScreen(PImage _ending)
{
    _ending.resize(width, height);
    image(_ending, 0, 0);
}

void keyPressed() // every time a key is pressed...
{
    keyHeldDown[keyCode] = true; // set that key to pressed in the keyHeldDown array

    if (keyPressed) 
    {
        if (key == 'k' || key == 'K') 
        {
            grabberSound.amp(0.5);
            grabberSound.play();
            //enviroman.grabber.reloadTimer.recharge();
        }
    }
}
void keyReleased() // once key is released...
{
    keyHeldDown[keyCode] = false; // set that key to released in the keyHeldDown array.
}
