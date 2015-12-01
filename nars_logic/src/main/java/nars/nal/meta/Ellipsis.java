package nars.nal.meta;

import com.gs.collections.api.block.predicate.primitive.IntObjectPredicate;
import com.gs.collections.api.set.primitive.ShortSet;
import nars.$;
import nars.nal.nal4.Product;
import nars.nal.nal7.InvisibleAtom;
import nars.term.*;
import nars.util.utf8.Utf8;

/**
 * Meta-term of the form:
 *      variable..exppression
 * for selecting a range of subterms
 *
 * ex:
 *   A..not(X)
 *   B..not(X,Y)
 *   B..not(first)
 *   B..not(first,last)
 */
public class Ellipsis extends Variable.VarPattern { //TODO use Immutable


    /** a placeholder that indicates an expansion of one or more terms that will be provided by an Ellipsis match.
     *  necessary for terms which require > 1 argument but an expression that will expand one ellipsis variable will not construct a valid prototype of it
     *  ex:
     *    (|, %A, ..)
     *
     *  */
    public final static InvisibleAtom Expand = new InvisibleAtom("..") {

    };

    /** 1 or more */
    public static Term PLUS = Atom.the("+");

    /** 0 or more */
    public static Term ASTERISK = Atom.the("*");

    /** everything except, ex: not(%2) */
    public static final Atom NOT = $.the("not");



    public final Variable name;
    public final Term expression;

    public Ellipsis(Variable name, Term expression) {
        super(
            Utf8.toUtf8(name.toString().substring(1) /* exclude variable type char */
                    + ".." + expression.toString())
        );

        this.name= name;
        this.expression = expression;
    }



    @Override
    public int volume() {
        return 0;
    }

    public static boolean hasEllipsis(Compound x) {
        int xs = x.size();
        for (int i = 0; i < xs; i++)
            if (x.term(i) instanceof Ellipsis) return true;
        return false;
    }

    public static int countEllipsisSubterms(Compound x) {
        final int xs = x.size();
        int n = 0;
        for (int i = 0; i < xs; i++) {
            Term xt = x.term(i);
            if (xt instanceof Ellipsis)
                n++;
        }
        return n;
    }

    public static int countNonEllipsisSubterms(Compound x) {
        final int xs = x.size();
        int n = xs;
        for (int i = 0; i < xs; i++) {
            Term xt = x.term(i);

            if ((xt instanceof Ellipsis)
             || (xt==Ellipsis.Expand)) //ignore expansion placeholders
                n--;
        }
        return n;
    }



    public Product match(ShortSet ySubsExcluded, Compound y) {
        Term ex = this.expression;
        if (ex == PLUS) {
            return matchRemainingOneOrMore(ySubsExcluded, y);
        }

        throw new RuntimeException("unimplemented expression: " + ex);

//        else if (ex instanceof Operation) {
//
//            Operation o = (Operation) ex;
//
//            //only NOT implemented currently
//            if (!o.getOperatorTerm().equals(NOT)) {
//                throw new RuntimeException("ellipsis operation " + expression + " not implemented");
//            }
//
//            return matchNot(o.args(), mapped, y);
//        }
    }

    private static Product matchRemainingOneOrMore(ShortSet ySubsExcluded, Compound Y) {

        final int ysize = Y.size();
        Term[] others = new Term[ysize-ySubsExcluded.size()];
        int k = 0;
        for (int j = 0; j < ysize; j++) {
            if (!ySubsExcluded.contains((short) j)) {
                Term yt = Y.term(j);
                others[k++] = yt;
            }
        }
        return Product.make(others);
    }

//    private static Term matchNot(Term[] oa, Map<Term, Term> mapped, Compound Y) {
//
//        if (oa.length!=1) {
//            throw new RuntimeException("only 1-arg not() implemented");
//        }
//
//        Term exclude = oa[0];
//
//        final int ysize = Y.size();
//        Term[] others = new Term[ysize-1];
//        int k = 0;
//        for (int j = 0; j < ysize; j++) {
//            Term yt = Y.term(j);
//            if (!mapped.get(exclude).equals(yt))
//                others[k++] = yt;
//        }
//        return Product.make(others);
//    }

    /**
     * @param x a compound which contains one or more ellipsis terms */
    public static int countNumNonEllipsis(Compound x) {
        //TODO depending on the expression, determine the sufficient # of terms Y must contain
        final int xsize = x.size();
        int numNonVarArgs = Ellipsis.countNonEllipsisSubterms(x);
        return numNonVarArgs;
    }

    public boolean valid(int numNonVarArgs, int ysize) {

        int collectable = ysize - numNonVarArgs;
        Term exp = this.expression;

        if (exp == PLUS)
            return collectable > 0;
        else if (exp == ASTERISK)
            return collectable >= 0;

        return false;
    }

    public static Ellipsis getFirstEllipsis(Compound X) {
        final int xsize = X.size();
        for (int i = 0; i < xsize; i++) {
            Term xi = X.term(i);
            if (xi instanceof Ellipsis) {
                return (Ellipsis) xi;
            }
        }
        return null;
    }
    public static Term getFirstNonEllipsis(Compound X) {
        final int xsize = X.size();
        for (int i = 0; i < xsize; i++) {
            Term xi = X.term(i);
            if (!(xi instanceof Ellipsis)) {
                return xi;
            }
        }
        return null;
    }

    public static Term matchedSubterms(TermContainer subterms) {
        return matchedSubterms(subterms.toArray());
    }

    public static Term matchedSubterms(TermContainer subterms, IntObjectPredicate<Term> filter) {
        return matchedSubterms(subterms.toArray(filter));
    }

    /** TODO use a special ProductN subclass */
    public static Term matchedSubterms(Term[] subterms) {
        return Product.make(subterms);
    }


    //    public static RangeTerm rangeTerm(String s) {
//        int uscore = s.indexOf("_");
//        if (uscore == -1) return null;
//        int periods = s.indexOf("..");
//        if (periods == -1) return null;
//        if (periods < uscore) return null;
//
//        String prefix = s.substring(0, uscore);
//        int from = Integer.parseInt( s.substring(uscore, periods) );
//        String to = s.substring(periods+2);
//        if (to.length() > 1) return null;
//
//        return new RangeTerm(prefix, from, to.charAt(0));
//    }
}
