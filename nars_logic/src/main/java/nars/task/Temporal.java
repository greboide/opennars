package nars.task;

import nars.Memory;
import nars.nal.nal7.Interval;
import nars.nal.nal7.Tense;
import nars.term.compound.Compound;

/**
 * interface for the temporal information about the
 * task to which this refers to.  used to separate
 * temporal tasks from non-temporal tasks
 */
public interface Temporal<T extends Compound> extends Tasked<T>, Interval {

    boolean isAnticipated();

    long getCreationTime();
    long getOccurrenceTime();

    void setOccurrenceTime(long t);


    default boolean concurrent(Task s, int duration) {
        return Tense.concurrent(s.getOccurrenceTime(), getOccurrenceTime(), duration);
    }

    default boolean startsAfter(Temporal other/*, int perceptualDuration*/) {
        long start = start();
        long other_end = other.end();
        return start - other_end > 0;
    }

    long start();
    long end();

    default long getLifespan(Memory memory) {
        long createdAt = getCreationTime();

        return createdAt >= Tense.TIMELESS ? memory.time() - createdAt : -1;

    }

    default boolean isTimeless() {
        return getOccurrenceTime() == Tense.TIMELESS;
    }

    default void setEternal() {
        setOccurrenceTime(Tense.ETERNAL);
    }

    default void setOccurrenceTime(Tense tense, int duration) {
        setOccurrenceTime(getCreationTime(), tense, duration);
    }

    default void setOccurrenceTime(long creation, Tense tense, int duration) {
        setOccurrenceTime(
            Tense.getOccurrenceTime(
                    creation,
                    tense,
                    duration));
    }
}