package twitter;

/**
 * Created by IntelliJ IDEA.
 * User: Yiguang
 * Date: Sep 7, 2009
 * Time: 9:33:50 AM
 * To change this template use File | Settings | File Templates.
 */
import java.util.Collection;

import javax.servlet.ServletContext;

/*import org.directwebremoting.WebContext;
import org.directwebremoting.WebContextFactory;
import org.directwebremoting.proxy.dwr.Util;

import org.directwebremoting.ServerContext;
import org.directwebremoting.ServerContextFactory;

import org.directwebremoting.util.Logger;*/

public class Tweek{ 
        //implements Runnable{
/*    public Tweek()
        {
            ServletContext servletContext = WebContextFactory.get().getServletContext();
            sctx = ServerContextFactory.get(servletContext);
         
        }
    public synchronized void begin()
       {
          
                update();
               new Thread(this).start();

       }
    public void update(){
                    Collection sessions = sctx.getScriptSessionsByPage("/gsword/gbook/v");
                          Util utilAll = new Util(sessions);
                          
                          utilAll.setValue("display_dict", " date nddd ");
        System.out.println("sessions:"+sessions.size());

    }
    *//* (non-Javadoc)
        * @see java.lang.Runnable#run()
        *//*
       public void run()
       {
           try
           {
               log.debug("Tweek: Starting server-side thread");
               System.out.println ("start tweet...") ;
               while (active)
               {

                   update();
                  // log.debug("Sent message");
                   Thread.sleep(60*1000);
               }
             System.out.println ("End tweet...");
           }
           catch (InterruptedException ex)
           {
               ex.printStackTrace();
           }
       }
    *//**
        * Our key to get hold of ServerContexts
        *//*
       private ServerContext sctx;

       *//**
        * Are we updating the clocks on all the pages?
        *//*
       private boolean active = true;

       *//**
        * The log stream
        *//*
       private static final Logger log = Logger.getLogger(Tweek.class);*/

}
