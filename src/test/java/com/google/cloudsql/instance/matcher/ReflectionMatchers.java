package com.google.cloudsql.instance.matcher;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.unitils.reflectionassert.ReflectionComparator;
import org.unitils.reflectionassert.ReflectionComparatorFactory;

public final class ReflectionMatchers {
  private static final ReflectionComparator REFLECTION_COMPARATOR =
      ReflectionComparatorFactory.createRefectionComparator();

  private ReflectionMatchers() {}

  public static <T> Matcher<T> isReflectionEqualTo(final T expected) {
    return new BaseMatcher<T>() {
      @Override
      public boolean matches(Object actual) {
        return REFLECTION_COMPARATOR.isEqual(expected, actual);
      }

      @Override
      public void describeTo(Description description) {
        description.appendText("should reflectively match ").appendText(expected.toString());
      }
    };
  }
}
