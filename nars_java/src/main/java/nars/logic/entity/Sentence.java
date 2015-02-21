/*
 * Sentence.java
 *
 * Copyright (C) 2008  Pei Wang
 *
 * This file is part of Open-NARS.
 *
 * Open-NARS is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2 of the License, or
 * (at your option) any later version.
 *
 * Open-NARS is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Open-NARS.  If not, see <http://www.gnu.org/licenses/>.
 */
package nars.logic.entity;

import nars.core.NAR;
import nars.core.Parameters;
import nars.io.Symbols;
import nars.io.Texts;
import nars.logic.NAL;
import nars.logic.Terms.Termable;
import nars.logic.TruthFunctions;
import nars.logic.TruthFunctions.EternalizedTruthValue;
import nars.logic.entity.TruthValue.Truthable;
import nars.logic.entity.stamp.Stamp;
import nars.logic.entity.stamp.Stamped;
import nars.logic.nal5.Conjunction;
import nars.logic.nal7.Interval;
import nars.logic.nal7.TemporalRules;
import nars.logic.nal8.Operation;
import nars.logic.nal8.Operator;

import java.util.*;

/**
 * A Sentence is an abstract class, mainly containing a Term, a TruthValue, and
 * a Stamp.
 * <p>
 * It is used as the premises and conclusions of all logic rules.
 */
public class Sentence<T extends CompoundTerm> implements Cloneable, Termable, Truthable, Stamped {



    public static interface Sentenced<T2 extends CompoundTerm> extends Termable {
        public Sentence<T2> getSentence();
    }

    /**
     * The content of a Sentence is a Term
     */
    public final T term;
    
    /**
     * The punctuation also indicates the type of the Sentence: 
     * Judgment, Question, Goal, or Quest.
     * Represented by characters: '.', '?', '!', or '@'
     */
    public final char punctuation;
    
    /**
     * The truth value of Judgment, or desire value of Goal     
     */
    public final TruthValue truth;
    
    /**
     * Partial record of the derivation path
     */
    public final Stamp stamp;

    /**
     * Whether the sentence can be revised
     */
    private boolean revisible;

    /** caches the 'getKey()' result */
    private transient CharSequence key;

    transient private int hash;


    public Sentence(Term invalidTerm, char punctuation, TruthValue newTruth, NAL.StampBuilder newStamp) {
        this((T)Sentence.termOrException(invalidTerm), punctuation, newTruth, newStamp);
    }

    public Sentence(T term, char punctuation, TruthValue newTruth, NAL.StampBuilder newStamp) {
        this(term, punctuation, newTruth, newStamp, true);
    }

    /**
     * Create a Sentence with the given fields
     *
     * @param seedTerm The Term that forms the content of the sentence
     * @param punctuation The punctuation indicating the type of the sentence
     * @param truth The truth value of the sentence, null for question
     * @param stamp The stamp of the sentence indicating its derivation time and
     * base
     */
    private Sentence(T seedTerm, final char punctuation, final TruthValue truth, final NAL.StampBuilder stamp, boolean normalize) {
        


        this.punctuation = punctuation;

        if ( (truth == null) && (!isQuestion() && !isQuest()) ) {
            throw new RuntimeException("Judgment and Goal sentences require non-null truth value");
        }



        Stamp st = stamp.build();
        if ((isQuestion() || isQuest()) && !st.isEternal()) {
            //need to clone in case this stamp is shared by others which are not to eternalize it
            st = st.cloneEternal();
            if (Parameters.DEBUG)
                throw new RuntimeException("Questions and Quests require eternal tense");
        }

        this.truth = truth;


        //HACK special handling for conjunctions which may have a trailing interval
        if ((seedTerm instanceof Conjunction) && (seedTerm.getTemporalOrder() == TemporalRules.ORDER_FORWARD)) {
            st = st.clone(); //temporary: clone the stamp in case it's modified in getTerm; shouldnt be necessary when terms are rerranged
            T newSeedTerm = getTerm(seedTerm, st);
            if (newSeedTerm != seedTerm) {
                normalize = true;
                seedTerm = newSeedTerm;
            }
        }


        if (Parameters.DEBUG) {
            if (invalidSentenceTerm(seedTerm))
                throw new RuntimeException("Invalid sentence content term: " + seedTerm);
        }


        this.stamp = st;

        if (Parameters.DEBUG && Parameters.DEBUG_INVALID_SENTENCES) {
            if (!Term.valid(seedTerm)) {
                CompoundTerm.UnableToCloneException ntc = new CompoundTerm.UnableToCloneException("Invalid Sentence term: " + seedTerm);
                ntc.printStackTrace();
                throw ntc;
            }
        }




        this.revisible = !((seedTerm instanceof Conjunction) && seedTerm.hasVarDep());

        this.term = normalize ? seedTerm.cloneNormalized() : seedTerm;

        this.hash = 0;

    }

    /** pre-processes a term for sentence's use; in most cases just returns the term itself */
    protected T getTerm(T t, Stamp st) {
        if (t instanceof Conjunction) {

            //cut interval at end for sentence in serial conjunction, and inbetween for parallel
            //TODO this can be extended to remove N suffix intervals, if we ever use the multiple interval thing
            Conjunction c=(Conjunction)t;
            if(c.getTemporalOrder()==TemporalRules.ORDER_FORWARD) {
                if(c.term[c.term.length-1] instanceof Interval) {
                    Term[] term2=new Term[c.term.length-1];
                    //TODO use System.arraycopy
                    for(int i=0;i<c.term.length-1;i++) {
                        term2[i]=c.term[i];
                    }
                    T u = (T) Conjunction.make(term2, c.getTemporalOrder());

                    //if the resulting term is valid for a sentence, adjust the stamp and return the new term
                    if ((u = termOrNull(u)) != null) {

                        //ok we removed a part of the interval, we have to transform the occurence time of the sentence back
                        //accordingly
                        //TODO make this interval not hardcoded
                        long time = Interval.cycles(((Interval) c.term[c.term.length - 1]).magnitude, new Interval.AtomicDuration(st.getDuration()));
                        st.setOccurrenceTime(st.getOccurrenceTime() - time);
                        return u;
                    }
                    else {
                        //the result would not be valid, so just return the original input
                        //this sentence should not be created but in the meantime, this will prevent it from having any effect
                        truth.setConfidence(0);
                    }
                }
            }

        }
        return t;
    }


    @Deprecated final protected boolean isUniqueByOcurrenceTime() {
        return true;
        //return ((punctuation == Symbols.JUDGMENT) || (punctuation == Symbols.QUESTION));
    }
    
    /**
     * To check whether two sentences are equal
     *
     * @param that The other sentence
     * @return Whether the two sentences have the same content
     */
    @Override
    public boolean equals(final Object that) {
        if (this == that) return true;
        if (that instanceof Sentence) {
            final Sentence t = (Sentence) that;

            if (punctuation!=t.punctuation) return false;


            if (isUniqueByOcurrenceTime()) {

                if (!TemporalRules.concurrent(this, t, Parameters.SentenceOcurrenceTimeCyclesEqualityThreshold)) return false;
            }


            if (truth==null) {
                if (t.truth!=null) return false;
            }
            else /*truth!=null*/ {
                if (t.truth==null) return false;

                if (!truth.equals(t.truth)) return false;
            }

            return term.equals(t.term);
        }
        return false;
    }

    /**
     * To produce the hashcode of a sentence
     *
     * @return A hashcode
     */
    @Override
    public int hashCode() {
        if ((this.hash == 0) && (stamp!=null)) {
            if (isUniqueByOcurrenceTime())
                this.hash = Objects.hash(term, punctuation, truth, stamp.getOccurrenceTime());
            else
                this.hash = Objects.hash(term, punctuation, truth);
        }
        return hash;
    }

    /**
     * Check whether different aspects of sentence are equivalent to another one
     *
     * @param that The other judgment
     * @return Whether the two are equivalent
     */
    public boolean equivalentTo(final Sentence that, boolean term, boolean truth, boolean stamp) {

        if (this == that) return true;
        if (term) {
            if (!equalTerms(that)) return false;
        }
        if (truth) {
            if (!this.truth.equals(that.truth)) return false;
        }
        if (stamp) {
            if (!this.stamp.equals(that.stamp, true, true, true)) return false;
        }
        return true;
    }

    /**
     * Clone the Sentence
     *
     * @return The clone
     */
    @Override
    public Sentence clone() {
        return clone(term);
    }

    /** returns a valid sentence CompoundTerm, or throws an exception */
    public static CompoundTerm termOrException(Term t) {
        if (invalidSentenceTerm(t))
            throw new RuntimeException(t + " not valid sentence content");
        return ((CompoundTerm)t);
    }

    /** returns a valid sentence CompoundTerm, or returns null */
    public static <X extends CompoundTerm> X termOrNull(Term t) {
        if (invalidSentenceTerm(t))
            return null;
        return (X)t;
    }

    
    public Sentence clone(boolean makeEternal) {
        Sentence clon = clone(term);
        if(clon.stamp.getOccurrenceTime()!=Stamp.ETERNAL && makeEternal) {
            //change occurence time of clone
            clon.stamp.setEternal();
        }
        return clon;
    }


    public final <X extends CompoundTerm> Sentence<X> clone(Term t, Class<? extends X> necessaryTermType) {
        X ct = termOrNull(t);
        if (ct == null) return null;

        if (!ct.getClass().isInstance(necessaryTermType))
            return null;

        if (ct.equals(term)) {
            return (Sentence<X>) this;
        }
        return clone_(ct);

    }

    /** Clone with a different Term */
    public <X extends CompoundTerm> Sentence<? extends X> clone(Term t) {
        X ct = termOrNull(t);
        if (ct == null) return null;

        if (ct.equals(term)) {
            //throw new RuntimeException("Clone with " + t + " would produces exact sentence");
            return (Sentence<X>) this;
        }

        return clone_(ct);
    }

    protected <X extends CompoundTerm> Sentence<X> clone_(X t) {
        return new Sentence(t, punctuation,
                truth!=null ? new TruthValue(truth) : null,
                stamp.clone());
    }

//    public final Sentence clone(final CompoundTerm t) {
//        //sentence content must be compoundterm
//        if (t instanceof CompoundTerm) {
//            return this.clone((CompoundTerm)t);
//        }
//        return null;
//    }


    /**
      * project a judgment to a difference occurrence time
      *
      * @param targetTime The time to be projected into
      * @param currentTime The current time as a reference
      * @return The projected belief
      */    
    public Sentence projection(final long targetTime, final long currentTime) {
            
        final TruthValue newTruth = projectionTruth(targetTime, currentTime);
        
        final boolean eternalizing = (newTruth instanceof EternalizedTruthValue);

        Stamp newStamp = eternalizing ? stamp.cloneWithNewOccurrenceTime(Stamp.ETERNAL) : stamp.clone();

        return new Sentence(term, punctuation, newTruth, newStamp, false);
    }



    public TruthValue projectionTruth(final long targetTime, final long currentTime) {
        TruthValue newTruth = null;
                        
        if (!isEternal()) {
            newTruth = TruthFunctions.eternalize(truth);
            if (targetTime != Stamp.ETERNAL) {
                long occurrenceTime = stamp.getOccurrenceTime();
                float factor = TruthFunctions.temporalProjection(occurrenceTime, targetTime, currentTime);
                float projectedConfidence = factor * truth.getConfidence();
                if (projectedConfidence > newTruth.getConfidence()) {
                    newTruth = new TruthValue(truth.getFrequency(), projectedConfidence);
                }
            }
        }
        
        if (newTruth == null) newTruth = truth.clone();
        
        return newTruth;
    }

    /** calculates projection truth quality without creating new TruthValue instances */
    public float projectionTruthQuality(long targetTime, long currentTime, boolean problemHasQueryVar) {
        float freq = truth.getFrequency();
        float conf = truth.getConfidence();

        if (!isEternal() && (targetTime != getOccurrenceTime())) {
            conf = TruthFunctions.eternalizedConfidence(conf);
            if (targetTime != Stamp.ETERNAL) {
                long occurrenceTime = stamp.getOccurrenceTime();
                float factor = TruthFunctions.temporalProjection(occurrenceTime, targetTime, currentTime);
                float projectedConfidence = factor * truth.getConfidence();
                if (projectedConfidence > conf) {
                    conf = projectedConfidence;
                }
            }
        }

        if (problemHasQueryVar) {
            return TruthValue.expectation(freq, conf) / term.getComplexity();
        } else {
            return conf;
        }
    }



//    /**
//     * Clone the content of the sentence
//     *
//     * @return A clone of the content Term
//     */
//    public Term cloneContent() {
//        return content.clone();
//    }
//


    /**
     * Recognize a Judgment
     *
     * @return Whether the object is a Judgment
     */
    public boolean isJudgment() {
        return (punctuation == Symbols.JUDGMENT);
    }

    /**
     * Recognize a Question
     *
     * @return Whether the object is a Question
     */
    public boolean isQuestion() {
        return (punctuation == Symbols.QUESTION);
    }

    public boolean isGoal() {
        return (punctuation == Symbols.GOAL);
    }
 
    public boolean isQuest() {
        return (punctuation == Symbols.QUEST);
    }    
    
    public boolean hasQueryVar() {
        return term.hasVarQuery();
    }

    public boolean isRevisible() {
        return revisible;
    }

    public Sentence setRevisible(final boolean b) {
        revisible = b;
        return this;
    }

    public int getTemporalOrder() {
        int t = term.getTemporalOrder();
        if (t == TemporalRules.ORDER_INVALID)
            throw new RuntimeException(this + " has INVALID temporal order");
        return t;
    }
    

    public Operator getOperator() {
        if (term instanceof Operation) {
             return (Operator) ((Statement) term).getPredicate();
        } else {
             return null;
        }
    }    
    
    /**
     * Get a String representation of the sentence
     *
     * @return The String
     */
    @Override
    public String toString() {
        return getKey().toString();
    }

 
    /**
     * Get a String representation of the sentence for key of Task and TaskLink
     *
     * @return The String
     */
    public CharSequence getKey() {
        //key must be invalidated if content or truth change
        if (key == null) {
            final CharSequence contentName = term.name();
            
            final boolean showOcurrenceTime = !isEternal(); //((punctuation == Symbols.JUDGMENT) || (punctuation == Symbols.QUESTION));
            //final String occurrenceTimeString =  ? stamp.getOccurrenceTimeString() : "";
            
            //final CharSequence truthString = truth != null ? truth.name() : null;

            int stringLength = 0; //contentToString.length() + 1 + 1/* + stampString.baseLength()*/;
            if (truth != null) {
                stringLength += (showOcurrenceTime ? 8 : 0) + 11 /*truthString.length()*/;
            }

            //suffix = [punctuation][ ][truthString][ ][occurenceTimeString]
            final StringBuilder suffix = new StringBuilder(stringLength).append(punctuation);

            if (truth != null) {
                suffix.append(' ');
                truth.appendString(suffix, false);
            }
            if ((showOcurrenceTime) && (stamp!=null)) {
                suffix.append(" {"); //space + stamp opener
                stamp.appendOcurrenceTime(suffix);
                suffix.append('}'); //stamp closer
            }

            key = Texts.yarn(Parameters.ROPE_TERMLINK_TERM_SIZE_THRESHOLD, 
                    contentName,//.toString(), 
                    suffix); //.toString());
            //key = new FlatCharArrayRope(StringUtil.getCharArray(k));

        }
        return key;
    }

    /**
     * Get a String representation of the sentence for display purpose
     *
     * @return The String
     */
    public CharSequence toString(NAR nar, boolean showStamp) {
    
        CharSequence contentName = term.name();
        
        final long t = nar.memory.time();

        final String tenseString = stamp.getTense(t, nar.memory.getDuration());
        
        
        CharSequence stampString = showStamp ? stamp.name() : null;
        
        int stringLength = contentName.length() + tenseString.length() + 1 + 1;
                
        if (truth != null)
            stringLength += 11;
        
        if (showStamp)
            stringLength += stampString.length()+1;
        
        
        final StringBuilder buffer = new StringBuilder(stringLength).
                    append(contentName).append(punctuation);
        
        if (tenseString.length() > 0)
            buffer.append(' ').append(tenseString);
        
        if (truth != null) {
            buffer.append(' ');
            truth.appendString(buffer, true);
        }
        
        if (showStamp)
            buffer.append(' ').append(stampString);
        
        return buffer;
    }
    
   
    /**
     * Get the truth value (or desire value) of the sentence
     *
     * Should only be used in Concept's sentences, not in other location where Sentence is expected to be immutable
     * TODO make a distinct superclass between Mutable & ImmutableSentence for the different places Sentence are used (ex: Task vs. Concept)
     *
     * @return Truth value, null for question
     */
    public void discountConfidence() {
        truth.setConfidence(truth.getConfidence() * Parameters.DISCOUNT_RATE).setAnalytic(false);
    }


    final public boolean equalTerms(final Sentence s) {
        return term.equals(s.term);
    }
    final public boolean equalPunctuations(Sentence s) {
        return punctuation == s.punctuation;
    }

    public final boolean isEternal() {
        return stamp.isEternal();
    }

    public long getCreationTime() { return stamp.getCreationTime();    }
    public long getOccurrenceTime() {
        return stamp.getOccurrenceTime();
    }

    public boolean after(Sentence s, int duration) {
        return stamp.after(s.stamp, duration);
    }

    public boolean before(Sentence s, int duration) {
        return stamp.before(s.stamp, duration);
    }


    public static final class ExpectationComparator implements Comparator<Sentence> {
        final static Comparator the = new ExpectationComparator();
        @Override public int compare(final Sentence b, final Sentence a) {
            return Float.compare(a.truth.getExpectation(), b.truth.getExpectation());
        }
    }
    public static final class ConfidenceComparator implements Comparator<Sentence> {
        final static Comparator the = new ExpectationComparator();
        @Override public int compare(final Sentence b, final Sentence a) {
            return Float.compare(a.truth.getConfidence(), b.truth.getConfidence());
        }
    }
    
    public static List<Sentence> sortExpectation(Collection<Sentence> s) {
        List<Sentence> l = new ArrayList(s);
        Collections.sort(l, ExpectationComparator.the);
        return l;
    }
    public static List<Sentence> sortConfidence(Collection<Sentence> s) {
        List<Sentence> l = new ArrayList(s);
        Collections.sort(l, ConfidenceComparator.the);
        return l;
    }
    
    /** performs some (but not exhaustive) tests on a term to determine some cases where it is invalid as a sentence content
     * returns true if the term is invalid for use as sentence content term
     * */
    public static final boolean invalidSentenceTerm(final Term t) {
        if ((t == null) || (!(t instanceof CompoundTerm))) { //(t instanceof Interval) || (t instanceof Variable)
            return true;
        }

        if (t instanceof Statement) {
            Statement st = (Statement) t;

            /* A statement sentence is not allowed to have a independent variable as subj or pred"); */
            if (t.subjectOrPredicateIsIndependentVar())
                return true;

            if (Statement.invalidStatement(st.getSubject(), st.getPredicate()))
                return true;

        }


        //ok valid
        return false;
    }

    @Override
    public T getTerm() {
        return term;
    }

    @Override
    public TruthValue getTruth() {
        return truth;
    }


    public static class SubTermVarCollector implements Term.TermVisitor {
        private final List<Variable> vars;

        public SubTermVarCollector(List<Variable> vars) {
            this.vars = vars;
        }

        @Override public void visit(final Term t, final Term parent) {
            if (t instanceof Variable) {
                Variable v = ((Variable)t);
                vars.add(v);
            }
        }
    }

    public Stamp getStamp() {
        return stamp;
    }

}
