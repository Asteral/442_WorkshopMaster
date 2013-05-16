class AardvarkSim {

  AE_AnimationEngineManager animation_manager;
  ArrayList breads;
  int breadsCount = 13;

  void setup() {
    
    this.animation_manager = new AE_AnimationEngineManager("aardvark.png", 200, 200);
   
    this.breads = new ArrayList();
    for (int i = 0; i < breadsCount; i++) {
       this.breads.add(new AI("bread.png", (int) random(0, width), (int) random(height), (int) random(3, 6)));
    } 
  }
  
  void update() {
    
  }
  
  void draw() {
    for (int i = 0; i < this.breadsCount; i++) {
       AI bread = (AI) this.breads.get(i); 
       
       bread.animate((int) this.animation_manager.pos.x, (int) this.animation_manager.pos.y);
       if (random(0, 1) < 0.003) {
         bread.cState = (bread.cState == 1 ? 0 : 1);
       }
    }
   
    this.animation_manager.animate(); 
  }
}
