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
    
    void display()
    {
        text(enviroman.grabber.reloadTimer.elapsedTime, xpos, ypos);    
        if (enviroman.grabber.reloading)
        {
            
        }
    }
}
