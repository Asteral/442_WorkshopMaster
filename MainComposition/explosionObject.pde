class Explosion {

  ExplosionParticle particle;

  ArrayList particles; 
  float xpos;
  float ypos;
  float density;
  float radius;
  float energy;
  float tension;
  float energyinit;

  boolean allow;
  boolean build;

  boolean on;

  Explosion(float x, float y, int d, float r, float e) {

    particles = new ArrayList();
    xpos=x;
    ypos=y;
    density=d;
    radius=r;
    energy=e;
    energyinit = energy;

    build = true;
    allow = false;

    on = false;
  }
  void build() {

    if (build == true) {

      for (int i = 0 ; i < density; i++) {
        float ix = random(radius) * cos(radians(random(0, 360)));
        float iy = random(radius) * sin(radians(random(0, 360)));

        particles.add(new ExplosionParticle(ix+xpos, iy+ypos, 0, 0, 15));
      }

      //for (int i; i<density+1 ; i++){
      for (int i = 0 ; i < particles.size()-1; i++) {
        ExplosionParticle particle = (ExplosionParticle) particles.get(i);
        particle.draw();

        build = false;
        allow = true;
      }
    }
  }

  void start() {
    if (on) {
      if (allow == true) {   

        for (int i = 0 ; i < particles.size()-1; i++) {
          ExplosionParticle particle = (ExplosionParticle) particles.get(i);
          int linelim = 0;

          for (int k = 0 ; k < particles.size()-1; k++) {
            ExplosionParticle particle2 = (ExplosionParticle) particles.get(k);

            //if ( dist(particle.location.x,particle.location.y,particle2.location.x,particle2.location.y) < 10){

            particle.speed.x =  particle.location.x - particle2.location.x;
            particle.speed.y =  particle.location.y - particle2.location.y;
            //}

            float dist = dist(particle.location.x, particle.location.y, particle2.location.x, particle2.location.y);

            if ( dist < 100 && linelim <= 1 ) {

              stroke(204, 102, 0, abs(map(energy, 0, energyinit, 0, 255))*0.3);
              strokeWeight(abs(map(energy, 0, energyinit, 0, 40)));
              line(particle.location.x, particle.location.y, particle2.location.x, particle2.location.y);
              strokeWeight(1);
              stroke(1);
              linelim++;
            }
          }
          particle.speed.normalize();
          randomSeed(i);
          particle.speed.mult(random (1)* energy/2);
          particle.move();
          particle.draw();

          particle.speed.x = 0;
          particle.speed.y = 0;
        }

        energy -- ;

        if (energy == 0) {
          allow = false;
          on = false;
        }
      }
    }
  }

  void reset(float x, float y) {
    for (int i = 0 ; i < particles.size()-1; i++) {

      xpos = x;
      ypos = y;

      float ix = random(radius) * cos(radians(random(0, 360)));
      float iy = random(radius) * sin(radians(random(0, 360)));  

      ExplosionParticle particle = (ExplosionParticle) particles.get(i);
      particle.location.x = ix+xpos;
      particle.location.y = iy+ypos;
      particle.draw();

      energy = energyinit;
    }

    build = false;
    allow = true;
    
  }
}

