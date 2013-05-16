class AE_AnimationEngineManager{
// This class animates an image
  PVector pos;
  PImage img;
  int dir;

  AE_AnimationEngineManager(String imgName, int x, int y){
    this.img = loadImage(imgName);
    this.pos = new PVector(x, y);
    image(this.img,x,y);
  }
 
  void animate(){
    
    this.pos.x = sin(frameCount * 0.01) * width * 0.4 + width * 0.5;
    this.pos.y = noise((frameCount + 600) * 0.1) * 50.0 + 400; 
    image(this.img, this.pos.x - 100, this.pos.y - 100);
  }
}
