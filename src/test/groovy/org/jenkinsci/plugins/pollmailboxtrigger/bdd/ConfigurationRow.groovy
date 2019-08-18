package org.jenkinsci.plugins.pollmailboxtrigger.bdd

import hudson.util.Secret
import org.apache.commons.lang.StringUtils

import static org.jenkinsci.plugins.pollmailboxtrigger.SafeJenkins.isNull

class ConfigurationRow {

    String host
    String username
    String password
    String script
    String attachments

    Secret buildPasswordSecret(){
        password ? new Secret(password) : null
    }

    void appendScript(String newScript){
        if (!StringUtils.isBlank(newScript)){
            script = isNull(script) ? newScript : script + "\n" + newScript
        }
    }
}
