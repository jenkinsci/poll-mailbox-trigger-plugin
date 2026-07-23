package org.jenkinsci.plugins.pollmailboxtrigger.mail.utils;

import javax.mail.Flags;
import javax.mail.search.AndTerm;
import javax.mail.search.ComparisonTerm;
import javax.mail.search.FlagTerm;
import javax.mail.search.FromStringTerm;
import javax.mail.search.NotTerm;
import javax.mail.search.ReceivedDateTerm;
import javax.mail.search.SearchTerm;
import javax.mail.search.SubjectTerm;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

/**
 * A slightly nicer way of building search terms.
 *
 * @author Nick Grealy
 */
public final class SearchTermHelpers {

    private SearchTermHelpers() {
    }

    public static SearchTerm and(final SearchTerm... terms) {
        return new AndTerm(terms);
    }

    public static SearchTerm and(final List<SearchTerm> terms) {
        return new AndTerm(terms.toArray(new SearchTerm[terms.size()]));
    }

    public static SearchTerm not(final SearchTerm term) {
        return new NotTerm(term);
    }

    public static SearchTerm subject(final String containsCaseInsensitive) {
        return new SubjectTerm(containsCaseInsensitive);
    }

    public static SearchTerm from(final String email) {
        return new FromStringTerm(email);
    }

    public static SearchTerm receivedSince(final Date date) {
        return new ReceivedDateTerm(ComparisonTerm.GT, date);
    }

    public static SearchTerm flag(final Flags.Flag flag) {
        return new FlagTerm(new Flags(flag), true);
    }

    /**
     * @param unit   - e.g. Calendar.HOURS
     * @param amount - negative for the past!
     * @return The past date.
     */
    public static Date relativeDate(final int unit, final int amount) {
        GregorianCalendar calendar = new GregorianCalendar();
        calendar.add(unit, amount);
        return calendar.getTime();
    }

}
