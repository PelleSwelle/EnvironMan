class Timer
{
    int targetTime;
    int elapsedTime;
    
    int cycle;
    int noOfCycles = 2;
    
    Timer(int _targetTime)
    {
        cycle = 1;
        targetTime = _targetTime;
    }
    
    void count()
    {
        elapsedTime += 1;
        if (elapsedTime >= targetTime)
        {
            reset();
        }
    }
    
    void displayTimer()
    {
        text("cycle: " + cycle + "  elapsedTime:  " + elapsedTime, width - 500, 200);
    }
    void zeroOut()
    {
        elapsedTime = 0;
    }

    void reset() // only used for enemies
    {
        if (cycle == noOfCycles)
        {
            cycle = 1;
        }
        else
        {
            cycle ++;
        }
        elapsedTime = 0;
    }
}
