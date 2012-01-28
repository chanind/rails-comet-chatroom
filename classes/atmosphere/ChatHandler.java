package atmosphere;

import org.jruby.Ruby;
import org.jruby.RubyObject;
import org.jruby.javasupport.util.RuntimeHelpers;
import org.jruby.runtime.builtin.IRubyObject;
import org.jruby.javasupport.JavaUtil;
import org.jruby.RubyClass;
import org.atmosphere.cpr.AtmosphereHandler;
import org.atmosphere.cpr.AtmosphereResource;
import org.atmosphere.cpr.AtmosphereResourceEvent;
import org.atmosphere.cpr.AtmosphereServlet;
import org.atmosphere.cpr.Broadcaster;
import org.atmosphere.plugin.jgroups.JGroupsFilter;
import org.atmosphere.util.XSSHtmlFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;


public class ChatHandler extends RubyObject implements AtmosphereHandler<HttpServletRequest, HttpServletResponse> {
    private static final Ruby __ruby__ = Ruby.getGlobalRuntime();
    private static final RubyClass __metaclass__;

    static {
        String source = new StringBuilder("require 'java'\n" +
            "java_package 'atmosphere'\n" +
            "\n" +
            "java_import 'org.atmosphere.cpr.AtmosphereHandler'\n" +
            "java_import 'org.atmosphere.cpr.AtmosphereResource'\n" +
            "java_import 'org.atmosphere.cpr.AtmosphereResourceEvent'\n" +
            "java_import 'org.atmosphere.cpr.AtmosphereServlet'\n" +
            "java_import 'org.atmosphere.cpr.Broadcaster'\n" +
            "java_import 'org.atmosphere.plugin.jgroups.JGroupsFilter'\n" +
            "java_import 'org.atmosphere.util.XSSHtmlFilter'\n" +
            "java_import 'org.slf4j.Logger'\n" +
            "java_import 'org.slf4j.LoggerFactory'\n" +
            "\n" +
            "java_import 'javax.servlet.http.HttpServletRequest'\n" +
            "java_import 'javax.servlet.http.HttpServletResponse'\n" +
            "java_import 'java.io.IOException'\n" +
            "java_import 'java.util.concurrent.ExecutionException'\n" +
            "java_import 'java.util.concurrent.Future'\n" +
            "java_import 'java.util.concurrent.TimeUnit'\n" +
            "java_import 'java.util.concurrent.atomic.AtomicBoolean'\n" +
            "\n" +
            "class ChatHandler\n" +
            "  java_implements 'AtmosphereHandler<HttpServletRequest, HttpServletResponse>'\n" +
            "  \n" +
            "  attr_accessor :filterAdded, :logger\n" +
            "  \n" +
            "  BEGIN_SCRIPT_TAG = \"<script type='text/javascript'>\\n\"\n" +
            "  END_SCRIPT_TAG = \"</script>\\n\"\n" +
            "  CLUSTER = \"org.atmosphere.useCluster\"\n" +
            "  \n" +
            "  \n" +
            "  def initialize\n" +
            "    @filterAdded = AtomicBoolean.new(false)\n" +
            "    @logger = LoggerFactory.getLogger(\"ChatHandler\")\n" +
            "  end\n" +
            "  \n" +
            "  java_signature 'void onRequest(AtmosphereResource<HttpServletRequest, HttpServletResponse> event) throws IOException'\n" +
            "  def onRequest(event)\n" +
            "    req = event.getRequest\n" +
            "    res = event.getResponse\n" +
            "    res.setContentType(\"text/html;charset=ISO-8859-1\")\n" +
            "    if req.getMethod.upcase == \"GET\"\n" +
            "      event.suspend\n" +
            "  \n" +
            "      bc = event.getBroadcaster\n" +
            "      clusterType = event.getAtmosphereConfig.getInitParameter(CLUSTER)\n" +
            "      if !filterAdded.getAndSet(true) && !clusterType.nil?\n" +
            "        if clusterType.equals(\"jgroups\")\n" +
            "          event.getAtmosphereConfig.getServletContext.log(\"JGroupsFilter enabled\")\n" +
            "          bc.getBroadcasterConfig.addFilter(JGroupsFilter.new(bc))\n" +
            "        end\n" +
            "      end\n" +
            "      bc.getBroadcasterConfig.addFilter(XSSHtmlFilter.new)\n" +
            "      f = bc.broadcast(event.getAtmosphereConfig.getWebServerName +\n" +
            "              \"**has suspended a connection from \" + req.getRemoteAddr)\n" +
            "      begin\n" +
            "          f.get\n" +
            "      rescue InterruptedException => ex\n" +
            "          logger.error(\"\", ex)\n" +
            "      rescue ExecutionException => ex\n" +
            "          logger.error(\"\", ex)\n" +
            "      end\n" +
            "  \n" +
            "      #Ping the connection every 30 seconds\n" +
            "      bc.scheduleFixedBroadcast(req.getRemoteAddr() + \"**is still listening\", 30, TimeUnit::SECONDS)\n" +
            "      #Delay a message until the next broadcast.\n" +
            "      bc.delayBroadcast(\"Delayed Chat message\")\n" +
            "    elsif req.getMethod.upcase == \"POST\"\n" +
            "      action = req.getParameterValues(\"action\")[0]\n" +
            "      name = req.getParameterValues(\"name\")[0]\n" +
            "      if \"login\" == action\n" +
            "        req.getSession().setAttribute(\"name\", name)\n" +
            "        event.getBroadcaster().broadcast(\"System Message from \" +\n" +
            "                event.getAtmosphereConfig.getWebServerName + \"**\" + name + \" has joined.\")\n" +
            "      elsif \"post\" == action\n" +
            "        message = req.getParameterValues(\"message\")[0]\n" +
            "        event.getBroadcaster.broadcast(name + \"**\" + message)\n" +
            "      else\n" +
            "        res.setStatus(422)\n" +
            "      end\n" +
            "      res.getWriter.write(\"success\")\n" +
            "      res.getWriter.flush\n" +
            "    end\n" +
            "  end\n" +
            "  \n" +
            "  java_signature 'void onStateChange(AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) throws IOException'\n" +
            "  def onStateChange(event)\n" +
            "    req = event.getResource.getRequest\n" +
            "    res = event.getResource.getResponse\n" +
            "    \n" +
            "    e = event.getMessage.to_s\n" +
            "    split_message = e.split('**')\n" +
            "    name = split_message[0]\n" +
            "    message = split_message[1] || \"\"\n" +
            "\n" +
            "    msg = BEGIN_SCRIPT_TAG + to_jsonp(name, message) + END_SCRIPT_TAG\n" +
            "    if event.isCancelled()\n" +
            "      event.getResource.getBroadcaster.broadcast(req.getSession.getAttribute(\"name\") + \" has left\")\n" +
            "    elsif event.isResuming || event.isResumedOnTimeout\n" +
            "      script = \"<script>window.parent.app.listen();\\n</script>\"\n" +
            "      res.getWriter.write(script)\n" +
            "    else\n" +
            "      res.getWriter.write(msg)\n" +
            "    end\n" +
            "    res.getWriter.flush\n" +
            "  end  \n" +
            "  \n" +
            "  java_signature 'void destroy()'\n" +
            "  def destroy\n" +
            "  end\n" +
            "  \n" +
            "private  \n" +
            "  \n" +
            "  def to_jsonp(name, message)\n" +
            "    \"window.parent.app.update({ name: \\\"#{name}\\\", message: \\\"#{message}\\\" });\\n\"\n" +
            "  end\n" +
            "  \n" +
            "  \n" +
            "end").toString();
        __ruby__.executeScript(source, "atmosphere/ChatHandler.rb");
        RubyClass metaclass = __ruby__.getClass("ChatHandler");
        metaclass.setRubyStaticAllocator(ChatHandler.class);
        if (metaclass == null) throw new NoClassDefFoundError("Could not load Ruby class: ChatHandler");
        __metaclass__ = metaclass;
    }

    /**
     * Standard Ruby object constructor, for construction-from-Ruby purposes.
     * Generally not for user consumption.
     *
     * @param ruby The JRuby instance this object will belong to
     * @param metaclass The RubyClass representing the Ruby class of this object
     */
    private ChatHandler(Ruby ruby, RubyClass metaclass) {
        super(ruby, metaclass);
    }

    /**
     * A static method used by JRuby for allocating instances of this object
     * from Ruby. Generally not for user comsumption.
     *
     * @param ruby The JRuby instance this object will belong to
     * @param metaclass The RubyClass representing the Ruby class of this object
     */
    public static IRubyObject __allocate__(Ruby ruby, RubyClass metaClass) {
        return new ChatHandler(ruby, metaClass);
    }

    
    public  ChatHandler() {
        this(__ruby__, __metaclass__);

        RuntimeHelpers.invoke(__ruby__.getCurrentContext(), this, "initialize");

    }

    
    public void onRequest(AtmosphereResource event) {
        IRubyObject ruby_event = JavaUtil.convertJavaToRuby(__ruby__, event);
        IRubyObject ruby_result = RuntimeHelpers.invoke(__ruby__.getCurrentContext(), this, "onRequest", ruby_event);
        return;

    }

    
    public void onStateChange(AtmosphereResourceEvent event) {
        IRubyObject ruby_event = JavaUtil.convertJavaToRuby(__ruby__, event);
        IRubyObject ruby_result = RuntimeHelpers.invoke(__ruby__.getCurrentContext(), this, "onStateChange", ruby_event);
        return;

    }

    
    public void destroy() {

        IRubyObject ruby_result = RuntimeHelpers.invoke(__ruby__.getCurrentContext(), this, "destroy");
        return;

    }

    
    public Object to_jsonp(Object name, Object message) {
        IRubyObject ruby_name = JavaUtil.convertJavaToRuby(__ruby__, name);
        IRubyObject ruby_message = JavaUtil.convertJavaToRuby(__ruby__, message);
        IRubyObject ruby_result = RuntimeHelpers.invoke(__ruby__.getCurrentContext(), this, "to_jsonp", ruby_name, ruby_message);
        return (Object)ruby_result.toJava(Object.class);

    }

}
