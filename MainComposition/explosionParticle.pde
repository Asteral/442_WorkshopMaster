class ExplosionParticle {

  PVector location = new PVector(0, 0);
  PVector speed = new PVector(0, 0);
  float r;  

  ExplosionParticle(float x, float y, float sx, float sy, float radius) {
    location.x = x;
    location.y = y;
    r = radius;
    speed.x = sx;
    speed.y = sy;
  }


  void move() {
    location.add(speed);
  }

  void draw() {
    //ellipse(location.x,location.y,r,r);
  }
}

