package edu.umich.carlab.template;

import edu.umich.carlab.TriggerSession;
import edu.umich.carlab.utils.Utilities;

public class ManualTrigger extends TriggerSession {
    @Override
    protected boolean checkSleepCondition() {
        return !prefs.getBoolean(Constants.ManualChoiceKey, false);
    }

    @Override
    protected boolean checkWakeupCondition() {
        return prefs.getBoolean(Constants.ManualChoiceKey, false);
    }

    @Override
    protected void reschedule(long triggerInMillis) {
        Utilities.scheduleOnce(context, getClass(), triggerInMillis);
    }
}
