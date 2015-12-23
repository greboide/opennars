package nars.nal.nal8.operator;

import nars.nal.nal8.AbstractOperator;
import nars.term.Term;

/**
 * Operator which executes synchronously (in current reasoner thread).
 * Should be used only if the operation procedure will not take long
 * and block the reasoner thread.
 */
public abstract class SyncOperator extends AbstractOperator {

    public SyncOperator(Term term) {
        super(term);
    }

    public SyncOperator(String name) {
        super(name);
    }

    /** uses the implementation class's simpleName as the term */
    public SyncOperator() {
        super((Term)null);
    }

}
