package tanin.wait;

import com.renomad.minum.logging.Logger;
import com.renomad.minum.state.Constants;
import com.renomad.minum.state.Context;
import com.renomad.minum.web.FullSystem;
import com.renomad.minum.web.Response;
import com.renomad.minum.web.StatusLine;

import java.net.URI;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

import static com.renomad.minum.web.RequestLine.Method.GET;

public class MinumBuilder {
  private static final java.util.logging.Logger logger = java.util.logging.Logger.getLogger(MinumBuilder.class.getName());

  public static FullSystem build(int port) {
    var props = new Properties();
    props.setProperty("SERVER_PORT", "" + port);
    props.setProperty("LOG_LEVELS", "ASYNC_ERROR,AUDIT");
    props.setProperty("IS_THE_BRIG_ENABLED", "false");

    var executor = Executors.newVirtualThreadPerTaskExecutor();
    var constants = new Constants(props);
    var context = new Context(executor, constants, new Logger(constants, executor, "primary logger"));
    var minum = new FullSystem(context).start();

    Runtime.getRuntime().addShutdownHook(new Thread(minum::shutdown));

    sun.misc.Signal.handle(new sun.misc.Signal("INT"), sig -> {
      logger.info("Received SIGINT signal. Shutting down...");
      minum.shutdown();
    });

    return minum;
  }
}
