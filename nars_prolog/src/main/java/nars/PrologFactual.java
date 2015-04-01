package nars;

import nars.nal.term.Term;
import nars.nal.nal3.SetExt;
import nars.nal.nal8.TermFunction;
import nars.prolog.NoMoreSolutionException;
import nars.prolog.Prolog;
import nars.prolog.SolveInfo;
import nars.prolog.Struct;

import java.util.HashSet;
import java.util.Set;

import static nars.NARPrologMirror.nterm;

/**
 *  usage: factual(termexpression, #resultSet)!
*/
public class PrologFactual extends TermFunction {


    private final PrologContext context;

    protected PrologFactual(PrologContext p) {
        super("^factual");
        this.context = p;
    }

    final double solveTime = 40.0f / 1e3f; //millisec
    final int maxAnswers = 16;

    @Override
    public Term function(Term[] x) {
        Prolog p = context.getProlog(null); //default

            nars.prolog.Term factTerm = NARPrologMirror.pterm(x[0]);
            if (factTerm instanceof Struct)
                try {
                    SolveInfo si = p.solve(factTerm, solveTime);

                    nars.prolog.Term lastSolution = null;

                    int a = 0;
                    Set<Term> answers = new HashSet();
                    do {
                        if (si == null) break;

                        nars.prolog.Term solution = si.getSolution();
                        if (solution == null)
                            break;

                        if (lastSolution!=null && solution.equals(lastSolution))
                            continue;

                        lastSolution = solution;

                        try {
                            Term n = nterm(solution);
                            if (n!=null)
                                answers.add(n);
                        }
                        catch (Exception e) {
                            e.printStackTrace();
                        }

                        try {
                            si = p.solveNext(solveTime);
                        }
                        catch (NoMoreSolutionException e) {
                            break;
                        }

                        //solveTime /= 2d;
                    }
                    while ((a++) < maxAnswers);

                    p.solveEnd();

                    if (answers.isEmpty()) {
                        return Term.get("null");
                    }
                    else {
                        return SetExt.make(answers);
                    }

                } catch (Exception e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }
            else {
                throw new RuntimeException("Could not assert non-struct: " + factTerm);
            }

    }

}
