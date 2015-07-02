package nars.nal.rule;

import nars.nal.LogicRule;
import nars.nal.tlink.TaskLink;
import nars.nal.tlink.TermLink;
import nars.nal.process.ConceptProcess;

/** when a concept fires a tasklink that fires a termlink */
abstract public class ConceptFireTaskTerm implements LogicRule<ConceptProcess>  {


    abstract public boolean apply(ConceptProcess f, TaskLink taskLink, TermLink termLink);

    @Override
    public boolean accept(final ConceptProcess f) {

        final TermLink ftl = f.getCurrentTermLink();

        if (ftl !=null) {
            return apply(f, f.getCurrentTaskLink(), ftl);
        }

        //continue by default
        return true;
    }

}

