package nars.build;

import nars.logic.nal8.NullOperator;
import nars.logic.nal8.Operator;

/**
 * Operators for NAL8 Examples
 */
public class ExampleOperators {
     
    public static Operator[] get() {
        
        return new Operator[] {
            //new Wait(),            
            new NullOperator("^break"),
            new NullOperator("^drop"),
            new NullOperator("^go-to"),
            new NullOperator("^open"),
            new NullOperator("^pick"),
            new NullOperator("^strike"),
            new NullOperator("^throw"),
            new NullOperator("^activate"),
            new NullOperator("^deactivate")
        };
    }
}
