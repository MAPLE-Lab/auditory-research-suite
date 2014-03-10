/*
 * Copyright (C) 2006-2007 University of Virginia Supported by grants to the
 * University of Virginia from the National Eye Institute and the National
 * Institute of Deafness and Communicative Disorders. PI: Prof. Michael
 * Kubovy <kubovy@virginia.edu>
 * 
 * Distributed under the terms of the GNU Lesser General Public License
 * (LGPL). See LICENSE.TXT that came with this file.
 * 
 * $Id$
 */
package edu.mcmaster.maplelab.rhythm;

import java.awt.*;
import java.io.File;
import java.net.URISyntaxException;
import java.util.logging.Level;

import javax.swing.*;

import edu.mcmaster.maplelab.common.Experiment;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.gui.*;
import edu.mcmaster.maplelab.rhythm.datamodel.*;

/**
 * Main container and configuration for the rhythm experiment.
 * @version   $Revision$
 * @author   <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since   Nov 7, 2006
 */
public class RhythmExperiment extends Experiment<RhythmSession> {
    public static final String EXPERIMENT_BASENAME = "Rhythm";

    public RhythmExperiment(RhythmSession session) {
        super(session);
        
        setPreferredSize(new Dimension(640, 480));
    }
    
    @Override
    protected void loadContent(JPanel contentCard) {
    	RhythmSession session = getSession();
        if (contentCard != null) {
            CardLayoutStepManager sMgr = new CardLayoutStepManager(contentCard);
            contentCard.add(new Introduction(sMgr, session), "intro");   
            if(session.getNumWarmupTrials() > 0) {
                contentCard.add(new PreWarmupInstructions(sMgr, session),  "prewarmup");
                contentCard.add(new StimulusResponseScreen(sMgr, session, true), "warmup");
            }   
            contentCard.add(new PreTrialsInstructions(sMgr, session),  "pretrials");            
            contentCard.add(new StimulusResponseScreen(sMgr, session, false), "test");            
            contentCard.add(new Completion(sMgr, session), "complete");            
            contentCard.add(new JLabel(), "Blank");
        }
    }
    
    /**
     * Default Soundbank in case user supplied soundbank does not work.
     * Should be located in Rhythm Directory
     * See: http://www.schristiancollins.com/generaluser.php
     */
    private static final String DEFAULT_SOUNDBANK = "GeneralUser.1.44.sf2";
    
    /**
     * Converts a string filename into a soundbank file. Loads default internal soundbank file
     * if the given filename fails to create a valid file. 
     * 
     * @param filename String representing soundbank file to attempt fetching.
     * @return File that contains a soundbank: either the one requested, or the default if that fails.
     */
    public static File getSoundbankFileFromString(String filename) {
    	File sbFile = new File(filename); // Load user specified soundbank file
    	if (sbFile == null || !sbFile.exists()) {
    		// Could not get user specified soundbank file
    		LogContext.getLogger().warning("User specified soundbank file is null or does not exist. " +
        			"Using default internal soundbank file.");
        	JOptionPane.showMessageDialog(null, "<html>No valid soundbank file given. " +
        			"<br>Using default internal soundbank file: <br>" + DEFAULT_SOUNDBANK + "</html>",
        			"Using Default Properties",
        			JOptionPane.INFORMATION_MESSAGE);
        	try {
				sbFile = new File(RhythmExperiment.class.getResource(DEFAULT_SOUNDBANK).toURI());
			} catch (URISyntaxException e) {
				e.printStackTrace();
				sbFile = null;
			}
    	}
    	
    	return sbFile;
    }
    
    
    /**
     * @param args ignored
     */
    public static void main(String[] args) {
    	System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Rhythm Experiment");
    	try {
    		// GUI initialization must be done in the EDT
    		EventQueue.invokeAndWait(new Runnable() {

    			@Override
    			public void run() {

    				final RhythmSetupScreen setup = new RhythmSetupScreen();
    				setup.display();

    				RhythmFrame f = new RhythmFrame(setup);
    				f.setTitle(String.format("Rhythm Experiment - Build %s", RhythmExperiment.getBuildVersion()));
    				f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    				f.setLocationRelativeTo(null);
    				f.pack();
    				f.setVisible(true);
    			}
    		});
    		LogContext.getLogger().finer("Experiment GUI launched");
    	}
        catch (Exception ex) {
            ex.printStackTrace();
            LogContext.getLogger().log(Level.SEVERE, "Unrecoverable error", ex);
        }
    }
}
