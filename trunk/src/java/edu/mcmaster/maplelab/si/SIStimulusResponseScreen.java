package edu.mcmaster.maplelab.si;

import edu.mcmaster.maplelab.av.AVStimulusResponseScreen;
import edu.mcmaster.maplelab.common.datamodel.MultiResponse;
import edu.mcmaster.maplelab.common.gui.ResponseInputs;
import edu.mcmaster.maplelab.common.gui.SliderResponseInputs;
import edu.mcmaster.maplelab.common.gui.StepManager;
import edu.mcmaster.maplelab.si.datamodel.SIResponseParameters;
import edu.mcmaster.maplelab.si.datamodel.SISession;
import edu.mcmaster.maplelab.si.datamodel.SITrial;

public class SIStimulusResponseScreen extends AVStimulusResponseScreen<MultiResponse, SITrial, 
									SITrialLogger, SISession> {

	public SIStimulusResponseScreen(StepManager steps, SISession session,
			boolean isWarmup) {
		super(steps, session, isWarmup);
	}

	@Override
	public ResponseInputs<MultiResponse> createResponseInputs(SISession session) {
		SliderResponseInputs sri = new SliderResponseInputs(SIResponseParameters.getResponseParameters(session));
		sri.enableSliderTickMarks(session.getShowTickMarks());
		return sri;
	}

	@Override
	public void updateResponseInputs(SITrial trial) {
		SliderResponseInputs inputs = (SliderResponseInputs) getResponseInputs();
		int num = trial.getNumMediaObjects();
		if (inputs != null) inputs.setInputVisibility(1, num > 1);
	}

}
