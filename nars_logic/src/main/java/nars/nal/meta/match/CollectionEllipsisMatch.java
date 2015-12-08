package nars.nal.meta.match;

import nars.term.Term;
import nars.term.transform.Subst;

import java.util.Collection;
import java.util.List;

/**
 * implementation which stores its series of subterms as a Term[]
 */
public class CollectionEllipsisMatch extends EllipsisMatch<Term> {

    public final Collection<Term> term;

    public CollectionEllipsisMatch(Collection<Term> term) {
        this.term = term;
    }


    @Override
    public boolean applyTo(Subst f, List<Term> target) {
        target.addAll(term);
        return true;
    }

    @Override
    public int size() {
        return term.size();
    }
}