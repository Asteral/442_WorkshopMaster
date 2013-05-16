import java.util.*;

class SSGenStars extends SSGen
{
  private ArrayList features;
  private PVector viewpointOffset;
  
  // Setup canvas bounds
  PVector lowBound;
  PVector highBound;
  
  private int STELLAR_POPULATION = 16384;
  private float STAR_STEP = 0.25;
  private float MAX_FEATURE_SIZE = 64;
  

  SSGenStars(PGraphics canvas)
  {
    super(canvas);
    
    // Setup data
    features = new ArrayList();
    viewpointOffset = new PVector(0,0,0);
    
    // Setup canvas bounds
    lowBound = new PVector( getCanvas().width * -1.0,  getCanvas().height * -1.0,  0 );
    highBound = new PVector( getCanvas().width * 2.0,  getCanvas().height * 2.0,  getCanvas().height * 3 );
    
    // Generate and subdivide features
    genDrunkStars(STELLAR_POPULATION);
    features = breakdownFeatures(features);
    features = quickSortFeatures(features);
  }


  public void render()
  // Draw some stars
  {
    getCanvas().beginDraw();
    
    // Render astronomical features
    renderFeatures();
    
    getCanvas().endDraw();
  }
  // render
  
  
  public void moveView(PVector dpos)
  // Change visible offset
  {
    viewpointOffset.add(dpos);
  }
  // moveView
  
  
  /*
  
  Devise galactic L-system
  
  We don't need an entire galaxy. We just want to be positioned within a starfield.
  
  This means 3-dimensional space. And most of it will probably be behind the viewer, oh well.
  
  Let's use a drunken walker to deposit stellar clusters for now.
  
  */
  
  
  private void genDrunkStars(int pop)
  // Simple looped drunk-walk star drawist
  {
    // Setup canvas-relative feature scalars
    float featureScale = getCanvas().height / 128.0;
    float stepMax = getCanvas().width * STAR_STEP;
    
    // Setup positional data
    float x = getCanvas().width / 2.0;
    float y = getCanvas().height / 2.0;
    float z = 0;
    PVector pos = new PVector(x, y, z);
    PVector posLast = pos.get();
    
    // Setup lifecycle data
    float lifeCycleStep = 0.1;
    
    // Do walk
    for(int i = 0;  i < pop;  i++)
    {
      // Record history
      posLast = pos.get();
      
      // Step in a random direction
      float step = random(stepMax) * pow(random(1.0), 4);
      PVector dir = PVector.random3D();
      dir.mult(step);
      pos.add(dir);
      x = pos.x;
      y = pos.y;
      z = pos.z;
      
      // Constrain to texture boundaries
      if( x < lowBound.x )      x = width;
      if( y < lowBound.y )      y = height;
      if( z < lowBound.z )      z = height;  // No natural z boundary exists
      if( highBound.x < x )  x = 0;
      if( highBound.y < y ) y = 0;
      if( highBound.z < z ) z = 0;       // Again, no natural z boundary exists
      pos.set(x,y,z);
      
      // Determine visible radius
      float zNorm = z / highBound.z;
      float rDist = pow(zNorm, 2);
      float randomAbsoluteMagnitude = random(featureScale) * pow(random(1.5), 2);
      float r = rDist * randomAbsoluteMagnitude;
      
      // Determine lifecycle state
      float lifeCycle = noise( i * lifeCycleStep,  lifeCycleStep * step / stepMax) * 2.0 - 1.0;
      lifeCycle += random(-1,1) * pow(random(1), 3) * 1.0;
      
      // Cache data
      AstroFeature af = new AstroFeature(pos.get(), posLast.get(), lifeCycle, r, 1.0);
      features.add(af);
      
      /*
      // Draw connection lines
      getCanvas().stroke(255,0,0,64);
      getCanvas().line(pos.x, pos.y,  posLast.x, posLast.y);
      getCanvas().noStroke();
      */
    }
  }
  // genDrunkStars
  
  
  
  
  
  private void renderFeatures()
  // Renders the features to the canvas
  {
    Iterator i = features.iterator();
    while( i.hasNext() )
    {
      AstroFeature af = (AstroFeature) i.next();
      af.render();
    }
  }
  // renderFeatures()
  
  
  
  
  
  private ArrayList quickSortFeatures(ArrayList list)
  // Sorts features by depth
  {
    // Cease recursion on empty or unitary list
    if( list.size() <= 1 )
      return list;
    
    // Determine pivot
    int pivotIndex = floor( list.size() / 2 );
    AstroFeature pivot = (AstroFeature) list.get(pivotIndex);
    list.remove(pivotIndex);
    
    // Create high/low lists
    ArrayList low = new ArrayList();
    ArrayList high = new ArrayList();
    
    // Sort into high/low
    Iterator i = list.iterator();
    while( i.hasNext() )
    {
      AstroFeature af = (AstroFeature) i.next();
      if(af.m_pos.z < pivot.m_pos.z)
        low.add(af);
      else
        high.add(af);
    }
    
    // Recurse lists
    low = quickSortFeatures(low);
    high = quickSortFeatures(high);
    
    // Concatenate lists
    ArrayList list2 = new ArrayList();
    list2.addAll(low);
    list2.add(pivot);
    list2.addAll(high);
    
    // Complete and return sorted list
    return( list2 );
  }
  // quickSortFeatures
  
  
  
  
  
  private ArrayList breakdownFeatures(ArrayList list)
  // Converts all large features into smaller, more complex ones
  {
    // Create return list
    ArrayList list2 = new ArrayList();
    
    // Completion check
    boolean tookAction = false;
    
    // Build return list
    Iterator i = list.iterator();
    while( i.hasNext() )
    {
      AstroFeature af = (AstroFeature) i.next();
      
      if(af.needsBreakdown(MAX_FEATURE_SIZE))
      {
        // Too large, break it down
        list2.addAll(af.breakdown());
        tookAction = true;
      }
      else
      {
        // Leave it alone
        list2.add(af);
      }
    }
    
    // Recursion
    if(tookAction)
    {
      // Breakdowns were used, therefore more might be needed
      list2 = breakdownFeatures(list2);
    }
    
    // Return new list
    return(list2);
  }
  // breakdownFeatures
  
  
  
  
  
  class AstroFeature
  // Catalogues an astronomical feature for drawing
  {
    PVector m_pos, m_lastPos;
    float m_lifeCycle, m_magnitude, m_density;
    
    float NEB_SCALE = 128;
    int NEB_POP = 32;
    float DUST_THRESH = -0.75;
    
    AstroFeature(PVector pos, PVector lastPos, float lifeCycle, float magnitude, float density)
    {
      m_pos = pos;
      m_lastPos = lastPos;
      m_lifeCycle = lifeCycle;
      m_magnitude = magnitude;
      m_density = density;
    }
    
    
    public void render()
    // Draw to the canvas
    {
      // Apparent size
      float r = m_magnitude;
      
      // Offset
      float zNorm = m_pos.z / highBound.z;
      getCanvas().pushMatrix();
      getCanvas().translate(viewpointOffset.x * zNorm, viewpointOffset.y * zNorm);
      
      // Lifecycle analysis
      
      
      if(m_lifeCycle < 0)
      {
        if(m_lifeCycle < -0.75)
        {
          // Dust cloud
          getCanvas().noStroke();
          float a = 32 * m_density * (1.5 - zNorm);
          getCanvas().fill(0, a);
          r *= NEB_SCALE;
        }
        else
        {
          // Nebula threshold: -1 to 0
          getCanvas().colorMode(HSB);
          getCanvas().noStroke();
          float h = 255 - 255 * abs(m_lifeCycle * 2);
          float a = 4 * m_density * (1.5 - zNorm);
          getCanvas().fill(h,  255, 255, a);
          r *= NEB_SCALE;
        }
        
        // Draw feature
        getCanvas().ellipse(m_pos.x, m_pos.y, r, r);
      }
      
      else
      {
        // Stellar threshold
        getCanvas().pushStyle();
        getCanvas().colorMode(RGB);
        getCanvas().noStroke();
        
        // Draw feature
        drawStar(m_pos.x, m_pos.y, r);
        
        getCanvas().popStyle();
      }
      
      
      // Terminate offset
      getCanvas().popMatrix();
    }
    // render
    
    
    private void drawStar(float x, float y, float r)
    // Draw a star
    {
      getCanvas().pushMatrix();
      
      // Transform
      getCanvas().translate(x, y);
      //getCanvas().scale(r);
      
      // Derive sequence brilliance
      float starR = map(m_lifeCycle, 0, 1, 255, 192);
      float starG = 192;
      float starB = map(m_lifeCycle, 0, 1, 192, 255);
      
      // Draw fancy
      // Draw core
      getCanvas().fill(255,255);
      getCanvas().ellipse(0,0,r,r);
      // Draw glow
      float grades = 8.0;
      getCanvas().fill(starR, starG, starB, 32.0 / grades);
      for(int i = 1;  i <= grades;  i++)
      {
        float auraR = (1.0 + i / grades) * 2 * r;
        getCanvas().ellipse(0,0,auraR,auraR);
      }
      // Draw spikes
      getCanvas().stroke(starR, starG, starB, 24);
      for(int i = 0;  i < 3;  i++)
      {
        getCanvas().line(0,3*r, 0,-3*r);
        getCanvas().rotate(TWO_PI / 3);
      }
      // Draw lens thingy
      getCanvas().noFill();
      getCanvas().ellipse(0, 0, r * 2, r * 2);
      
      
      getCanvas().popMatrix();
    }
    // drawStar
    
    
    public boolean needsBreakdown(float maxSize)
    // Returns "true" if the feature could be broken down into smaller features
    {
      if(0 <= m_lifeCycle)
      {
        // Is a star
        return(false);
      }
      // Is a nebula
      float r = m_magnitude * NEB_SCALE;
      if(r < maxSize)
      {
        // Visible radius is below threshold
        return(false);
      }
      // Visible radius is above threshold
      return true;
    }
    // needsBreakdown
    
    
    public ArrayList breakdown()
    // Returns a breakdown of this feature transformed into a range of subfeatures
    {
      ArrayList list = new ArrayList();
      
      /*
      if(m_lifeCycle < DUST_THRESH)
      {
        // It's dust, make it linear
        int newPop = floor( random(NEB_POP) );
        for(int i = 0;  i < newPop;  i++)
        {
          // Determine position along line
          
          // Determine line magnitude
          
          // Deviate from line
        }
      }
      
      else
      */
      {
        // It's nebular material
        
        // Determine some parameters
        float shellSymmetry = random(1, 2);  // How regular the sphere of the subfeatures is
        float baseOffset = m_magnitude * NEB_SCALE * 0.15;
        
        // Compose new feature list
        int newPop = floor( random(NEB_POP) );
        for(int i = 0;  i < newPop;  i++)
        {
          // Determine core position
          PVector newPos = m_pos.get();
          
          // Determine offset direction
          PVector dir = PVector.random3D();
          
          // Determine offset magnitude
          float offsetVariance = (shellSymmetry  *  (noise(dir.x + m_pos.x, dir.y + m_pos.y, dir.z + m_pos.z)  -  0.5)) + 1;
          float offset = baseOffset * offsetVariance;
          dir.mult(offset);
          
          // Apply offset
          newPos.add(dir);
          
          // Compute altered lifecycle parameters based on deviation from expected shell position
          //float dLife = (offsetVariance - 1) * 1.0;
          float dLife = (noise(newPos.x, newPos.y, newPos.z) - 0.5) * 0.2;
          
          // Determine new density
          float newDensity = m_density * pow( random(0.5,1.0),  0.5 );
          
          // Create new feature
          float r = m_magnitude * newDensity / pow(newPop, 1.0 / 2.0);  // Conserve visible area
          float newLife = m_lifeCycle + dLife;
          AstroFeature af = new AstroFeature(newPos, m_lastPos, newLife, r, newDensity);
          list.add(af);
        }
        
      }
      
      // Complete and return new feature list
      return( list );
    }
    // breakdown
  }
  // AstroFeature

}
// SSGenStars

