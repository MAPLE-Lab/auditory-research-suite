/*
 * Copyright (C) 2006-2007 University of Virginia Supported by grants to the
 * University of Virginia from the National Eye Institute and the National
 * Institute of Deafness and Communicative Disorders. PI: Prof. Michael
 * Kubovy <kubovy@virginia.edu>
 *
 * Distributed under the terms of the GNU Lesser General Public License
 * (LGPL). See LICENSE.TXT that came with this file.
 *
 * $Id: Block.java 484 2009-06-22 13:26:59Z bhocking $
 */
package edu.mcmaster.maplelab.common.datamodel;

import java.util.List;

/**
 * Abstraction around trial blocking.
 * @version  $Revision: 484 $
 * @author  <a href="mailto:simeon.fitch@mseedsoft.com">Simeon H.K. Fitch</a>
 * @since  Sep 7, 2006
 */
public abstract class Block<S extends Session<?,?>, T extends Trial<?>> {

    protected List<T> _trials;
    private final S _session;
    private int _blockNum;
    private int _trialIndex = 0;

    protected Block(S session, int blockNum) {
        _session = session;
        _blockNum = blockNum;
    }

    protected void assignTrialNumbers() {
        int i = 1;
        for(T t : getTrials()) {
            t.setNum(i++);
        }
    }

    /**
     * @return
     * @uml.property  name="session"
     */
    public S getSession()
    {
        return _session;
    }

    /**
     * Get the list of Trials.
     *
     */
    public abstract List<T> getTrials();

    /**
     * Get the current trial. incTrial() must be called first.
     */
    public T currTrial() {
        try {
            return getTrials().get(_trialIndex);
        }
        catch(IndexOutOfBoundsException ex) {
            return null;
        }
    }

    /**
     * Increment the trail index.
     */
    public void incTrial() {
        _trialIndex++;
    }

    /**
     * Determine if this block is finished with.
     */
    public boolean isDone() {
        return (_trialIndex >= getNumTrials());
    }

    /**
     * Reduce the number of trials in this block to the given number. If number
     * is greater than current number then call is ignored. Used for constructing
     * warmup blocks.
     *
     * @param numWarmupTrials number of trials to clip to.
     */
    public void clipTrials(int numWarmupTrials) {
        if (numWarmupTrials < _trials.size()) {
            _trials = _trials.subList(0, numWarmupTrials);
        }
    }

    /**
     * Get the number of trials.
     */
    public int getNumTrials() {
        return getTrials().size();
    }

    @Override
    public String toString() {
        return String.format("Block %d", _blockNum);
    }

    /**
     * Get the assigned block number.
     */
    public int getNum() {
        return _blockNum;
    }

    /**
     * Set the block number. Useful for when original blocks are
     * randomized.
     */
    public void setNum(int blockNum) {
        _blockNum = blockNum;
    }
}
