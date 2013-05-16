AardvarkSim as;

void setup() {
  size(1024,768, JAVA2D);
  
  
  as = new AardvarkSim();
  as.setup();
}

void update() {
  as.update();
}


void draw() {
  background(255, 255, 255);
  as.draw();
}
     
