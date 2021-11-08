class TextBox
{
    int xpos, ypos;
    int initxpos, initypos;
    PImage sprite;
    
    TextBox(PImage _sprite, int _xpos, int _ypos)
    {
        sprite = _sprite;
        _sprite.resize(width, height);
        xpos = _xpos;
        ypos = _ypos;
        initxpos = _xpos;
        initypos = ypos;
    }
    
    void display()
    {
        image(sprite, xpos, ypos);
    }
}

void loadTextBoxes()
{
    textboxTable = loadTable("csv/textboxPos.csv", "header");
    textBoxes = new TextBox[textboxTable.getRowCount()];

    int b = 0;
    for (TableRow row : textboxTable.rows())
    {
        PImage _sprite = loadImage(row.getString("sprite"));
        int _xpos = row.getInt("xpos")*gridSize;
        int _ypos = height - row.getInt("ypos")*gridSize;

        textBoxes[b] = new TextBox(_sprite, _xpos, _ypos);
        b++;
    }
    println("texboxes loaded");
}
