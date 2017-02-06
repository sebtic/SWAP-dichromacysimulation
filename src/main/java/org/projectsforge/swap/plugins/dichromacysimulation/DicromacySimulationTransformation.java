/**
 * Copyright 2010 Sébastien Aupetit <sebastien.aupetit@univ-tours.fr> This file
 * is part of SHS. SHS is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or (at your
 * option) any later version. SHS is distributed in the hope that it will be
 * useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Lesser
 * General Public License for more details. You should have received a copy of
 * the GNU Lesser General Public License along with SHS. If not, see
 * <http://www.gnu.org/licenses/>. $Id$
 */
package org.projectsforge.swap.plugins.dichromacysimulation;

import java.util.ArrayList;
import java.util.List;
import org.projectsforge.swap.core.environment.Environment;
import org.projectsforge.swap.core.handlers.Handler;
import org.projectsforge.swap.core.handlers.HandlerContext;
import org.projectsforge.swap.core.handlers.Resource;
import org.projectsforge.swap.core.http.CacheManager;
import org.projectsforge.swap.core.http.Response;
import org.projectsforge.swap.core.mime.css.GlobalStyleSheets;
import org.projectsforge.swap.core.mime.css.nodes.Media;
import org.projectsforge.swap.core.mime.css.property.color.DichromacyDeficiency;
import org.projectsforge.swap.core.mime.css.resolver.PropertyResolver;
import org.projectsforge.swap.core.mime.css.resolver.RuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.StateRecorder;
import org.projectsforge.swap.core.mime.css.resolver.color.BackgroundColorRuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.color.BorderColorRuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.color.ColorRuleSetProcessor;
import org.projectsforge.swap.core.mime.html.nodes.Document;
import org.projectsforge.swap.handlers.html.HtmlDomTransformation;
import org.projectsforge.swap.handlers.html.HtmlTransformation;
import org.projectsforge.swap.handlers.mime.StatisticsCollector;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The Class DicromacySimulationTransformation.
 * 
 * @author Sébastien Aupetit
 */
@Handler(singleton = true)
public class DicromacySimulationTransformation extends HtmlDomTransformation {

  /** The environment. */
  @Autowired
  private Environment environment;

  /** The cache manager. */
  @Autowired
  private CacheManager cacheManager;

  /** The global style sheets. */
  @Autowired
  private GlobalStyleSheets globalStyleSheets;

  @Override
  public boolean transform(final HandlerContext<HtmlTransformation> context,
      final StatisticsCollector statisticsCollector, final Response response,
      final Resource<Document> document) throws Exception {

    document.lockRead();

    try {
      final List<RuleSetProcessor> ruleSetProcessors = new ArrayList<>();
      ruleSetProcessors.add(new ColorRuleSetProcessor());
      ruleSetProcessors.add(new BackgroundColorRuleSetProcessor());
      ruleSetProcessors.add(new BorderColorRuleSetProcessor());

      final List<StateRecorder> stateRecorders = new ArrayList<>();
      final StylesRecorder stylesRecorder = environment.autowireBean(new StylesRecorder());
      stateRecorders.add(stylesRecorder);

      final PropertyResolver propertyResolver = environment.autowireBean(new PropertyResolver(
          document.get(), response.getRequest(), Media.SCREEN, globalStyleSheets
              .getUserAgentStylesheet(), globalStyleSheets.getUserStylesheet(), ruleSetProcessors,
          stateRecorders));

      propertyResolver.resolve();

      final DichromacyDeficiency deficiency = DichromacySimulationPropertyHolder.dichromacyDeficiency
          .get();

      document.lockWrite();
      try {
        stylesRecorder.doChange(deficiency, document.get());
      } finally {
        document.unlockWrite();
      }

      return true;
    } finally {
      document.unlockRead();
    }
  }

}
