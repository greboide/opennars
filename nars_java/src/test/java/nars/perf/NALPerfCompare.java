package nars.perf;

import nars.core.NALTest;
import nars.core.NAR;
import nars.core.Parameters;
import nars.core.build.Curve;
import nars.core.build.Default;

import java.util.Collection;

import static nars.perf.NALStressMeasure.perfNAL;

/**
 * Runs NALTestPerf continuously, for profiling
 */
public class NALPerfCompare {
    static int repeats = 100;
    static int warmups = 2;
    static int extraCycles = 256;
        
    public static void compareRope(String examplePath) {
        
        for (int r = 0; r < 96; r+=12) {
            Parameters.ROPE_TERMLINK_TERM_SIZE_THRESHOLD = r;            
            System.out.print("ROPE_TERMLINK_TERM_SIZE_THRESHOLD " + r + ":\t");
            perfNAL(new NAR(new Default()), examplePath,extraCycles,repeats,warmups,true);
        }
        
        Parameters.ROPE_TERMLINK_TERM_SIZE_THRESHOLD = -1;
        System.out.print("ROPE_TERMLINK_TERM_SIZE_THRESHOLD NO:\t");
        perfNAL(new NAR(new Default()), examplePath,extraCycles,repeats,warmups,true);
        
        System.out.println();

    }
    
    public static void compareDiscreteContinuousBag(String examplePath) {
        System.out.print("DISCRETE:");
        perfNAL(new NAR(new Default()), examplePath,extraCycles,repeats,warmups,true);
        
        System.out.print("CONTINUOUS:\t");
        perfNAL(new NAR(new Curve()), examplePath,extraCycles,repeats,warmups,true);
        
        System.out.print("DISCRETE:");
        perfNAL(new NAR(new Default()), examplePath,extraCycles,repeats,warmups,true);

        
        System.out.println();

    }

    public static void main(String[] args) {


        NAR n = new NAR(new Default());
        
        Collection c = NALTest.params();
        while (true) {
            for (Object o : c) {
                String examplePath = (String)((Object[])o)[0];
                compareRope(examplePath);
                //compareDiscreteContinuousBag(examplePath);
            }
        }        
    }
}
