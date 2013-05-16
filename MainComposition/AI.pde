class AI{
  
  // Bread either avoids or follows for the Aardvark(s)
  float xPos;
  float yPos;
  PImage img;
  int speed;
  float cSize;
  float cHSize;
  int cState;
  
  AI(String imgName, int x, int y, int speed){
    this.img = loadImage(imgName);
    this.xPos = x;
    this.yPos = y;
    this.speed = speed;
    image(this.img,x,y);
    
    this.cSize = speed * 15.0;
    this.cHSize = this.cSize * 0.5;
    
    if (random(0, 1) < 0.5) {
      this.cState = 0;
    } else {
      this.cState = 1;
    }
  }
  
  float distFromArd(int aardvark_xPos, int aardvark_yPos) {
    return sqrt(pow(this.xPos - aardvark_xPos, 2) + pow(this.yPos - aardvark_yPos, 2)); 
  }
  
  PVector getDir(int aardvark_xPos, int aardvark_yPos) {
    float xDiff= aardvark_xPos - this.xPos;
    float yDiff= aardvark_yPos - this.yPos;
    float theta = atan2(xDiff, yDiff);
    
    float xStep = sin(theta) * speed;
    float yStep = cos(theta) * speed; 
    return new PVector(xStep, yStep);
  }
 
  void animate(int aardvark_xPos, int aardvark_yPos){
    PVector direction = getDir(aardvark_xPos, aardvark_yPos);
    if (cState != 0) {
      if (distFromArd(aardvark_xPos, aardvark_yPos) < 300) {       
        avoid(direction);
      }
    } else {
      if (distFromArd(aardvark_xPos, aardvark_yPos) > 50) {       
        follow(direction);
      }
    }
    
    image(this.img, this.xPos - cHSize, this.yPos - cHSize, cSize, cSize);
  }
  
  void follow(PVector dir) {
    this.xPos += dir.x * 0.3;
    this.yPos += dir.y * 0.3;
  }
  
  void avoid(PVector dir) {
    this.xPos -= dir.x * 0.3;
    this.yPos -= dir.y * 0.3;
  }
}
