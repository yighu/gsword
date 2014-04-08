/**
 * Created by IntelliJ IDEA.
 * User: Yiguang
 * Date: Sep 7, 2009
 * Time: 9:41:49 AM
 * To change this template use File | Settings | File Templates.
 */

public class DwrConfigBootStrap {
  def init = { servletContext -> }
         def destroy = {}

         def dwrconfig = {
           create(creator:'new', javascript:'Tweek', scope:'application') {
                                 param(name:'class', value:'twitter.Tweek')
                         }
         }

}