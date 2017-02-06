package org.projectsforge.swap.plugins.dichromacysimulation;

import org.projectsforge.swap.handlers.mime.StatisticsCollector;
import org.projectsforge.swap.handlers.mime.StatisticsCollectorInterceptor;

// @Component
public class DemoStatisticsCollector implements StatisticsCollectorInterceptor {

  @Override
  public void intercept(final StatisticsCollector statisticsCollector) {
    for (final String key : statisticsCollector.getTimerKeySet()) {
      System.err.println(key + " => " + statisticsCollector.getElapsedTimer(key));
    }
    for (final String key : statisticsCollector.getValueKeySet()) {
      System.err.println("Value key : " + key);
    }
  }

}
