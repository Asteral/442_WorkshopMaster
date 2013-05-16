import crayolon.portamod.*;

PortaMod musicTracker;
public NoteData incoming;

void setup(){
 size(1024, 1024);
 smooth(); 
 
 musicTracker = new PortaMod(this);
 musicTracker.doModLoad("syphus-oldendays.mod", true, 64);
  
}


void draw(){
  
  
}



void stop()
{
  musicTracker.stop();
  super.stop();
} 


//----------------------
//Music tracker callback
//----------------------
public void grabNewdata(PortaMod b) {        
  incoming = b.localnotes;
  /* Available from NoteData objects:
  channel, currentrealrow, currentrow, currentseq, effect,
  effparam, inst, note, seqlength, timestamp, vol
  */
    if(incoming.channel == 0) {
      println(incoming.currentrealrow + "   " + musicTracker.noteConvert(incoming.note));
    }
}
