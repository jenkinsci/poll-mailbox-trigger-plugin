package org.jenkinsci.plugins.pollmailboxtrigger;

import org.jenkinsci.lib.xtrigger.XTriggerLog;

import java.util.List;

/**
 * I pity the fool, who tries to log to two places...
 * <b>See also:</b> <a href="https://en.wikipedia.org/wiki/Tee_(command)">https://en.wikipedia.org/wiki/Tee_(command)</a>
 */
public class TeeLogger {

    private XTriggerLog log;
    private List<String> testing;

    public TeeLogger(final XTriggerLog log, final List<String> testing) {
        this.log = log;
        this.testing = testing;
    }

    public void info(final String message) {
        log.info(message);
        testing.add(message);
    }

    public void error(final String message) {
        log.error(message);
        testing.add(message);
    }

    public XTriggerLog getLog() {
        return log;
    }

    public List<String> getTesting() {
        return testing;
    }
}
