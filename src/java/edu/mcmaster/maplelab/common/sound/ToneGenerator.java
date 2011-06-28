/*
* Copyright (C) 2006-2007 University of Virginia
* Supported by grants to the University of Virginia from the National Eye Institute 
* and the National Institute of Deafness and Communicative Disorders.
* PI: Prof. Michael Kubovy <kubovy@virginia.edu>
*
* Distributed under the terms of the GNU Lesser General Public License
* (LGPL). See LICENSE.TXT that came with this file.
*
* $Id$
*/
package edu.mcmaster.maplelab.common.sound;

import java.util.*;
import java.util.logging.Level;

import javax.sound.midi.*;

import edu.mcmaster.maplelab.common.LogContext;


/**
 * Encapsulation of device used to generating tones from Pitch Stimulus.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Apr 17, 2006
 */
public class ToneGenerator {
	private static final int GENERATOR_CHANNEL = 0;
    private static final int MIDI_MAX_VOLUME = 127;
    private static final int MIDI_NOTE_VELOCITY_CMD = 64;
    private static final int PITCH_BEND_MAX = 16383;
    private static final int PITCH_BEND_NONE = 8192;
    
    private static final int MIDI_VOLUME_CMD = 7;
    private static final short MIDI_BANK_MSB_FLUTE = 0;
    private static final short MIDI_BANK_LSB_FLUTE = 74;

    
    /** Number of pitch bend units per cent. Although not guaranteed, the 
     * General midi spec states that a full pitch bend should be +/- two 
     * semitones. This has been verified with an external tuner. */
    private static final float PITCH_BEND_PER_CENTS = (PITCH_BEND_MAX - PITCH_BEND_NONE)/200f;
    
    // Constants for computing and managing timing
    private static final int TICKS_PER_MILLISECOND = 1;
    public static final int TEMPO_BEATS_PER_MINUTE = 60;
    private static final int TICKS_PER_QNOTE = 1000;
    
    private static ToneGenerator _singleton = null;
    public static ToneGenerator getInstance() throws MidiUnavailableException {
        if(_singleton == null) {
            _singleton = new ToneGenerator();
        }
        return _singleton;
    }
    
    private Sequencer _sequencer;
    private SequenceLatch _latch;
    private short _midiBankLSB = MIDI_BANK_LSB_FLUTE;
    private short _midiBankMSB = MIDI_BANK_MSB_FLUTE;
    
    private Map<Track, Integer> _lastDetunes = new  HashMap<Track, Integer>();
    
    private ToneGenerator() throws MidiUnavailableException {
        // The default sequencer should already be attached to the default synth.
        _sequencer = MidiSystem.getSequencer();
        _sequencer.addMetaEventListener(_latch = new SequenceLatch());
        _sequencer.open();
    }
    
    protected void finalize() throws Throwable {
        super.finalize();
        try {
            _sequencer.close();
        } 
        catch (Exception e) {
        }
    }
    
    /**
     * Get the sequencer used by this.
     */
    public Sequencer getSequencer() {
        return _sequencer;
    }
    
    /**
     * Set the midi sound.
     * 
     * @param msb
     * @param lsb
     */
    public void setMidiBank(short msb, short lsb) {
        _midiBankMSB = msb;
        _midiBankLSB = lsb;
    }
    
    /**
     * Playback a single note. Blocks until playback finishes.
     * 
     * @param note note to play
     */
    public void play(Note note, float volumePct) {
        List<Note> singleNote = Collections.singletonList(note);
        play(singleNote, volumePct);
    }
    
    /**
     * Create the default sequence configuration.
     */
    private static Sequence defaultSequence() throws InvalidMidiDataException {
        return new Sequence(Sequence.PPQ, TICKS_PER_QNOTE); 
    }
    
    /**
     * Play a series of notes on the default sequencer. Blocks until
     * sequence playback finishes.
     * 
     * @param notes notes to play.
     */
    public void play(List<Note> notes, float volumePct) {
        play(notes, volumePct, true);
    }
    
    /**
     * Play a series of notes on the default sequencer. Blocks until
     * sequence playback finishes, if indicated.
     * 
     * @param notes notes to play.
     */
    public Sequence play(List<Note> notes, float volumePct, boolean block) {
        Sequence retval = null;
        try {
            _lastDetunes.clear();
            retval = defaultSequence();
            Track track = retval.createTrack();
            prepareTrack(track, volumePct, _midiBankLSB, _midiBankMSB);
            
            int cumulativeDuration = 0;
            for (Note n : notes) {
                int noteDuration = n.getDuration();
                
                Pitch ps = n.getPitch();
                if(ps != null) {
                
                    int midiNote = ps.toMidiNoteNumber();
                    int detune = ps.getDetuneCents();
                    
                    long ticks = toTicks(cumulativeDuration);
                    MidiEvent detuneEvent = createDetuneEvent(track, detune, ticks);
                    if(detuneEvent != null) {
                        track.add(detuneEvent);
                    }
                    track.add(createNoteOnEvent(midiNote, ticks));
                    track.add(createNoteOffEvent(midiNote, toTicks(cumulativeDuration+noteDuration)));
                    
                }
                cumulativeDuration += noteDuration;
                
            }
            _sequencer.setSequence(retval);
            setTempo(_sequencer, TEMPO_BEATS_PER_MINUTE);
         
            long milli = retval.getMicrosecondLength()/1000;
            _sequencer.startRecording();
            
            if(block) {
                synchronized (_latch) {
                    // Wait for the sequence to end
                    _latch.wait(milli*4);
                    _sequencer.stop();
                }
            }
        } 
        catch (Exception ex) {
            LogContext.getLogger().log(Level.WARNING, "Unexpected MIDI error", ex);
            MidiUnavailableException wrapper = new MidiUnavailableException("Unexpected MIDI error");
            wrapper.initCause(ex);
        }
        return retval;
    }
    
//    private MetaMessage createTempoEvent() {
//        byte b1, b2, b3;
//        MetaMessage retval = new MetaMessage();
//        byte[] date = new byte[] { 0xff, 0x51, 0x03, b1, b2, b3 };
//        retval.setMessage(i, abyte0, j)
//    }
    
    private void setTempo(Sequencer sequencer, int tempoBPM) {
        // Per: http://www.jsresources.org/faq_midi.html#tempo_methods
        float current = sequencer.getTempoInBPM();
        float factor = tempoBPM / current;
        sequencer.setTempoFactor(factor);
    }

    /**
     * Compute the number of sequencer ticks.
     * 
     * @param milliseconds time in milliseconds
     * @return time in ticks.
     */
    public static long toTicks(long milliseconds) {
        return milliseconds * TICKS_PER_MILLISECOND;
    }

    /**
     * Create the event to start the note playback.
     * @param note midi note
     * @return note event
     */
    public static MidiEvent createNoteOnEvent(int note, long ticks) throws InvalidMidiDataException {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_ON, GENERATOR_CHANNEL, note, MIDI_NOTE_VELOCITY_CMD);
        return new MidiEvent(msg, ticks);
    }

    /**
     * Create event to turn  note off.
     * 
     * @param note note to turn off.
     */
    public static MidiEvent createNoteOffEvent(int note, long ticks) throws InvalidMidiDataException {
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.NOTE_OFF, GENERATOR_CHANNEL, note, 0);
        
        return new MidiEvent(msg, ticks);        
    }

    /**
     * Create the event to adjust the tuning of the note playback following this event.
     * @param track 
     * 
     * @param detuneCents amount to adjust in cents, where 100 cents = semitone.
     * @return detune event
     */
    private MidiEvent createDetuneEvent(Track track, int detuneCents, long ticks) throws InvalidMidiDataException {
        // Apply any detune via the pitch bend controller.
        Integer lastDetune = _lastDetunes.get(track);
        if(lastDetune != null && lastDetune.intValue() == detuneCents) {
            return null;
        }
        
        _lastDetunes.put(track, detuneCents);

        ShortMessage msg = new ShortMessage();
        int amount = (int)(PITCH_BEND_NONE + PITCH_BEND_PER_CENTS * detuneCents);
        msg.setMessage(ShortMessage.PITCH_BEND, GENERATOR_CHANNEL, amount % 128, amount / 128);
        
        return new MidiEvent(msg, ticks);
    }
    
    /**
     * Prepare the track for tone playback.
     * 
     * @param track track to populate with setup events.
     */
    public static void prepareTrack(Track track, float volumePct, 
    		short midiBankLSB, short midiBankMSB) throws InvalidMidiDataException {
    	
    	for (MidiEvent me : initializationEvents(volumePct, midiBankLSB, midiBankMSB)) {
    		track.add(me);
    	}
    }
    
    public static MidiEvent[] initializationEvents(float volumePct, short midiBankLSB, 
    		short midiBankMSB) throws InvalidMidiDataException {
    	
    	MidiEvent[] retval = new MidiEvent[2];
    	ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.CONTROL_CHANGE, 0, MIDI_VOLUME_CMD, 
        		(int) (volumePct*MIDI_MAX_VOLUME));
        retval[0] = new MidiEvent(msg, 0);
        
        msg = new ShortMessage();
        msg.setMessage(ShortMessage.PROGRAM_CHANGE, 0, midiBankLSB, midiBankMSB);
        retval[1] = new MidiEvent(msg, 0);
        
        return retval;
    }
    
    private class SequenceLatch implements MetaEventListener {
        private static final int MIDI_TRACK_END = 47;
        public void meta(MetaMessage meta) {
            // Check for end of track message.
            if(meta.getType() == MIDI_TRACK_END) {
                synchronized(this) {
                    notify();
                }
            }
        }
    }
    
    /**
     * Debugging code for inspecting sequence.
     */
    public static String toDebugString(Sequencer sequencer) {
        StringBuffer buf = new StringBuffer();
        
        buf.append("Sequencer:\n");
        buf.append("\ttempo factor = " + sequencer.getTempoFactor() + "\n");
        buf.append("\ttempo BPM = " + sequencer.getTempoInBPM() + "\n");
        buf.append("\ttempo MPQ = " + sequencer.getTempoInMPQ() + "\n");
        buf.append("\ttick length= " + sequencer.getTickLength() + "\n");
        buf.append("\ttick position = " + sequencer.getTickPosition() + "\n");
        buf.append("\tmicrosecond length = " + sequencer.getMicrosecondLength() + "\n");
        buf.append("\tmicrosecond position = " + sequencer.getMicrosecondPosition() + "\n");
        buf.append("\tmaster sync mode = " + sequencer.getMasterSyncMode() + "\n");
        
        return buf.toString();
    }
    
    
    /**
     * Test code.
     * @param args ignored
     */
    public static void main(String[] args) {
        try {
            ToneGenerator tg = new ToneGenerator();
            
            Note[] notes = new Note[] {
                new Note(new Pitch(NotesEnum.C, 4), 2000),
                new Note(null, 250),
                new Note(new Pitch(NotesEnum.C, 4, -20), 2000),
                new Note(new Pitch(NotesEnum.C, 4, 20), 2000),
                new Note(new Pitch(NotesEnum.C, 4), 2000),
            };
            
            tg.play(Arrays.asList(notes), 1.0f);
        }
        catch(Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
        System.exit(0);
    }
}
