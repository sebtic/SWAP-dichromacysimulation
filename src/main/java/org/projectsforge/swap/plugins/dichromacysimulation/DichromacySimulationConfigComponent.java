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

import org.projectsforge.swap.core.environment.Environment;
import org.projectsforge.swap.core.mime.css.property.color.DichromacyDeficiency;
import org.projectsforge.swap.proxy.webui.configuration.ConfigurationComponent;
import org.projectsforge.swap.proxy.webui.configuration.ConfigurationController;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * The Class DichromacySimulationConfigComponent.
 * 
 * @author Sébastien Aupetit
 */
@Component
@Controller
@RequestMapping(DichromacySimulationConfigComponent.URL)
public class DichromacySimulationConfigComponent extends ConfigurationComponent {

  /** The environment. */
  @Autowired
  private Environment environment;

  /** The Constant URL. */
  public static final String URL = ConfigurationController.URL
      + "/org.projectsforge.swap.plugins.dichromacysimulation";

  /*
   * (non-Javadoc)
   * @see
   * org.projectsforge.swap.core.web.mvc.AbstractMVCComponent#getDescription()
   */
  @Override
  public String getDescription() {
    return "Configure the dichromacy deficiency to simulate";
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.swap.core.web.mvc.AbstractMVCComponent#getName()
   */
  @Override
  public String getName() {
    return "Dichromacy simulation";
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.swap.core.web.mvc.AbstractMVCComponent#getPriority()
   */
  @Override
  public int getPriority() {
    return Integer.MAX_VALUE;
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.swap.core.web.mvc.AbstractMVCComponent#getUrl()
   */
  @Override
  public String getUrl() {
    return DichromacySimulationConfigComponent.URL;
  }

  /**
   * The GET form.
   * 
   * @return the model and view
   */
  @RequestMapping(value = "/", method = RequestMethod.GET)
  public ModelAndView handleGet() {
    if (!isActive()) {
      return getInactiveMAV();
    }

    final ModelAndView mav = new ModelAndView(
        "org.projectsforge.swap.plugins.dichromacysimulation/get");
    mav.addObject("values", DichromacyDeficiency.values());
    mav.addObject("value", DichromacySimulationPropertyHolder.dichromacyDeficiency.get());
    mav.addObject("rootline", getRootline());
    return mav;
  }

  /**
   * The POST form.
   * 
   * @param deficiency the deficiency
   * @return the model and view
   */
  @RequestMapping(value = "/", method = RequestMethod.POST)
  public ModelAndView handlePost(@RequestParam final DichromacyDeficiency deficiency) {
    if (!isActive()) {
      return getInactiveMAV();
    }

    DichromacySimulationPropertyHolder.dichromacyDeficiency.set(deficiency);
    environment.saveConfigurationProperties();

    return handleGet();
  }

  /*
   * (non-Javadoc)
   * @see org.projectsforge.swap.core.web.mvc.AbstractMVCComponent#isActive()
   */
  @Override
  public boolean isActive() {
    return true;
  }
}
