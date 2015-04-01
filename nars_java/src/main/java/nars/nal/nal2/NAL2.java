package nars.nal.nal2;

import nars.budget.Budget;
import nars.nal.*;
import nars.nal.nal1.Inheritance;
import nars.nal.nal5.Equivalence;
import nars.nal.term.Compound;
import nars.nal.term.Term;

/**
 * Created by me on 1/13/15.
 */
public class NAL2 {
    /**
     * {<S --> P>, <P --> S} |- <S <-> p> Produce Similarity/Equivalence from a
     * pair of reversed Inheritance/Implication
     *
     * @param judgment1 The first premise
     * @param judgment2 The second premise
     * @param nal Reference to the memory
     */
    public static void inferToSym(Sentence judgment1, Sentence judgment2, NAL nal) {
        Statement s1 = (Statement) judgment1.term;
        Term t1 = s1.getSubject();
        Term t2 = s1.getPredicate();
        Compound content;
        if (s1 instanceof Inheritance) {
            content = Similarity.make(t1, t2);
        } else {
            content = Equivalence.make(t1, t2, s1.getTemporalOrder());
        }
        TruthValue truth = TruthFunctions.intersection(judgment1.truth, judgment2.truth);
        Budget budget = BudgetFunctions.forward(truth, nal);
        nal.doublePremiseTask(content, truth, budget,
                nal.newStamp(judgment1, judgment2), false, false);
    }
}
