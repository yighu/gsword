<%--
  Created by IntelliJ IDEA.
  User: yhu
  Date: July 27, 2009
  Time: 10:55:42 AM
  To change this template use File | Settings | File Templates.
--%>

<%@ page contentType="text/html;charset=UTF-8" %>
  <div style="padding: 10px"><div style="background:#DFDDD1;font-weight: bold; padding:10px">
                  <span class="pgray">
<div class="paginateButtons">
<!--c:spaginate total="${total}" controller="bible" action="search" params='["":"${searchkk}"]' /-->
<g:paginate total="${total}" controller="bible" action="search" params='["vk":"${searchkk}"]' />

</div>
                     
                  </span>
              </div></div>
