/*
 * Copyright � 2005 J�r�me LOUVEL.
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.noelios.restlet.ext.jetty;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.logging.Logger;

import org.mortbay.http.HttpConnection;
import org.mortbay.http.HttpContext;
import org.mortbay.http.HttpException;
import org.mortbay.http.HttpFields;
import org.mortbay.http.HttpRequest;
import org.mortbay.http.HttpResponse;
import org.restlet.UniformCall;
import org.restlet.data.CookieSetting;
import org.restlet.data.MediaTypes;

/**
 * Restlet handler for Jetty HTTP calls.
 */
public class JettyConnection extends HttpConnection
{
   /** Serial version identifier. */
   private static final long serialVersionUID = 1L;

   /** Obtain a suitable logger. */
   private static Logger logger = Logger.getLogger("com.noelios.restlet.ext.jetty.JettyConnection");

   /**
    * Constructor.
    * @param connector     The parent Jetty connector.
    * @param remoteAddress The address of the remote end or null.
    * @param in            Input stream to read the request from.
    * @param out           Output stream to write the response to.
    * @param connection    The underlying connection object.
    */
   public JettyConnection(JettyServer connector, InetAddress remoteAddress, InputStream in, OutputStream out, Object connection)
   {
      super(connector, remoteAddress, in, out, connection);
   }

   /**
    * Handle Jetty HTTP calls.
    * @param request    The HttpRequest request.
    * @param response   The HttpResponse response.
    * @return           The HttpContext that completed handling of the request or null.
    * @exception        HttpException 
    * @exception        IOException 
    */
   protected HttpContext service(HttpRequest request, HttpResponse response) throws HttpException, IOException
   {
      long startTime = System.currentTimeMillis();

      try
      {
         UniformCall call = new JettyCall(request, response);
         getJettyConnector().getTarget().handle(call);

         // Set the status code in the response
         if(call.getStatus() != null)
         {
            response.setStatus(call.getStatus().getHttpCode());
         }

         // Set cookies
         CookieSetting cookieSetting;
         for (Iterator iter = call.getCookieSettings().iterator(); iter.hasNext(); )
         {
            cookieSetting = (CookieSetting)iter.next();
            response.addSetCookie(new JettyCookie(cookieSetting));
         }

         if ((response.getStatus() == HttpResponse.__201_Created) ||
             (response.getStatus() == HttpResponse.__300_Multiple_Choices) ||
             (response.getStatus() == HttpResponse.__301_Moved_Permanently) ||
             (response.getStatus() == HttpResponse.__302_Moved_Temporarily) ||
             (response.getStatus() == HttpResponse.__303_See_Other) || (response.getStatus() == 307))
             {
                // Extract the redirection URI from the call output
                if ((call.getOutput() != null) && (call.getOutput().getMetadata().getMediaType().equals(MediaTypes.TEXT_URI)))
                {
                   response.setField(HttpFields.__Location, call.getOutput().toString());
                   call.setOutput(null);
                }
         }

         // If an output was set during the call, copy it to the output stream;
         if (call.getOutput() != null)
         {
            response.setContentType(call.getOutput().getMetadata().getMediaType().toString());
            call.getOutput().write(response.getOutputStream());
         }

         // Commit the response and ensures that all data is flushed out to the caller
         response.commit();

         // Indicates that the request fully handled
         request.setHandled(true);
      }
      catch (Exception re)
      {
         response.setStatus(HttpResponse.__500_Internal_Server_Error);
         request.setHandled(true);
         re.printStackTrace();
      }

      long endTime = System.currentTimeMillis();
      int duration = (int)(endTime - startTime);
      logger.info("Call duration=" + duration + "ms");
      
      // TOODO
      return null;
   }

   private JettyServer getJettyConnector()
   {
      return (JettyServer)getListener();
   }
   
}



