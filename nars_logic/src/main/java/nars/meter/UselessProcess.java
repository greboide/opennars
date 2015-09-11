package nars.meter;

import nars.NAR;
import nars.event.NARReaction;
import nars.process.ConceptProcess;
import nars.task.Task;
import nars.util.event.DefaultTopic;

import java.util.Collection;

/**
 * Meter utility for analyzing useless inference processes
 * --no derivations
 * --derivations which are rejected by memory
 * --...
 */
public class UselessProcess extends NARReaction {

    private final NAR nar;
    private final DefaultTopic.Subscription conceptProcessed;

    public UselessProcess(NAR n) {
        super(n);
        conceptProcessed = n.memory.eventConceptProcessed.on(c -> {
            onConceptProcessed(c);
        });
        this.nar = n;
    }

    @Override
    public void event(Class event, Object... args) {
    }

    void onConceptProcessed(ConceptProcess arg) {
        Collection<Task> derived = arg.get();
        int numDerived = derived.size();
        if (numDerived == 0) {
            System.err.println(nar.time() + ": " +  arg + " no derivations" );
        }
        else {
            if (arg.getTaskLink().type==0) {
                System.err.println(nar.time() + " type 0 tasklink caused derivations: " + arg);
            }
            System.err.println(nar.time() + ": " +  arg + " OK derived=" + numDerived );
        }
    }
}
