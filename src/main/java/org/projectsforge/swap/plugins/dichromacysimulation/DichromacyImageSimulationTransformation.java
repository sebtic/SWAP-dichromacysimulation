package org.projectsforge.swap.plugins.dichromacysimulation;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.imageio.ImageIO;
import org.projectsforge.swap.core.environment.Environment;
import org.projectsforge.swap.core.handlers.Handler;
import org.projectsforge.swap.core.handlers.HandlerContext;
import org.projectsforge.swap.core.handlers.Resource;
import org.projectsforge.swap.core.http.Mime;
import org.projectsforge.swap.core.http.Response;
import org.projectsforge.swap.core.mime.css.property.color.ColorSpaceUtil;
import org.projectsforge.swap.core.mime.css.property.color.DichromacyDeficiency;
import org.projectsforge.swap.handlers.mime.MimeHandler;
import org.projectsforge.swap.handlers.mime.StatisticsCollector;
import org.projectsforge.utils.temporarystreams.TemporaryContentHolder;
import org.projectsforge.utils.temporarystreams.TemporaryStreamsFactory;
import org.springframework.beans.factory.annotation.Autowired;

@Handler(singleton = true)
@Mime(mime = { "image/gif", "image/jpg", "image/jpeg", "image/png" })
public class DichromacyImageSimulationTransformation implements MimeHandler {

  @Autowired
  private Environment environment;

  @Autowired
  private TemporaryStreamsFactory temporaryStreamsFactory;

  @Override
  public void handle(final HandlerContext<MimeHandler> context,
      final StatisticsCollector statisticsCollector, final Response response) throws IOException,
      InterruptedException {

    final DichromacyDeficiency deficiency = DichromacySimulationPropertyHolder.dichromacyDeficiency
        .get();

    final InputStream input = response.getContent().getInputStream();
    if (input == null) {
      return;
    }

    final BufferedImage image;
    try {
      image = ImageIO.read(input);
    } finally {
      input.close();
    }
    if (image == null) {
      context.getLogger().info("Can not read image {}", response);
      return;
    }

    final BufferedImage transformed_image = new BufferedImage(image.getWidth(), image.getHeight(),
        image.getColorModel().hasAlpha() ? BufferedImage.TYPE_4BYTE_ABGR
            : BufferedImage.TYPE_3BYTE_BGR);

    final int width = image.getWidth();
    final int height = image.getHeight();
    final double[] inputLab = new double[3];
    final double[] outputLab = new double[3];
    final int[] srgbOutput = new int[3];
    final boolean hasAlpha = image.getColorModel().hasAlpha();

    for (int i = 0; i < width; i++) {
      for (int j = 0; j < height; j++) {
        final int color = image.getRGB(i, j);
        final int alpha = (hasAlpha ? (color >> 24) & 0xFF : 255);
        final int red = (color >> 16) & 0xFF;
        final int green = (color >> 8) & 0xFF;
        final int blue = (color >> 0) & 0xFF;

        ColorSpaceUtil.SRGBToLab(red, green, blue, inputLab);
        DichromacyDeficiency.kuhnSimulation(deficiency, inputLab, outputLab);
        ColorSpaceUtil.LabToSRGB(outputLab, srgbOutput);

        final int newColor = ((alpha & 0xFF) << 24) | ((srgbOutput[0] & 0xFF) << 16)
            | ((srgbOutput[1] & 0xFF) << 8) | ((srgbOutput[2] & 0xFF) << 0);

        transformed_image.setRGB(i, j, newColor);
      }
    }

    final Response transformedResponse = environment.autowireBean(new Response(response));
    final TemporaryContentHolder content = new TemporaryContentHolder(temporaryStreamsFactory);
    final OutputStream out = content.getOutputStream();
    try {
      ImageIO.write(transformed_image, response.getMime().split("/")[1], out);
    } finally {
      out.close();
    }
    transformedResponse.setContent(content);
    final Resource<Response> outputResponse = context.getResource(MimeHandler.OUTPUT_RESPONSE,
        Response.class);
    outputResponse.lockWrite();
    try {
      outputResponse.set(transformedResponse);
    } finally {
      outputResponse.unlockWrite();
    }

  }
}
