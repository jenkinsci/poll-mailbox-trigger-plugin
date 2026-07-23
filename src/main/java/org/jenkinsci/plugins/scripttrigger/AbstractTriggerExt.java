package org.jenkinsci.plugins.scripttrigger;

import antlr.ANTLRException;
import hudson.Util;
import hudson.model.Action;
import hudson.model.BuildableItem;
import hudson.model.Node;
import org.apache.commons.lang3.StringUtils;
import org.jenkinsci.lib.xtrigger.XTriggerLog;
import org.jenkinsci.plugins.pollmailboxtrigger.PollMailboxTriggerRunAction;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Gregory Boissinot
 */
public abstract class AbstractTriggerExt extends org.jenkinsci.lib.xtrigger.AbstractTrigger {

    private boolean labelRestriction;

    private boolean enableConcurrentBuild;

    // ANTLRException is deprecated in Jenkins core but is still declared by the
    // xtrigger-lib AbstractTrigger constructor that this class extends, so it cannot be avoided.
    @SuppressWarnings("deprecation")
    public AbstractTriggerExt(
            final String cronTabSpec,
            final boolean labelRestriction,
            final String triggerLabel,
            final boolean enableConcurrentBuild
    ) throws ANTLRException {
        super(cronTabSpec, triggerLabel, enableConcurrentBuild);
        this.labelRestriction = labelRestriction;
        this.enableConcurrentBuild = enableConcurrentBuild;
    }

    @SuppressWarnings("unused")
    public boolean isEnableConcurrentBuild() {
        return enableConcurrentBuild;
    }

    @SuppressWarnings("unused")
    public boolean isLabelRestriction() {
        return labelRestriction;
    }

    @Override
    protected void start(final Node pollingNode, final BuildableItem project, final boolean newInstance, final XTriggerLog log) {
    }

    @Override
    protected String getName() {
        return "ScriptTrigger";
    }

    @Override
    protected String getCause() {
        try {
            String scriptContent = Util.loadFile(getLogFile(), StandardCharsets.UTF_8);
            String cause = extractRootCause(scriptContent);
            if (cause == null) {
                return getDefaultMessageCause();
            }
            return cause;
        } catch (IOException e) {
            return getDefaultMessageCause();
        }
    }

    protected abstract String getDefaultMessageCause();

    private String extractRootCause(final String content) {
        return StringUtils.substringBetween(content, "<cause>", "</cause>");
    }

    @Override
    protected Action[] getScheduledActions(final Node pollingNode, final XTriggerLog log) {
        String scriptContent;
        try {
            scriptContent = Util.loadFile(getLogFile(), StandardCharsets.UTF_8);
        } catch (IOException e) {
            return new Action[0];
        }

        List<Action> actionList = new ArrayList<Action>();
        final String description = extractDescription(scriptContent);
        if (description != null) {
            actionList.add(new PollMailboxTriggerRunAction(description));
        }
        return actionList.toArray(new Action[actionList.size()]);
    }

    private String extractDescription(final String content) {
        return StringUtils.substringBetween(content, "<description>", "</description>");
    }

    protected boolean requiresWorkspaceForPolling() {
        return false;
    }

}
