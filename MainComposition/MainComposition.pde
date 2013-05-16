AardvarkSim as;
ArrayList<PParticleSystem> antHells;
ArrayList<PVector> targets;

Explosion explosion;

void setup() {
  size(1024,768, JAVA2D);
  
  as = new AardvarkSim();
  as.setup();
  
  setupAnts();
}

void setupAnts()
{
  antHells = new ArrayList();
  targets = new ArrayList();
  
  PParticleSystem ants;
  PVector target;
  for(int i=0; i< as.breadsCount; ++i) {
    target = new PVector();
    targets.add(target);
    
    ants = new PParticleSystem();
    ants.addBehaviour(new ChaseTarget( target ));
    ants.addBehaviour(new Jitter());
    
    for(int j=0; j<15; ++j) {
      ants.addParticle(new PParticlePoint(new PVector(random(width), random(height), 0)));
    }
    antHells.add(ants);
  }
  
  AI bread = ((AI)as.breads.get(0));
  makeExplosion( bread.xPos, bread.yPos, 100, 10, 35);
}

void makeExplosion(float x, float y, int d, float r, float e){
  explosion = new Explosion(x,y,d,r,e);
  explosion.build();  // generates particles, only needed once
}

void runExplosion(){
  explosion.start(); // starts the very first explosion
}

void update() {
  as.update();
}


void draw() {
  background(255, 255, 255);
  as.draw();
  
  AI bread;
  PParticleSystem ants;
  PVector target;
  for(int i=0; i<as.breadsCount; ++i) {
    bread = ((AI)as.breads.get(i));
    target = (PVector)targets.get(i);
    
    target.x = bread.xPos + bread.cSize*random(-0.5, 0.5);
    target.y = bread.yPos + bread.cHSize*random(-0.5, 0.5);

    ants = (PParticleSystem)antHells.get(i);
    ants.updateAndDraw();
  }
  
  runExplosion();
  
}

void mousePressed()
{
  if(!explosion.on) 
    explosion.reset(mouseX, mouseY);
  explosion.on = true;
  
}
     
