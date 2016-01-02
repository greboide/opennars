package nars.nal;

import nars.$;
import nars.Global;
import nars.Op;
import nars.budget.Budget;
import nars.budget.BudgetFunctions;
import nars.nal.meta.TaskBeliefPair;
import nars.nal.meta.op.MatchTerm;
import nars.nal.nal7.Tense;
import nars.nal.nal8.Operator;
import nars.nal.op.ImmediateTermTransform;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.term.Term;
import nars.term.Termed;
import nars.term.compound.Compound;
import nars.term.transform.FindSubst;
import nars.term.transform.VarCachedVersionMap;
import nars.truth.Truth;
import nars.util.version.Versioned;

import java.util.Map;
import java.util.Random;
import java.util.function.Consumer;


/**
 * rule matching context, re-recyclable as thread local
 */
public class PremiseMatch extends FindSubst {

    /** Global Context */
    public Consumer<Task> receiver;

    /** current Premise */
    public ConceptProcess premise;

    public final VarCachedVersionMap secondary;
    public final Versioned<Integer> occurrenceShift;
    public final Versioned<Truth> truth;
    public final Versioned<Character> punct;
    public final Versioned<MatchTerm> pattern;

    private TaskBeliefPair termPattern = new TaskBeliefPair();
    public boolean cyclic;

    final Map<Operator, ImmediateTermTransform> transforms =
            Global.newHashMap();

    public PremiseMatch(Random r) {
        super(Op.VAR_PATTERN, r );

        for (Class<? extends ImmediateTermTransform> c : PremiseRule.Operators) {
            addTransform(c);
        }

        secondary = new VarCachedVersionMap(this);
        occurrenceShift = new Versioned(this);
        truth = new Versioned(this);
        punct = new Versioned(this);
        pattern = new Versioned(this);
    }

    private void addTransform(Class<? extends ImmediateTermTransform> c) {
        Operator o = $.operator(c.getSimpleName());
        try {
            transforms.put(o, c.newInstance());
        } catch (Exception e) {
            throw new RuntimeException(c + ": " + e);
        }
    }

    @Override public ImmediateTermTransform getTransform(Operator t) {
        return transforms.get(t);
    }



    public final void match(MatchTerm pattern /* callback */) {
        this.pattern.set(pattern); //to notify of matches
        this.constraints = constraints;
        matchAll(pattern.x, term.get() /* current term */);
        this.constraints = null;
    }

    @Override
    public boolean onMatch() {
        return pattern.get().onMatch(this);
    }




    @Override
    public String toString() {
        return "RuleMatch:{" +
                "premise:" + premise +
                ", subst:" + super.toString() +
                (pattern.get()!=null ? (", derived:" + pattern) : "")+
                (truth.get()!=null ? (", truth:" + truth) : "")+
                (!secondary.isEmpty() ? (", secondary:" + secondary) : "")+
                (occurrenceShift.get()!=null ? (", occShift:" + occurrenceShift) : "")+
                //(branchPower.get()!=null ? (", derived:" + branchPower) : "")+
                '}';

    }

    /**
     * set the next premise
     */
    public final void start(ConceptProcess p, Consumer<Task> receiver, Deriver d) {

        premise = p;
        this.receiver = receiver;

        Compound taskTerm = p.getTask().term();
        Task pBelief = p.getBelief();
        Termed beliefTerm = pBelief != null ? pBelief.get() : p.termLink.get(); //experimental, prefer to use the belief term's Term in case it has more relevant TermMetadata (intermvals)

        termPattern.set( taskTerm.term(), beliefTerm.term() );
        term.set( termPattern );

        cyclic = p.isCyclic();

//        //set initial power which will be divided by branch
//        setPower(
//            //LERP the power in min/max range by premise mean priority
//            (int) ((p.getMeanPriority() * (Global.UNIFICATION_POWER - Global.UNIFICATION_POWERmin))
//                    + Global.UNIFICATION_POWERmin)
//        );

        //setPower(branchPower.get()); //HACK is this where it should be assigned?

        d.run(this);

        clear();

    }





    public final void occurrenceAdd(long durationsDelta) {
        //TODO move to post
        int oc = occurrenceShift.getIfAbsent(Tense.TIMELESS);
        if (oc == Tense.TIMELESS)
            oc = 0;
        oc += durationsDelta * premise.getTask().duration();
        occurrenceShift.set((int)oc);
    }

    /** calculates Budget used in a derived task,
     *  returns null if invalid / insufficient */
    public final Budget getBudget(Truth truth, Termed c) {

        ConceptProcess p = this.premise;

        Budget budget = truth != null ?
                BudgetFunctions.compoundForward(truth, c, p) :
                BudgetFunctions.compoundBackward(c, p);

//        if (Budget.isDeleted(budget.getPriority())) {
//            throw new RuntimeException("why is " + budget + " deleted");
//        }

        if (!!budget.summaryLessThan(p.memory().derivationThreshold.floatValue())) {
//            if (false) {
//                RuleMatch.removeInsufficientBudget(premise, new PreTask(t,
//                        m.punct.get(), truth, budget,
//                        m.occurrenceShift.getIfAbsent(Tense.TIMELESS), premise));
//            }
            return null;
        }

        return budget;
    }

    @Override public final Term apply(Term t) {
        //TODO make a half resolve that only does xy?

//        Term ret = getXY(t);
//        if (ret != null) {
//            ret = getYX(ret);
//        }
//
//        if (ret != null) return ret;
//        return t;
            Term ret = premise.memory().index.apply(this, t);

//            if ((ret != null) /*&& (!yx.isEmpty())*/) {
//                ret = ret.apply(yx, fullMatch);
//            }
            return ret;


    }


}

