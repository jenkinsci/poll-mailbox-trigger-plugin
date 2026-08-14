package org.jenkinsci.plugins.pollmailboxtrigger.environment;

import hudson.model.Action;
import hudson.model.Queue.QueueAction;

import java.util.List;
import java.util.Map;

/**
 * This wrapper is needed since SECURITY-170 is blocking undefined build parameters in the job.
 */
public class SetEnvironmentVariablesAction implements QueueAction {

    private final Map<String, String> parameters;

    public SetEnvironmentVariablesAction(final Map<String, String> parameters) {
        this.parameters = parameters;
    }

    public Map<String, String> getParametersAsMap() {
        return parameters;
    }

    // interface -> hudson.model.Action

    @Override
    public String getIconFileName() {
        // don't create a link to content...
        return null;
    }

    @Override
    public String getDisplayName() {
        // don't create a link to content...
        return null;
    }

    @Override
    public String getUrlName() {
        // don't create a link to content...
        return null;
    }

    // interface -> hudson.model.Queue.QueueAction

    @Override
    public boolean shouldSchedule(final List<Action> actions) {
        // always execute task...
        return true;
    }

}