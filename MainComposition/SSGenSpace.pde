class SSGenSpace extends SSGen
// Space background generator (cosmic microwave background)
{
  color bgCol;
  
  SSGenSpace(PGraphics canvas)
  {
    super(canvas);
    
    bgCol = color(random(32), random(32), random(32));
  }
  
  
  public void render()
  // Override default method
  {
    getCanvas().beginDraw();
    getCanvas().background(bgCol);
    getCanvas().endDraw();
  }
  // render
  
}
// SSGenSpace
