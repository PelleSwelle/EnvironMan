class BackgroundLayer
{
    PImage image;
    int xpos, ypos;
    int layer;
    int initxpos;
    
    BackgroundLayer(PImage _image, int _xpos)
    {
        image = _image;
        image.resize(216*gridSize, height);
        xpos = _xpos;
        initxpos = xpos;
        ypos = 0;
    }
    
    void display()
    {
        image(image, xpos, ypos);
    }
}
