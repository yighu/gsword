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
<div class="paginateButtons">Chinese Keys
<span class="pager-list"><strong class="pager-current">
<g:paginate total="${totalkeyc}" controller="bible" action="search" params='["vk":"ChiUn"]' />
</strong>
</span></div>
                     
                  </span>
              </div></div>
