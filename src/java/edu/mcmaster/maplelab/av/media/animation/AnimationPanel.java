/*
 * Copyright (C) 2011 McMaster University PI: Dr. Michael Schutz
 * <schutz@mcmaster.ca>
 * 
 * Distributed under the terms of the GNU Lesser General Public License (LGPL).
 * See LICENSE.TXT that came with this file.
 */
package edu.mcmaster.maplelab.av.media.animation;

import java.awt.*;
import java.io.File;

import javax.media.opengl.*;
import javax.media.opengl.awt.GLCanvas;
import javax.swing.*;

import net.miginfocom.swing.MigLayout;

import com.jogamp.opengl.util.Animator;

import edu.mcmaster.maplelab.av.media.Playable;
import edu.mcmaster.maplelab.av.media.SoundClip;
import edu.mcmaster.maplelab.common.LogContext;
import edu.mcmaster.maplelab.common.datamodel.DurationEnum;
import edu.mcmaster.maplelab.common.sound.NotesEnum;

/**
 * Creates a panel to be used for animation rendering.
 * @author Catherine Elder <cje@datamininglab.com>
 */
public class AnimationPanel extends JPanel {
	private final GLCanvas _canvas;
	private GLEventListener _renderer;
	private final Animator _defaultTrigger;
	private final AnimationTrigger _altTrigger;
    
    static {
        // Put JOGL version information into system properties to 
        // assist in debugging.
        Package joglPackage = Package.getPackage("javax.media.opengl");
        System.setProperty("jogl.specification.version", joglPackage.getSpecificationVersion());
        System.setProperty("jogl.implementation.version", joglPackage.getImplementationVersion());
        
        ToolTipManager.sharedInstance().setLightWeightPopupEnabled(false);
    }
    
    public AnimationPanel(GLEventListener renderer) {
    	this(renderer, null, null);
    }
    
    public AnimationPanel(GLEventListener renderer, AnimationTrigger trigger) {
    	this(renderer, trigger, null);
    }
    
    public AnimationPanel(GLEventListener renderer, Dimension dim) {
    	this(renderer, null, dim);
    }
    
    public AnimationPanel(GLEventListener renderer, AnimationTrigger trigger, Dimension dim) {
    	super(new MigLayout("insets 0, nogrid, center, fill", "center", "center"));
    	
        GLCapabilities caps = new GLCapabilities(GLProfile.get(GLProfile.GL2));

        _canvas = new GLCanvas(caps);
        _canvas.setName("glCanvas");
    	_canvas.setMinimumSize(new Dimension(5, 5));
        setRenderer(renderer);
        
        if (trigger != null) {
        	_defaultTrigger = null;
        	_altTrigger = trigger;
        	_altTrigger.setCanvas(_canvas);
        }
        else {
        	_altTrigger = null;
        	_defaultTrigger = new Animator(_canvas);
            _defaultTrigger.setPrintExceptions(true);
        }
 		
        add(_canvas);
        setSize(dim);
    }
    
    public void setSize(Dimension dim) {
    	_canvas.setPreferredSize(dim != null ? dim : new Dimension(640, 480));
    }
    
    public void setSize(int width, int height) {
    	_canvas.setPreferredSize(new Dimension(width, height));
    }
    
    /**
     * Set the renderer to provide animation.
     */
    public void setRenderer(GLEventListener renderer) {
    	if (_renderer != null) _canvas.removeGLEventListener(_renderer);
    	_renderer = renderer;
    	if (_renderer != null) _canvas.addGLEventListener(_renderer);
    }
    
    /**
     * Start processing animation events.
     */
    public synchronized void start() {
    	if (_defaultTrigger != null) _defaultTrigger.start();
    	else _altTrigger.start();
    }
    
    /**
     * Stop processing animation events.
     */
    public synchronized void stop() {
    	if (_defaultTrigger != null) _defaultTrigger.stop();
    	else _altTrigger.stop();
    }
    
    /**
     * Set the size of the animation.
     */
    public void setAnimationSize(Dimension dim) {
    	_canvas.setPreferredSize(dim);
    }
    
    /**
     * {@inheritDoc} 
     * @see java.awt.Component#setCursor(java.awt.Cursor)
     */
    @Override
    public void setCursor(Cursor cursor) {
        super.setCursor(cursor);
        _canvas.setCursor(cursor);
    }

    /**
     * Attempt to make errors generated when 3D not available a little nicer.
     * {@inheritDoc} 
     * @see java.awt.Component#addNotify()
     */
    @Override
    public void addNotify() {
        try {
            super.addNotify();
        }
        catch (UnsatisfiedLinkError ex) {
        	LogContext.getLogger().warning("Sorry, 3D model rendering has not been enabled " +
        			"for this platform.");
        	ex.printStackTrace();
        }
    } 
    
    /**
     * Example panel. Animate an example file (E short) and play a tone (D long).
     */
    public static void main(String[] args) {
        try {
            JFrame f = new JFrame(AnimationPanel.class.getName());
            f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            AnimationRenderer ani = new AnimationRenderer();
            
            final AnimationPanel view = new AnimationPanel(ani);

            f.getContentPane().add(view, BorderLayout.CENTER);
            f.pack();
            f.setLocationRelativeTo(null);
            f.setVisible(true);
            
    		File file = new File("datafiles/examples/vis/es_.txt");
    		
			final AnimationSequence animation = AnimationParser.parseFile(file);
			
			String filename = NotesEnum.D.toString().toLowerCase() + "_" +  DurationEnum.LONG.codeString() + ".wav";
			//System.out.printf("sound clip filename = %s\n", filename);

			File dir = new File("datafiles/examples/aud");

			final Playable audio = SoundClip.findPlayable(filename, dir, false);
			
			Runnable r = new Runnable() {
				@Override
				public void run() {
					audio.play();
				}
			};

    		long currentTime = System.nanoTime();
            ani.setAnimationSource(new AnimationSource() {
            	public AnimationSequence getAnimationSequence() {
            		return animation;
            	}
            	public int getNumPoints() {
            		return 5;
            	}
            	public float getDiskRadius() {
            		return 0.3f;
            	}
            	public boolean isConnected() {
            		return true;
            	}
            });
               
    		ani.setNanoStartTime(currentTime); 
    		SwingUtilities.invokeLater(r);
        }
        catch (Throwable ex) {
            ex.printStackTrace();
        }	
    }
}
