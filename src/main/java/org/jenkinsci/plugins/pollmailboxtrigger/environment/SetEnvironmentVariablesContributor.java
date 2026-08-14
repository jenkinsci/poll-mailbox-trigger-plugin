package org.jenkinsci.plugins.pollmailboxtrigger.environment;

import hudson.EnvVars;
import hudson.Extension;
import hudson.model.EnvironmentContributor;
import hudson.model.Run;
import hudson.model.TaskListener;
import edu.umd.cs.findbugs.annotations.NonNull;

import java.io.IOException;

/**
 * If {@link SetEnvironmentVariablesAction} exists, set the environment variables.
 */
@Extension
public final class SetEnvironmentVariablesContributor extends EnvironmentContributor {

    @Override
    public void buildEnvironmentFor(@NonNull final Run r, @NonNull final EnvVars envVars, @NonNull final TaskListener listener)
            throws IOException, InterruptedException {
        SetEnvironmentVariablesAction action = r.getAction(SetEnvironmentVariablesAction.class);
        if (action != null) {
            envVars.putAll(action.getParametersAsMap());
        }
    }
}
