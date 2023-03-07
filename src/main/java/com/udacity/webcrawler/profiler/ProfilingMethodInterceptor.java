package com.udacity.webcrawler.profiler;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Objects;

/**
 * A method interceptor that checks whether {@link Method}s are annotated with the {@link Profiled}
 * annotation. If they are, the method interceptor records how long the method invocation took.
 */
final class ProfilingMethodInterceptor implements InvocationHandler {

  private final Clock clock;
  private final Object passedDelegate;
  private final ProfilingState profilingState;

  // TODO: You will need to add more instance fields and constructor arguments to this class.
  ProfilingMethodInterceptor(Clock clock, Object delegate, ProfilingState state) {
    this.clock = Objects.requireNonNull(clock);
    this.passedDelegate = delegate;
    this.profilingState = state;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

    Instant methodStartTime = null;
    Object invokedObject;
    //checks if the passed method is profiled
    if (method.isAnnotationPresent(Profiled.class)) {
      methodStartTime = clock.instant();
    }
    try {
      // method is invoked with the object that is profiled
      // this should happen with all methods
      invokedObject = method.invoke(passedDelegate, args);
    }catch (InvocationTargetException ex) {
      throw ex.getTargetException();
    } catch (IllegalAccessException e) {
      throw new RuntimeException(e);
      // placed in a finally block because even methods that throw exceptions
      // need to have their time recorded
    }finally {
      if (method.isAnnotationPresent(Profiled.class)){
        assert methodStartTime != null;
        profilingState.record(passedDelegate.getClass(), method, Duration.between(methodStartTime, clock.instant()));
      }
    }
    return invokedObject;
  }
}
