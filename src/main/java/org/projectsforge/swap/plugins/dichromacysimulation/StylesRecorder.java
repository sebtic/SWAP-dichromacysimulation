package org.projectsforge.swap.plugins.dichromacysimulation;

import java.util.HashSet;
import java.util.Set;
import org.projectsforge.swap.core.mime.css.property.color.DichromacyDeficiency;
import org.projectsforge.swap.core.mime.css.property.color.ImmutableSRGBColor;
import org.projectsforge.swap.core.mime.css.resolver.CssRule;
import org.projectsforge.swap.core.mime.css.resolver.CssRule.Origin;
import org.projectsforge.swap.core.mime.css.resolver.Properties;
import org.projectsforge.swap.core.mime.css.resolver.ResolverState;
import org.projectsforge.swap.core.mime.css.resolver.StateRecorder;
import org.projectsforge.swap.core.mime.css.resolver.color.BackgroundColorRuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.color.BorderColorRuleSetProcessor;
import org.projectsforge.swap.core.mime.css.resolver.color.ColorAndRule;
import org.projectsforge.swap.core.mime.css.resolver.color.ColorRuleSetProcessor;
import org.projectsforge.swap.core.mime.html.nodes.Document;
import org.projectsforge.swap.core.mime.html.nodes.Text;
import org.projectsforge.swap.core.mime.html.nodes.elements.AbstractElement;
import org.projectsforge.swap.core.mime.html.nodes.elements.Attributes;
import org.projectsforge.swap.core.mime.html.nodes.elements.ElementFactory;
import org.projectsforge.swap.core.mime.html.nodes.elements.HEADElement;
import org.projectsforge.swap.core.mime.html.nodes.elements.HTMLElement;
import org.projectsforge.swap.core.mime.html.nodes.elements.STYLEElement;
import org.springframework.beans.factory.annotation.Autowired;

public class StylesRecorder implements StateRecorder {

  private final Set<ColorAndRule> foregroundColor = new HashSet<>();

  private final Set<ColorAndRule> backgroundColor = new HashSet<>();

  private final Set<ColorAndRule> borderTopColor = new HashSet<>();

  private final Set<ColorAndRule> borderLeftColor = new HashSet<>();

  private final Set<ColorAndRule> borderBottomColor = new HashSet<>();

  private final Set<ColorAndRule> borderRightColor = new HashSet<>();

  /** The element factory. */
  @Autowired
  private ElementFactory elementFactory;

  public void doChange(final DichromacyDeficiency deficiency, final Document document) {
    final StringBuilder stylesheet = new StringBuilder();

    doChange(deficiency, stylesheet, foregroundColor, "color");
    doChange(deficiency, stylesheet, backgroundColor, "background-color");
    doChange(deficiency, stylesheet, borderTopColor, "border-top-color");
    doChange(deficiency, stylesheet, borderLeftColor, "border-left-color");
    doChange(deficiency, stylesheet, borderBottomColor, "border-bottom-color");
    doChange(deficiency, stylesheet, borderRightColor, "border-right-color");

    if (stylesheet.length() != 0) {
      final STYLEElement styleElement = elementFactory.newElement(STYLEElement.class);
      styleElement.addChildAtEnd(new Text(stylesheet.toString()));
      styleElement.setAttribute(Attributes.TYPE, "text/css");
      HEADElement head = document.getFirstChildrenToLeaves(HEADElement.class);
      if (head == null) {
        head = elementFactory.newElement(HEADElement.class);
        HTMLElement html = document.getFirstChildrenToLeaves(HTMLElement.class);
        if (html == null) {
          html = elementFactory.newElement(HTMLElement.class);
          document.addChildAtEnd(html);
        }
        html.addChildAtEnd(head);
      }
      head.addChildAtEnd(styleElement);
    }
  }

  private void doChange(final DichromacyDeficiency deficiency, final StringBuilder stylesheet,
      final Set<ColorAndRule> cars, final String styleName) {
    for (final ColorAndRule car : cars) {
      if (car == null || car.color == null) {
        continue;
      }
      final CssRule cssRule = car.rule;
      final ImmutableSRGBColor color = car.color.kuhnSimulationOfDichromacy(deficiency,
          ImmutableSRGBColor.class);

      if (cssRule.getOrigin() == Origin.HTMLTAG_PROPERTY
          || cssRule.getOrigin() == Origin.HTMLTAG_STYLE_PROPERTY) {
        final AbstractElement element = cssRule.getElement();
        String style = element.getAttribute(Attributes.STYLE);
        if (style == null) {
          style = "";
        }
        style = style + ";" + styleName + ":" + color;
        element.setAttribute(Attributes.STYLE, style);
      } else {
        stylesheet.append(cssRule.getSelector());
        stylesheet.append("{ " + styleName + ":").append(color).append("; }\n");
      }
    }
  }

  @Override
  public synchronized void element(final ResolverState resolverState) {
    final Properties prop = resolverState.getElementProperties();

    borderTopColor.add(prop.get(BorderColorRuleSetProcessor.CSS_BORDER_TOP_COLOR,
        ColorAndRule.class));

    borderLeftColor.add(prop.get(BorderColorRuleSetProcessor.CSS_BORDER_LEFT_COLOR,
        ColorAndRule.class));

    borderBottomColor.add(prop.get(BorderColorRuleSetProcessor.CSS_BORDER_BOTTOM_COLOR,
        ColorAndRule.class));

    borderRightColor.add(prop.get(BorderColorRuleSetProcessor.CSS_BORDER_RIGHT_COLOR,
        ColorAndRule.class));
  }

  @Override
  public synchronized void text(final ResolverState parentResolverState, final Text text) {
    // are we in the body tag ?
    if (!parentResolverState.isInBodyTagBranch()) {
      return;
    }

    final Properties prop = parentResolverState.getElementProperties();
    foregroundColor.add(prop.get(ColorRuleSetProcessor.CSS_FOREGROUND_COLOR, ColorAndRule.class));
    backgroundColor.add(prop.get(BackgroundColorRuleSetProcessor.CSS_BACKGROUND_COLOR,
        ColorAndRule.class));
  }
}
