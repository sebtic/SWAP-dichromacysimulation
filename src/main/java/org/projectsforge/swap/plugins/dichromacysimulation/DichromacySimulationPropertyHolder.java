package org.projectsforge.swap.plugins.dichromacysimulation;

import org.projectsforge.swap.core.mime.css.property.color.DichromacyDeficiency;
import org.projectsforge.utils.propertyregistry.EnumProperty;
import org.projectsforge.utils.propertyregistry.PropertyHolder;

public class DichromacySimulationPropertyHolder implements PropertyHolder {

  public static final EnumProperty<DichromacyDeficiency> dichromacyDeficiency = new EnumProperty<DichromacyDeficiency>(
      "org.projectsforge.swap.plugins.dichromacysimulation.DicromacySimulationTransformation.deficiency",
      DichromacyDeficiency.class, DichromacyDeficiency.Deuteranope);
}
