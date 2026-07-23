package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import static org.apache.commons.lang3.StringUtils.isBlank;
import static org.jenkinsci.plugins.pollmailboxtrigger.SafeJenkins.isNull;
import static org.jenkinsci.plugins.pollmailboxtrigger.SafeJenkins.nonNull;
import static org.jenkinsci.plugins.pollmailboxtrigger.mail.utils.Stringify.stringify;

/**
 * Quick and dirty Properties implementation. (But with shorthand get/put methods).
 *
 * @author Nick Grealy
 */
@SuppressWarnings("unused")
public class CustomProperties {

    private Map<String, String> delegate = new HashMap<String, String>();

    public CustomProperties() {
    }

    public CustomProperties(final Properties properties1) {
        putAll(properties1);
    }

    public CustomProperties(final Map<String, String> properties1) {
        putAll(properties1);
    }

    public CustomProperties(final String properties) {
        putAll(properties);
    }

    public static CustomProperties read(final String properties) {
        Properties p = new Properties();
        try {
            if (nonNull(properties) && !isBlank(properties.trim())) {
                p.load(new StringReader(properties));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return new CustomProperties(p);
    }

    public void putAll(final String properties) {
        putAll(read(properties));
    }

    public void putAll(final CustomProperties p2) {
        this.delegate.putAll(p2.getMap());
    }

    public void putAll(final CustomProperties p2, final String prefix) {
        putAll(p2.getMap(), prefix);
    }

    public void putAll(final Properties p2) {
        putAll(p2, null);
    }

    public void putAll(final Properties p2, final String prefix) {
        Map<String, String> tmp = new HashMap<String, String>();
        for (String key : p2.stringPropertyNames()) {
            tmp.put(key, p2.getProperty(key));
        }
        putAll(tmp, prefix);
    }

    public void putAll(final Map<String, String> p2, final String pPrefix) {
        String prefix = pPrefix;
        if (isNull(prefix)) {
            prefix = "";
        }
        final Iterator<Map.Entry<String, String>> entries = p2.entrySet().iterator();
        while (entries.hasNext()) {
            final Map.Entry<String, String> next = entries.next();
            this.delegate.put(prefix + next.getKey(), next.getValue());
        }
    }

    public Properties getProperties() {
        Properties p = new Properties();
        for (Map.Entry entry : delegate.entrySet()) {
            if (entry != null && entry.getKey() != null && entry.getValue() != null) {
                p.put(entry.getKey(), entry.getValue());
            }
        }
        return p;
    }

    public Map<String, String> getMap() {
        return delegate;
    }

    public boolean has(final String o) {
        return delegate.containsKey(o);
    }

    public boolean has(final Enum o) {
        return has(o.name());
    }

    public String get(final Enum o) {
        return get(o.name());
    }

    public CustomProperties put(final Enum s, final String s2) {
        put(s.name(), s2);
        return this;
    }

    public String remove(final Enum o) {
        return remove(o.name());
    }

    public CustomProperties putIfBlank(final Enum s, final String s2) {
        return putIfBlank(s.name(), s2);
    }

    public CustomProperties putIfBlank(final String s, final String s2) {
        String existingValue = get(s);
        if (!has(s) || isNull(existingValue) || isBlank(existingValue)) {
            return put(s, s2);
        }
        return null;
    }

    public void removeBlanks() {
        List<String> removekeys = new ArrayList<String>();
        for (Map.Entry<String, String> entry : delegate.entrySet()) {
            if (isBlank(entry.getValue())) {
                removekeys.add(entry.getKey());
            }
        }
        for (String removekey : removekeys) {
            delegate.remove(removekey);
        }
    }

    public String toString() {
        return stringify(delegate);
    }

    /* delegate methods */

    public int size() {
        return delegate.size();
    }

    public boolean isEmpty() {
        return delegate.isEmpty();
    }

    public boolean containsKey(final String o) {
        return delegate.containsKey(o);
    }

    public boolean containsValue(final String o) {
        return delegate.containsValue(o);
    }

    public String get(final String o) {
        return delegate.get(o);
    }

    public CustomProperties put(final String s, final String s2) {
        delegate.put(s, s2);
        return this;
    }

    public String remove(final String o) {
        return delegate.remove(o);
    }

    public void putAll(final Map<? extends String, ? extends String> map) {
        delegate.putAll(map);
    }

    public void clear() {
        delegate.clear();
    }

    public Set<String> keySet() {
        return delegate.keySet();
    }

    public Collection<String> values() {
        return delegate.values();
    }

    public Set<Map.Entry<String, String>> entrySet() {
        return delegate.entrySet();
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CustomProperties)) {
            return false;
        }
        CustomProperties that = (CustomProperties) o;
        return delegate.equals(that.delegate);
    }

    @Override
    public int hashCode() {
        return delegate.hashCode();
    }
}
