package nars.nal.nal4;

import nars.NAR;
import nars.meter.TestNAR;
import nars.nal.AbstractNALTest;
import nars.narsese.InvalidInputException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Collection;
import java.util.function.Supplier;

@RunWith(Parameterized.class)
public class NAL4MultistepTest extends AbstractNALTest {


    public NAL4MultistepTest(Supplier<NAR> b) { super(b);  }

    @Parameterized.Parameters(name= "{0}")
    public static Collection configurations() {
        return AbstractNALTest.core;
//        return Arrays.asList(new Supplier[][]{
//                {() -> new Default()},
//                //{new DefaultDeep()},
//                //{new NewDefault()},
//                //{new NewDefault().setInternalExperience(null)},
//                //{new Default().setInternalExperience(null) },
//                {() -> new Default().nal(5)},
//                //{new Classic().setInternalExperience(null) },
//
//                //{new Solid(1, 128, 1, 1, 1, 2).level(5)}
//
//
//        });
    }

    @Test
    public void nal4_everyday_reasonoing() throws InvalidInputException {
        TestNAR tester = test();
        tester.believe("<{sky} --> [blue]>",1.0f,0.9f); //en("the sky is blue");
        tester.believe("<{tom} --> cat>",1.0f,0.9f); //en("tom is a cat");
        tester.believe("<(*,{tom},{sky}) --> likes>",1.0f,0.9f); //en("tom likes the sky");
     //   test().ask("<(*,cat,[blue]) --> likes>"); //cats like blue?
        tester.mustBelieve(10000, "<(*,cat,[blue]) --> likes>", 1.0f, 0.23f); //en("A base is something that has a reaction with an acid.");
        tester.run();
    }


}