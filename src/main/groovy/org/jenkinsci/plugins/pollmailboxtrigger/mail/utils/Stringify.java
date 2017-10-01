package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils;

import org.apache.commons.lang.StringUtils;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static javax.mail.Flags.Flag.ANSWERED;
import static javax.mail.Flags.Flag.DELETED;
import static javax.mail.Flags.Flag.DRAFT;
import static javax.mail.Flags.Flag.FLAGGED;
import static javax.mail.Flags.Flag.RECENT;
import static javax.mail.Flags.Flag.SEEN;
import static javax.mail.Flags.Flag.USER;

/**
 * @author Nick Grealy
 */
public abstract class Stringify {

    public static final String DATE_FORMAT_TEXT = "yyyy-MM-dd'T'HH:mm'Z'";

    /**
     * Converts the given object to a {@link String}.
     *
     * @param t the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        t.printStackTrace(pw);
        return sw.toString();
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param message the object to convert to a String.
     * @return a String
     */
    public static String stringify(final String message) {
        return message;
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param array the object to convert to a String.
     * @param <A>   the type of the array
     * @return a String
     */
    public static <A extends Object> String stringify(final A[] array) {
        return stringify(array, null);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param array     the object to convert to a String.
     * @param delimiter a delimiter.
     * @param <A>
     * @return a String
     */
    public static <A extends Object> String stringify(final A[] array, final String delimiter) {
        return stringify(Arrays.asList(array), delimiter);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param list the object to convert to a String.
     * @return a String
     */
    public static String stringify(final List list) {
        return stringify(list.iterator(), null);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param list      the object to convert to a String.
     * @param delimiter a delimiter.
     * @return a String
     */
    public static String stringify(final List list, final String delimiter) {
        return stringify(list.iterator(), delimiter);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param list the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Set list) {
        return stringify(list.iterator(), null);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param list      the object to convert to a String.
     * @param delimiter a delimiter.
     * @return a String
     */
    public static String stringify(final Set list, final String delimiter) {
        return stringify(list.iterator(), delimiter);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param iterator   the object to convert to a String.
     * @param pDelimiter a delimiter.
     * @return a String
     */
    public static String stringify(final Iterator iterator, final String pDelimiter) {
        String delimiter = pDelimiter;
        if (delimiter == null) {
            delimiter = ", ";
        }
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            try {
                sb.append(stringify(iterator.next()));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (iterator.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param iterator           the object to convert to a String.
     * @param pKeyValueDelimiter a delimiter.
     * @param pDelimiter         a delimiter.
     * @return a String
     */
    public static String stringify(final Iterator<? extends Map.Entry<?, ?>> iterator, final String pKeyValueDelimiter, final String pDelimiter) {
        String keyValueDelimiter = pKeyValueDelimiter;
        String delimiter = pDelimiter;
        if (keyValueDelimiter == null) {
            keyValueDelimiter = "=";
        }
        if (delimiter == null) {
            delimiter = ", ";
        }
        StringBuilder sb = new StringBuilder();
        while (iterator.hasNext()) {
            try {
                sb.append(stringify(iterator.next(), keyValueDelimiter));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            if (iterator.hasNext()) {
                sb.append(delimiter);
            }
        }
        return sb.toString();
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param date the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Date date) {
        if (date == null) {
            return "null";
        }
        return new SimpleDateFormat(DATE_FORMAT_TEXT).format(date);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param messageNumber the object to convert to a String.
     * @return a String
     */
    public static String stringify(final int messageNumber) {
        return String.valueOf(messageNumber);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param allHeaders the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Enumeration allHeaders) {
        if (allHeaders == null) {
            return "null";
        }
        List<Object> list = new ArrayList<Object>();
        while (allHeaders.hasMoreElements()) {
            list.add(allHeaders.nextElement());
        }
        return stringify(list);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param content the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Map content) {
        return stringify(content, null, null);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param content           the object to convert to a String.
     * @param keyValueDelimiter a delimiter.
     * @param delimiter         a delimiter.
     * @return a String
     */
    public static String stringify(final Map<?, ?> content, final String keyValueDelimiter, final String delimiter) {
        Set<? extends Map.Entry<?, ?>> entries = content.entrySet();
        Iterator<? extends Map.Entry<?, ?>> iterator = entries.iterator();
        return stringify(iterator, keyValueDelimiter, delimiter);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param content           the object to convert to a String.
     * @param keyValueDelimiter a delimiter.
     * @return a String
     */
    public static String stringify(final Map.Entry content, final String keyValueDelimiter) {
        return content.getKey() + keyValueDelimiter + content.getValue();
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param content the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Object content) {
        if (content instanceof Header) {
            return stringify((Header) content);
        } else if (content instanceof Part) {
            return stringify((Part) content);
        } else if (content instanceof Map) {
            return stringify((Map) content);
        } else if (content instanceof Flags.Flag) {
            return stringify((Flags.Flag) content);
        }
        return content == null ? "null" : content.toString();
    }


    /* javax.mail */

    public static final String TEXT_WILDCARD = "text/*", TEXT_HTML = "text/html", TEXT_PLAIN = "text/plain",
            MULTIPART_WILDCARD = "multipart/*", MULTIPART_ALTERNATIVE = "multipart/alternative", NEWLINE = "\n",
            BLANK = "";


    /**
     * Converts the given object to a {@link String}.
     *
     * @param content the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Header content) {
        return content.getName() + "=" + content.getValue();
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param addresses the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Address[] addresses) {
        if (addresses != null) {
            List<String> parsed = new ArrayList<String>();
            for (Address addr : addresses) {
                if (addr instanceof InternetAddress) {
                    parsed.add(((InternetAddress) addr).getAddress());
                } else {
                    parsed.add(addr.toString());
                }
            }
            return StringUtils.join(parsed, ",");
        }
        return "";
    }

    /**
     * Get text from all parts of the email.
     * Loosely based on <a href="http://www.oracle.com/technetwork/java/javamail/faq/index.html#mainbody">oracle.com</a>.
     *
     * @param part - the mail part to convert to string.
     * @return a String
     */
    public static String stringify(final Part part) {
        try {
            return stringify(new StringBuilder(), part);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static String stringify(final StringBuilder builder, final Part part) throws MessagingException, IOException {
        final Object content = part.getContent();
        if (content == null) {
            return "null";
        }
        if (part.isMimeType(TEXT_WILDCARD) && content instanceof String) {
            builder.append(NEWLINE + content);
        }
        if (part.isMimeType(MULTIPART_WILDCARD) && content instanceof Multipart) {
            Multipart mp = (Multipart) content;

            // iterate backwards through the parts... should be html first (muddled with html tags), plain text (pure) last
            for (int i = mp.getCount() - 1; i >= 0; i--) {
                stringify(builder, mp.getBodyPart(i));
            }
        }
        return builder.toString();
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param flags the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Flags flags) {
        if (flags == null) {
            return "null";
        }
        List<Object> tmp = new ArrayList<Object>();
        tmp.addAll(Arrays.asList(flags.getSystemFlags()));
        tmp.addAll(Arrays.asList(flags.getUserFlags()));
        return stringify(tmp);
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param flag the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Flags.Flag flag) {
        if (flag == SEEN) {
            return "SEEN";
        } else if (flag == ANSWERED) {
            return "ANSWERED";
        } else if (flag == DELETED) {
            return "DELETED";
        } else if (flag == DRAFT) {
            return "DRAFT";
        } else if (flag == FLAGGED) {
            return "FLAGGED";
        } else if (flag == RECENT) {
            return "RECENT";
        } else if (flag == USER) {
            return "USER";
        } else {
            return "";
        }
    }

    /**
     * Converts the given object to a {@link String}.
     *
     * @param folder the object to convert to a String.
     * @return a String
     */
    public static String stringify(final Folder folder) {
        if (folder == null) {
            return "null";
        }
        return folder.getFullName();
    }

}
