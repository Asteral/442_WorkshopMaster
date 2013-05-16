class SSGen
// Template for starscape generation methods
{
  // Draw plane
  private PGraphics canvas;
  
  
  SSGen(PGraphics canvas)
  {
    this.canvas = canvas;
  }
  
  
  public PGraphics getCanvas()
  // Returns the draw canvas - useful for subclasses that cannot access private members
  {
    return(canvas);
  }
  // getCanvas
  
  
  public void render()
  // Render generated graphics onto the canvas
  // Assumes canvas is NOT currently drawable
  {
    getCanvas().beginDraw();
    // Do nothing, please extend
    getCanvas().endDraw();
  }
  // render
  
}
// SSGen
