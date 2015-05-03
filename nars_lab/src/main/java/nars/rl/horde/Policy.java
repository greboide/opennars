package nars.rl.horde;

import org.apache.commons.math3.linear.RealVector;

import java.io.Serializable;



public interface Policy<A> extends Serializable {
  void update(RealVector x);

  double pi(A a);

  A sampleAction();
}
