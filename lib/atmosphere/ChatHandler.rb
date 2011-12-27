require 'java'
java_package 'atmosphere'

java_import 'org.atmosphere.cpr.AtmosphereHandler'
java_import 'org.atmosphere.cpr.AtmosphereResource'
java_import 'org.atmosphere.cpr.AtmosphereResourceEvent'
java_import 'org.atmosphere.cpr.AtmosphereServlet'
java_import 'org.atmosphere.cpr.Broadcaster'
java_import 'org.atmosphere.plugin.jgroups.JGroupsFilter'
java_import 'org.atmosphere.util.XSSHtmlFilter'
java_import 'org.slf4j.Logger'
java_import 'org.slf4j.LoggerFactory'

java_import 'javax.servlet.http.HttpServletRequest'
java_import 'javax.servlet.http.HttpServletResponse'
java_import 'java.io.IOException'
java_import 'java.util.concurrent.ExecutionException'
java_import 'java.util.concurrent.Future'
java_import 'java.util.concurrent.TimeUnit'
java_import 'java.util.concurrent.atomic.AtomicBoolean'

class ChatHandler
  java_implements 'AtmosphereHandler<HttpServletRequest, HttpServletResponse>'
  
  attr_accessor :filterAdded
  
  BEGIN_SCRIPT_TAG = "<script type='text/javascript'>\n"
  END_SCRIPT_TAG = "</script>\n"
  CLUSTER = "org.atmosphere.useCluster"
  
  def initialize
    @filterAdded = AtomicBoolean.new(false)
  end
  
  java_signature 'void onRequest(AtmosphereResource<HttpServletRequest, HttpServletResponse> event) throws IOException'
  def onRequest(event)
    req = event.getRequest
    res = event.getResponse
    res.setContentType("text/html;charset=ISO-8859-1")
    
    if req.getMethod.equalsIgnoreCase("GET")
      event.suspend
  
      bc = event.getBroadcaster
      clusterType = event.getAtmosphereConfig.getInitParameter(CLUSTER)
      if !filterAdded.getAndSet(true) && clusterType != null
        if clusterType.equals("jgroups")
          event.getAtmosphereConfig.getServletContext.log("JGroupsFilter enabled")
          bc.getBroadcasterConfig.addFilter(JGroupsFilter.new(bc))
        end
      end
  
      bc.getBroadcasterConfig.addFilter(XSSHtmlFilter.new)
      f = bc.broadcast(event.getAtmosphereConfig.getWebServerName +
              "**has suspended a connection from " + req.getRemoteAddr)
      begin
          f.get
      rescue InterruptedException => ex
          logger.error("", ex)
      rescue ExecutionException => ex
          logger.error("", ex)
      end
  
      #Ping the connection every 30 seconds
      bc.scheduleFixedBroadcast(req.getRemoteAddr() + "**is still listening", 30, TimeUnit.SECONDS)
  
      #Delay a message until the next broadcast.
      bc.delayBroadcast("Delayed Chat message")
    elsif req.getMethod.equalsIgnoreCase("POST")
      action = req.getParameterValues("action")[0]
      name = req.getParameterValues("name")[0]
  
      if "login" == action
        req.getSession().setAttribute("name", name)
        event.getBroadcaster().broadcast("System Message from " +
                event.getAtmosphereConfig.getWebServerName + "**" + name + " has joined.")
      elsif "post" == action
        message = req.getParameterValues("message")[0]
        event.getBroadcaster.broadcast(name + "**" + message)
      else
        res.setStatus(422)
      end
      res.getWriter.write("success")
      res.getWriter.flush
    end
  end
  
  java_signature 'void onStateChange(AtmosphereResourceEvent<HttpServletRequest, HttpServletResponse> event) throws IOException'
  def onStateChange(event)
    req = event.getResource.getRequest
    res = event.getResource.getResponse

    unless event.getMessage.nil?
      e = event.getMessage.toString
      name = e
      message = ""
  
      if e.indexOf("**") > 0
        name = e.substring(0, e.indexOf("**"))
        message = e.substring(e.indexOf("**") + 2)
      end
  
      msg = BEGIN_SCRIPT_TAG + toJsonp(name, message) + END_SCRIPT_TAG
  
      if event.isCancelled()
        event.getResource.getBroadcaster.broadcast(req.getSession.getAttribute("name") + " has left")
      elsif event.isResuming || event.isResumedOnTimeout
        script = "<script>window.parent.app.listen();\n</script>"
        res.getWriter.write(script)
      else
        res.getWriter.write(msg)
      end
      res.getWriter.flush
    end
  end  
  
  java_signature 'void destroy()'
  def destroy
  end
  
private  
  
  java_signature 'String toJsonp(String name, String message)'
  def toJsonp(name, message)
    "window.parent.app.update({ name: \"#{name}\", message: \"#{message}\" });\n"
  end
  
  
end