package edu.mcmaster.maplelab.av.media.animation;

public interface AnimationSource {
	public AnimationSequence getAnimationSequence();
	public int getNumPoints();
	public float getDiskRadius();
	public boolean isConnected();
}
